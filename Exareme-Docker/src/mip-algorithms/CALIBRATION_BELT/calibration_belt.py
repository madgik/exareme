from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from itertools import groupby
from operator import itemgetter
from math import sqrt, exp, pi, asin, acos, atan

import numpy as np
from scipy.integrate import quad
from scipy.stats import chi2
from scipy.special import expit, logit, xlogy
from mipframework import Algorithm, AlgorithmResult, UserError
from mipframework.highcharts import CalibrationBeltPlot
from mipframework import create_runner
from mipframework.constants import (
    PREC,
    MAX_ITER,
)


class CalibrationBelt(Algorithm):
    def __init__(self, cli_args):
        super(CalibrationBelt, self).__init__(__file__, cli_args, intercept=False)

    def local_init(self):
        observed = np.array(self.data.variables.iloc[:, 1]).flatten()
        expected = np.array(self.data.covariables).flatten()
        max_deg = int(self.parameters.max_deg)
        if self.parameters.devel == "external":
            if not 2 <= max_deg <= 4:
                raise UserError(
                    "Max deg should be between 2 and 4 for " "`devel`=`external`."
                )
        elif self.parameters.devel == "internal":
            if not 3 <= max_deg <= 4:
                raise UserError(
                    "Max deg should be between 3 and 4 for `devel`=`internal`."
                )
        n_obs = len(expected)
        ge = logit(expected)

        Y = observed
        X = np.array([np.power(ge, i) for i in range(max_deg + 1)]).T
        Xs = np.broadcast_to(X, (4, n_obs, max_deg + 1))
        masks = np.zeros(Xs.shape, dtype=bool)
        for idx in range(0, max_deg - 1):
            masks[idx, :, idx + 2 :] = True
        Xs = np.ma.masked_array(Xs, mask=masks)

        self.store(Xs=Xs)
        self.store(Y=Y)
        self.push_and_add(n_obs=n_obs)

    def global_init(self):
        n_obs = self.fetch("n_obs")
        max_deg = int(self.parameters.max_deg)

        iter_ = 0

        coeffs, lls = init_model(max_deg, n_obs)

        self.store(n_obs=n_obs)
        self.store(lls=lls)
        self.store(coeffs=coeffs)
        self.store(iter_=iter_)
        self.push(coeffs=coeffs)

    def local_step(self):
        Xs = self.load("Xs")
        Y = self.load("Y")
        max_deg = int(self.parameters.max_deg)
        coeffs = self.fetch("coeffs")

        grads, hessians, lls = update_local_model_parameters(Xs, Y, coeffs, max_deg)

        self.push_and_add(lls=lls)
        self.push_and_add(grads=grads)
        self.push_and_add(hessians=hessians)

    def global_step(self):
        max_deg = int(self.parameters.max_deg)
        lls_old = self.load("lls")
        iter_ = self.load("iter_")
        grads = self.fetch("grads")
        lls = self.fetch("lls")
        hessians = self.fetch("hessians")

        coeffs = update_coefficients(grads, hessians, max_deg)

        # Update termination quantities
        deltas = abs(lls - lls_old)
        if all(deltas < PREC) or iter_ >= MAX_ITER:
            self.terminate()
        iter_ += 1

        self.store(lls=lls)
        self.store(coeffs=coeffs)
        self.store(grads=grads)
        self.store(hessians=hessians)
        self.store(iter_=iter_)
        self.push(coeffs=coeffs)

    def local_final(self):
        Xs = self.load("Xs")
        Y = self.load("Y")

        log_like_bisector = compute_log_like_on_bisector(Xs, Y)

        e_min = min(np.array(self.data.covariables).flatten())
        e_max = max(np.array(self.data.covariables).flatten())

        self.push_and_agree(e_name=self.parameters.x[0])
        self.push_and_agree(o_name=self.parameters.y[0])
        self.push_and_add(log_like_bisector=log_like_bisector)
        self.push_and_min(e_min=e_min)
        self.push_and_max(e_max=e_max)

    def global_final(self):
        devel = self.parameters.devel
        threshold = float(self.parameters.thres)
        max_deg = int(self.parameters.max_deg)
        num_points = int(self.parameters.num_points)
        lls = self.load("lls")
        hessians = self.load("hessians")
        coeffs = self.load("coeffs")
        log_like_bisector = self.fetch("log_like_bisector")
        e_bounds = self.fetch("e_min"), self.fetch("e_max")
        confidence_levels = [float(cl) for cl in self.parameters.confLevels.split(",")]

        coeff, covariance, hess, ll, model_deg = select_model(
            coeffs, devel, hessians, lls, max_deg, threshold
        )

        p_value = compute_pvalue(devel, ll, log_like_bisector, model_deg, threshold)

        G, calibration_curve, e = compute_calibration_curve(coeff, num_points, e_bounds)

        calibration_belts, p_min, p_max = compute_calibration_belt(
            G, coeff, covariance, confidence_levels
        )

        over_bisect1 = find_relative_to_bisector(np.around(e, 4), p_min[0], "over")
        under_bisect1 = find_relative_to_bisector(np.around(e, 4), p_max[0], "under")
        over_bisect2 = find_relative_to_bisector(np.around(e, 4), p_min[1], "over")
        under_bisect2 = find_relative_to_bisector(np.around(e, 4), p_max[1], "under")

        cl1, cl2 = [float(cl) for cl in self.parameters.confLevels.split(",")]
        raw_data = {
            "Model degree": int(model_deg),
            "coeff": coeff.tolist(),
            "log-likelihood": ll,
            "n_obs": int(self.load("n_obs")),
            "seqP": np.around(e, 8).tolist(),
            "Calibration curve": np.around(calibration_curve, 4).tolist(),
            "Calibration belt 1": np.around(calibration_belts[0], 8).tolist(),
            "Calibration belt 2": np.around(calibration_belts[1], 8).tolist(),
            "p value": p_value,
            "Over bisector 1": over_bisect1,
            "Under bisector 1": under_bisect1,
            "Over bisector 2": over_bisect2,
            "Under bisector 2": under_bisect2,
            "Confidence level 1": str(int(cl1 * 100)) + "%",
            "Confidence level 2": str(int(cl2 * 100)) + "%",
            "Threshold": str(int(threshold * 100)) + "%",
            "Expected name": self.fetch("e_name"),
            "Observed name": self.fetch("o_name"),
        }

        calibration_belt1_hc = np.array([e, p_min[0], p_max[0]]).transpose()
        calibration_belt2_hc = np.array([e, p_min[1], p_max[1]]).transpose()
        chart = CalibrationBeltPlot(
            title="Calibration Belt",
            data=[calibration_belt1_hc.tolist(), calibration_belt2_hc.tolist()],
            confidence_levels=[cl1, cl2],
            e_name=self.fetch("e_name"),
            o_name=self.fetch("o_name"),
        )

        self.result = AlgorithmResult(raw_data, highcharts=[chart])


