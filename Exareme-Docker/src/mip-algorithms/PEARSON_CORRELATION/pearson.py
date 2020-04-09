from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import numpy as np
import scipy.special as special
import scipy.stats as st

from mipframework import Algorithm, AlgorithmResult, TabularDataResource, UserError
from mipframework.constants import P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR, CONFIDENCE
from mipframework.highcharts import CorrelationHeatmap


class Pearson(Algorithm):
    def __init__(self, cli_args):
        super(Pearson, self).__init__(__file__, cli_args, intercept=False)

    def local_(self):
        n_obs = self.data.variables.shape[0]
        Y = X = self.data.variables.to_numpy()
        y_names = x_names = self.data.variables.columns.to_numpy()
        if self.data.covariables is not None:
            X = self.data.covariables.to_numpy()
            x_names = self.data.covariables.columns.to_numpy()

        if any(len(set(column)) == 1 for column in X.T) or any(
            len(set(column)) == 1 for column in Y.T
        ):
            raise UserError(
                "Data contains a constant row and the Pearson correlation coefficient "
                "is not defined in this case."
            )

        sx, sxx, sxy, sy, syy = get_local_sums(X, Y)
        pair_names = get_var_pair_names(x_names, y_names)

        self.push_and_add(n_obs=n_obs)
        self.push_and_add(sx=sx)
        self.push_and_add(sy=sy)
        self.push_and_add(sxx=sxx)
        self.push_and_add(sxy=sxy)
        self.push_and_add(syy=syy)
        self.push_and_agree(pair_names=pair_names)

    def global_(self):
        n_obs = self.fetch("n_obs")
        sx = self.fetch("sx")
        sy = self.fetch("sy")
        sxx = self.fetch("sxx")
        sxy = self.fetch("sxy")
        syy = self.fetch("syy")
        pair_names = self.fetch("pair_names")

        correlation, p_value = get_correlation_and_pvalue(n_obs, sx, sxx, sxy, sy, syy)
        ci_hi, ci_lo = get_confidence_intervals(n_obs, correlation)

        p_value_str = [
            str(p) if p >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR
            for prow in p_value
            for p in prow
        ]

        # Format results for output
        raw_data = {
            "n_obs": n_obs,
            "Correlation matrix labels": pair_names.tolist(),
            "Pearson correlation coefficient": correlation.tolist(),
            "p-value": p_value.tolist(),
            "C.I. Lower": ci_lo.tolist(),
            "C.I. Upper": ci_hi.tolist(),
        }
        correlation_table_data = [
            row
            for row in zip(
                pair_names.ravel(),
                correlation.ravel(),
                p_value_str,
                ci_lo.ravel(),
                ci_hi.ravel(),
            )
        ]
        table_output = TabularDataResource(
            fields=[
                "Variables",
                "Pearson correlation coefficient",
                "p-value",
                "Lower c.i.",
                "Upper c.i.",
            ],
            data=correlation_table_data,
            title="Pearson Correlation Summary",
        )

        highchart = CorrelationHeatmap(
            title="Pearson Correlation Heatmap",
            matrix=correlation,
            min_val=-1,
            max_val=1,
        )

        self.result = AlgorithmResult(
            raw_data=raw_data, tables=[table_output], highcharts=[highchart]
        )


def get_local_sums(X, Y):
    sx = X.sum(axis=0)
    sy = Y.sum(axis=0)
    sxx = (X ** 2).sum(axis=0)
    sxy = (X * Y.T[:, :, np.newaxis]).sum(axis=1)
    syy = (Y ** 2).sum(axis=0)
    return sx, sxx, sxy, sy, syy


def get_var_pair_names(x_names, y_names):
    tildas = np.empty((len(y_names), len(x_names)), dtype=object)
    tildas[:] = " ~ "
    pair_names = y_names[:, np.newaxis] + tildas + x_names
    return pair_names


def get_correlation_and_pvalue(n_obs, sx, sxx, sxy, sy, syy):
    df = n_obs - 2
    d = np.sqrt(n_obs * sxx - sx * sx) * np.sqrt(n_obs * syy - sy * sy)[:, np.newaxis]
    r = (n_obs * sxy - sx * sy[:, np.newaxis]) / d
    r[d == 0] = 0
    r = r.clip(-1, 1)
    t_squared = r ** 2 * (df / ((1.0 - r) * (1.0 + r)))
    prob = special.betainc(
        0.5 * df, 0.5, np.fmin(np.asarray(df / (df + t_squared)), 1.0)
    )
    prob[abs(r) == 1] = 0
    return r, prob


def get_confidence_intervals(n_obs, r):
    r_z = np.arctanh(r)
    se = 1 / np.sqrt(n_obs - 3)
    alpha = 1 - CONFIDENCE
    z = st.norm.ppf(1 - alpha / 2)
    lo_z, hi_z = r_z - z * se, r_z + z * se
    ci_lo, ci_hi = np.tanh((lo_z, hi_z))
    return ci_hi, ci_lo


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-x",
        "rightioginferioroccipitalgyrus,rightmfcmedialfrontalcortex",
        "-y",
        "subjectage,rightventraldc,rightaccumbensarea",
        "-pathology",
        "dementia, leftaccumbensarea",
        "-dataset",
        "adni",
        "-filter",
        "",
        "-formula",
        "",
        "-coding",
        "",
    ]
    runner = create_runner(
        for_class="Pearson",
        found_in="PEARSON_CORRELATION/pearson",
        alg_type="local-global",
        algorithm_args=algorithm_args,
        num_workers=3,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
