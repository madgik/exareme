from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from collections import namedtuple
from math import sqrt, exp, pi, asin, acos, atan

import numpy as np
import scipy.stats as st
from scipy.integrate import quad
from scipy.stats import chi2
from scipy.special import expit, logit, xlogy
from mipframework import Algorithm, AlgorithmResult, UserError
from mipframework import TabularDataResource
from mipframework import create_runner
from mipframework.highcharts import ConfusionMatrix, ROC
from mipframework.constants import (
    P_VALUE_CUTOFF,
    P_VALUE_CUTOFF_STR,
    PREC,
    MAX_ITER,
    CONFIDENCE,
)


class CalibrationBelt(Algorithm):
    def __init__(self, cli_args):
        super(CalibrationBelt, self).__init__(__file__, cli_args, intercept=False)

    def local_init(self):
        observed = np.array(self.data.variables).flatten()
        expected = np.array(self.data.covariables).flatten()
        max_deg = int(self.parameters.max_deg)
        if not 1 < max_deg <= 4:  # todo proper condition
            raise UserError(
                "Max deg should be between 2 and 4 for `devel`=`external` "
                "or between 3 and 4 for `devel`=`internal`."
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

        lls = np.ones((max_deg,), dtype=np.float) * (-2 * n_obs * np.log(2))
        coeffs = np.zeros((max_deg, max_deg + 1))
        masks = np.zeros(coeffs.shape, dtype=bool)
        for idx in range(0, max_deg - 1):
            masks[idx, idx + 2 :] = True
        coeffs = np.ma.masked_array(coeffs, mask=masks)

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

        # Compute 0th, 1st and 2nd derivatives of loglikelihood
        hessians = np.ma.empty((4, coeffs.shape[1], coeffs.shape[1]), dtype=np.float)
        grads = np.ma.empty((4, coeffs.shape[1]), dtype=np.float)
        lls = np.empty((4,), dtype=np.float)
        for idx in range(max_deg):
            X = Xs[idx]
            coeff = coeffs[idx]
            # Auxiliary quantities
            z = np.ma.dot(X, coeff)
            s = expit(z)
            d = np.multiply(s, (1 - s))
            D = np.diag(d)
            # Hessian
            hess = np.ma.dot(np.transpose(X), np.ma.dot(D, X))
            hessians[idx] = hess
            # Gradient
            Ymsd = (Y - s) / d  # Stable computation of (Y - s) / d
            Ymsd[(Y == 0) & (s == 0)] = -1
            Ymsd[(Y == 1) & (s == 1)] = 1

            grad = np.ma.dot(np.transpose(X), np.ma.dot(D, z + Ymsd))
            grads[idx] = grad

            # Log-likelihood
            ll = np.sum(xlogy(Y, s) + xlogy(1 - Y, 1 - s))
            lls[idx] = ll

        self.push_and_add(lls=lls)
        self.push_and_add(grads=grads)
        self.push_and_add(hessians=hessians)

    def global_step(self):
        max_deg = int(self.parameters.max_deg)
        coeffs = self.load("coeffs")
        lls_old = self.load("lls")
        iter_ = self.load("iter_")
        grads = self.fetch("grads")
        lls = self.fetch("lls")
        hessians = self.fetch("hessians")

        deltas = np.empty(lls.shape)

        for idx in range(max_deg):
            hess = hessians[idx]
            covariance = np.ma.zeros(hess.shape)
            covariance.mask = hess.mask
            covariance[: idx + 2, : idx + 2] = np.linalg.inv(hess[: idx + 2, : idx + 2])
            coeffs[idx] = np.ma.dot(covariance, grads[idx])

            # Update termination quantities
            deltas[idx] = abs(lls[idx] - lls_old[idx])

        if all([delta < PREC for delta in deltas]) or iter_ >= MAX_ITER:
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
        coeffs = self.fetch("coeffs")

        # Compute partial log-likelihood on bisector,
        # i.e. coeff = [0, 1] (needed for p-value calculation)
        X = Xs[0]
        coeff = coeffs[0]
        coeff[:2] = np.array([0, 1])
        # Auxiliary quantities
        z = np.dot(X, coeff)
        s = expit(z)
        # Log-likelihood
        ls1, ls2 = np.log(s), np.log(1 - s)
        log_lik_bisector = np.dot(Y, ls1) + np.dot(1 - Y, ls2)

        self.push_and_add(log_lik_bisector=log_lik_bisector)

    def global_final(self):
        devel = self.parameters.devel
        thres = float(self.parameters.thres)
        max_deg = int(self.parameters.max_deg)
        num_points = int(self.parameters.num_points)

        lls = self.load("lls")
        hessians = self.load("hessians")
        coeffs = self.load("coeffs")
        log_lik_bisector = self.fetch("log_lik_bisector")

        # Perform likelihood-ratio test
        if devel == "external":
            idx = 0
        elif devel == "internal":
            idx = 1
        else:
            raise ValueError("devel should be `internal` or `external`")

        crit = chi2.ppf(q=thres, df=1)
        for i in range(idx, max_deg):
            ddev = 2 * (lls[i] - lls[i - 1])
            if ddev > crit:
                idx = i
            else:
                break

        model_deg = idx + 1

        # Get selected model coefficients, log-likelihood, grad, Hessian and covariance
        hess = hessians[idx]
        ll = lls[idx]
        coeff = coeffs[idx]
        coeff = coeff[~coeff.mask]
        covariance = np.linalg.inv(hess[: idx + 2, : idx + 2])

        # Compute p value
        calibrationStat = 2 * (ll - log_lik_bisector)
        p_value = 1 - giviti_stat_cdf(
            calibrationStat, m=model_deg, devel=devel, thres=thres
        )

        # Compute calibration curve
        e_min, e_max = 0.01, 0.99  # todo
        e_lin = np.linspace(e_min, e_max, num=(int(num_points) + 1) // 2)
        e_log = expit(np.linspace(logit(e_min), logit(e_max), num=int(num_points) // 2))
        e = np.concatenate((e_lin, e_log))
        e = np.sort(e)
        ge = logit(e)
        G = np.array([np.power(ge, i) for i in range(len(coeff))]).T
        p = expit(np.dot(G, coeff))
        calib_curve = np.array([e, p]).transpose()

        # Compute confidence intervals
        cl1, cl2 = 0.8, 0.95  # todo
        GVG = np.stack([np.dot(G[i], np.dot(covariance, G[i])) for i in range(len(G))])
        sqrt_chi_GVG_1 = np.sqrt(np.multiply(chi2.ppf(q=cl1, df=2), GVG))
        sqrt_chi_GVG_2 = np.sqrt(np.multiply(chi2.ppf(q=cl2, df=2), GVG))
        g_min1, g_max1 = (
            np.dot(G, coeff) - sqrt_chi_GVG_1,
            np.dot(G, coeff) + sqrt_chi_GVG_1,
        )
        g_min2, g_max2 = (
            np.dot(G, coeff) - sqrt_chi_GVG_2,
            np.dot(G, coeff) + sqrt_chi_GVG_2,
        )
        p_min1, p_max1 = expit(g_min1), expit(g_max1)
        p_min2, p_max2 = expit(g_min2), expit(g_max2)
        calib_belt1 = np.array([p_min1, p_max1])
        calib_belt2 = np.array([p_min2, p_max2])
        calib_belt1_hc = np.array([e, p_min1, p_max1]).transpose()
        calib_belt2_hc = np.array([e, p_min2, p_max2]).transpose()
        pass


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


if __name__ == "__main__":
    import time

    algorithm_args = [
        "-x",
        "probGiViTI_2017_Complessiva",
        "-y",
        "hospOutcomeLatest_RIC10",
        "-devel",
        "external",
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
        for_class="CalibrationBelt",
        found_in="CALIBRATION_BELT/calibration_belt",
        alg_type="iterative",
        num_workers=1,
        algorithm_args=algorithm_args,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