def init_model(max_deg, n_obs):
    lls = np.ones((max_deg,), dtype=np.float) * (-2 * n_obs * np.log(2))
    coeffs = np.zeros((max_deg, max_deg + 1))
    masks = np.zeros(coeffs.shape, dtype=bool)
    for idx in range(0, max_deg - 1):
        masks[idx, idx + 2 :] = True
    coeffs = np.ma.masked_array(coeffs, mask=masks)
    return coeffs, lls


def update_local_model_parameters(Xs, Y, coeffs, max_deg):
    hessians = np.ma.empty((max_deg, coeffs.shape[1], coeffs.shape[1]), dtype=np.float)
    grads = np.ma.empty((max_deg, coeffs.shape[1]), dtype=np.float)
    lls = np.empty((max_deg,), dtype=np.float)
    for idx in range(max_deg):
        X = Xs[idx]
        coeff = coeffs[idx]

        z = np.ma.dot(X, coeff)
        s = expit(z)
        d = np.multiply(s, (1 - s))
        D = np.diag(d)

        hess = np.ma.dot(np.transpose(X), np.ma.dot(D, X))
        hessians[idx] = hess

        Ymsd = (Y - s) / d  # Stable computation of (Y - s) / d
        Ymsd[(Y == 0) & (s == 0)] = -1
        Ymsd[(Y == 1) & (s == 1)] = 1
        Ymsd = Ymsd.clip(-1e6, 1e6)
        grad = np.ma.dot(np.transpose(X), np.ma.dot(D, z + Ymsd))
        grads[idx] = grad

        ll = np.sum(xlogy(Y, s) + xlogy(1 - Y, 1 - s))
        lls[idx] = ll
    return grads, hessians, lls


def update_coefficients(grads, hessians, max_deg):
    coeffs = np.zeros((max_deg, max_deg + 1))
    masks = np.zeros(coeffs.shape, dtype=bool)
    for idx in range(0, max_deg - 1):
        masks[idx, idx + 2 :] = True
    coeffs = np.ma.masked_array(coeffs, mask=masks)
    if np.isinf(hessians).any():
        hessians = hessians.clip(-1e6, 1e6)
    if np.isinf(grads).any():
        grads = grads.clip(-1e6, 1e6)
    for idx in range(max_deg):
        hess = hessians[idx]
        covariance = np.ma.zeros(hess.shape)
        covariance.mask = hess.mask
        try:
            covariance[: idx + 2, : idx + 2] = np.linalg.inv(hess[: idx + 2, : idx + 2])
            if np.isnan(covariance[: idx + 2, : idx + 2]).any():
                raise np.linalg.LinAlgError
        except np.linalg.LinAlgError:
            covariance[: idx + 2, : idx + 2] = np.linalg.pinv(
                hess[: idx + 2, : idx + 2]
            )
        if (
            np.isclose(grads[idx], 0.0).any()
            and np.isinf(covariance[: idx + 2, : idx + 2]).any()
        ):
            covariance[: idx + 2, : idx + 2] = covariance[: idx + 2, : idx + 2].clip(
                -1e6, 1e6
            )

        coeffs[idx] = np.ma.dot(covariance, grads[idx])

    return coeffs


def compute_log_like_on_bisector(Xs, Y):
    X = Xs[0]
    coeff = np.zeros(X.shape[-1])
    coeff[1] = 1
    z = np.dot(X, coeff)
    s = expit(z)
    log_like_bisector = np.sum(xlogy(Y, s) + xlogy(1 - Y, 1 - s))
    return log_like_bisector


def select_model(coeffs, devel, hessians, lls, max_deg, thres):
    if devel == "external":
        idx = 0
    elif devel == "internal":
        idx = 1
    else:
        raise ValueError("devel should be `internal` or `external`")
    crit = chi2.ppf(q=thres, df=1)
    for i in range(idx + 1, max_deg):
        ddev = 2 * (lls[i] - lls[i - 1])
        if ddev > crit:
            idx = i
        else:
            break
    model_deg = idx + 1
    hess = hessians[idx]
    ll = lls[idx]
    coeff = coeffs[idx]
    coeff = coeff[~coeff.mask]
    covariance = np.linalg.inv(hess[: idx + 2, : idx + 2])
    return coeff, covariance, hess, ll, model_deg


def compute_pvalue(devel, ll, log_lik_bisector, model_deg, thres):
    calibration_stat = 2 * (ll - log_lik_bisector)
    p_value = 1 - giviti_stat_cdf(
        calibration_stat, m=model_deg, devel=devel, thres=thres
    )
    return p_value


def compute_calibration_curve(coeff, num_points, e_bounds):
    e_min, e_max = e_bounds
    e_lin = np.linspace(e_min, e_max, num=(int(num_points) + 1) // 2)
    e_log = expit(np.linspace(logit(e_min), logit(e_max), num=int(num_points) // 2))
    e = np.concatenate((e_lin, e_log))
    e = np.sort(e)
    ge = logit(e)
    G = np.array([np.power(ge, i) for i in range(len(coeff))]).T
    p = expit(np.dot(G, coeff))
    calibration_curve = np.array([e, p]).transpose()
    return G, calibration_curve, e


def compute_calibration_belt(G, coeff, covariance, confidence_levels):
    GVG = np.einsum("li, ij, lj -> l", G, covariance, G)
    sqrt_chi_GVG = np.sqrt(
        np.multiply(chi2.ppf(q=confidence_levels, df=2)[:, np.newaxis], GVG)
    )
    g_min = np.dot(G, coeff) - sqrt_chi_GVG
    g_max = np.dot(G, coeff) + sqrt_chi_GVG
    p_min, p_max = expit(g_min), expit(g_max)
    calibration_belts = np.array([p_min, p_max])
    calibration_belts = np.einsum("ijk -> jik", calibration_belts)
    return calibration_belts, p_min, p_max


def giviti_stat_cdf(t, m, devel="external", thres=0.95):
    assert m in {1, 2, 3, 4}, "m must be an integer from 1 to 4"
    assert 0 <= thres <= 1, "thres must be a number in [0, 1]"
    p_deg_inc = 1 - thres
    k = chi2.ppf(q=1 - p_deg_inc, df=1)
    cdf_value = None
    if devel == "external":
        if t <= (m - 1) * k:
            cdf_value = 0
        else:
            if m == 1:
                cdf_value = chi2.cdf(t, df=2)
            elif m == 2:
                cdf_value = (
                    chi2.cdf(t, df=1)
                    - 1
                    + p_deg_inc
                    + (-1) * sqrt(2) / sqrt(pi) * exp(-t / 2) * (sqrt(t) - sqrt(k))
                ) / p_deg_inc
            elif m == 3:
                integral1 = quad(
                    lambda y: (chi2.cdf(t - y, df=1) - 1 + p_deg_inc)
                    * chi2.pdf(y, df=1),
                    k,
                    t - k,
                )[0]
                integral2 = quad(
                    lambda y: (sqrt(t - y) - sqrt(k)) * 1 / sqrt(y), k, t - k
                )[0]
                num = integral1 - exp(-t / 2) / (2 * pi) * 2 * integral2
                den = p_deg_inc ** 2
                cdf_value = num / den
            elif m == 4:
                integral = quad(
                    lambda r: r ** 2
                    * (exp(-(r ** 2) / 2) - exp(-t / 2))
                    * (
                        -pi * sqrt(k) / (2 * r)
                        + 2 * sqrt(k) / r * asin((r ** 2 / k - 1) ** (-1 / 2))
                        - 2 * atan((1 - 2 * k / r ** 2) ** (-1 / 2))
                        + 2 * sqrt(k) / r * atan((r ** 2 / k - 2) ** (-1 / 2))
                        + 2 * atan(r / sqrt(k) * sqrt(r ** 2 / k - 2))
                        - 2 * sqrt(k) / r * atan(sqrt(r ** 2 / k - 2))
                    ),
                    sqrt(3 * k),
                    sqrt(t),
                )[0]
                cdf_value = (2 / (pi * p_deg_inc ** 2)) ** (3 / 2) * integral
    elif devel == "internal":
        assert m != 1, "if devel=`internal`, m must be an integer from 2 to 4"
        if t <= (m - 2) * k:
            cdf_value = 0
        else:
            if m == 2:
                cdf_value = chi2.cdf(t, df=1)
            elif m == 3:
                integral = quad(
                    lambda r: r * exp(-(r ** 2) / 2) * acos(sqrt(k) / r),
                    sqrt(k),
                    sqrt(t),
                )[0]
                cdf_value = 2 / (pi * p_deg_inc) * integral
            elif m == 4:
                integral = quad(
                    lambda r: r ** 2
                    * exp(-(r ** 2) / 2)
                    * (
                        atan(sqrt(r ** 2 / k * (r ** 2 / k - 2)))
                        - sqrt(k) / r * atan(sqrt(r ** 2 / k - 2))
                        - sqrt(k) / r * acos((r ** 2 / k - 1) ** (-1 / 2))
                    ),
                    sqrt(2 * k),
                    sqrt(t),
                )[0]
                cdf_value = (2 / pi) ** (3 / 2) * (p_deg_inc) ** (-2) * integral
    else:
        raise ValueError("devel argument must be either `internal` or `external`")
    if cdf_value < -0.001 or cdf_value > 1.001:
        raise ValueError("cdf_value outside [0,1].")
    elif -0.001 <= cdf_value < 0:
        return 0
    elif 1 < cdf_value <= 1.001:
        return 1
    else:
        return cdf_value


def find_relative_to_bisector(x, y, region):
    assert len(x) == len(y), "x and y must have the same length"
    if region == "over":
        reg = [yi > xi for yi, xi in zip(y, x)]
    elif region == "under":
        reg = [yi < xi for yi, xi in zip(y, x)]
    else:
        raise ValueError("region_type must either be `over` or `under`")
    idxreg = [i for i, o in enumerate(reg) if o]
    if len(idxreg) == 0:
        return "NEVER"
    segments = ""
    for k, g in groupby(enumerate(idxreg), lambda ix: ix[0] - ix[1]):
        seg = list(map(itemgetter(1), g))
        if x[seg[0]] != x[seg[-1]]:
            segments += str(x[seg[0]]) + "-" + str(x[seg[-1]]) + ", "
        else:
            segments += str(x[seg[0]]) + ", "
    segments = segments[:-2]
    return segments


if __name__ == "__main__":
    import time

    algorithm_args = [
        "-x",
        "probGiViTI_2017_Complessiva",
        "-y",
        "hospOutcomeLatest_RIC10",
        "-devel",
        "internal",
        "-max_deg",
        "4",
        "-confLevels",
        "0.80, 0.95",
        "-thres",
        "0.95",
        "-num_points",
        "200",
        "-pathology",
        "dementia",
        "-dataset",
        "cb_data",
        "-filter",
        "",
        "-formula",
        "",
    ]
    runner = create_runner(
        algorithm_class=CalibrationBelt, num_workers=1, algorithm_args=algorithm_args,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
