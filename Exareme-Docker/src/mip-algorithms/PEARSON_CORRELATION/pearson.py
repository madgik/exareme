from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import numpy as np
import scipy.special as special
import scipy.stats as st

from mipframework import Algorithm, AlgorithmResult, TabularDataResource
from mipframework.constants import P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR, CONFIDENCE
from mipframework.highcharts import CorrelationHeatmap


class Pearson(Algorithm):
    def __init__(self, cli_args):
        super(Pearson, self).__init__(__file__, cli_args, intercept=False)

    def local_(self):
        n_obs = self.data.variables.shape[0]
        Y = X = self.data.variables.to_numpy()
        y_names = x_names = self.data.variables.columns.to_numpy()[:, np.newaxis]
        if self.data.covariables is not None:
            X = self.data.covariables.to_numpy()
            x_names = self.data.covariables.columns.to_numpy()

        sx = X.sum(axis=0)
        sy = Y.sum(axis=0)
        sxx = (X ** 2).sum(axis=0)
        sxy = (X * Y.T[:, :, np.newaxis]).sum(axis=1)
        syy = (Y ** 2).sum(axis=0)
        tildas = np.empty(sxy.shape, dtype=object)
        tildas[:] = " ~ "
        pair_names = y_names + tildas + x_names

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

        df = n_obs - 2
        d = (
            np.sqrt(n_obs * sxx - sx * sx)
            * np.sqrt(n_obs * syy - sy * sy)[:, np.newaxis]
        )
        r = (n_obs * sxy - sx * sy[:, np.newaxis]) / d
        r[d == 0] = 0
        r = r.clip(-1, 1)
        t_squared = r ** 2 * (df / ((1.0 - r) * (1.0 + r)))
        prob = special.betainc(
            0.5 * df, 0.5, np.fmin(np.asarray(df / (df + t_squared)), 1.0)
        )
        prob[abs(r) == 1] = 0
        r_z = np.arctanh(r)
        se = 1 / np.sqrt(n_obs - 3)
        alpha = 1 - CONFIDENCE
        z = st.norm.ppf(1 - alpha / 2)
        lo_z, hi_z = r_z - z * se, r_z + z * se
        ci_lo, ci_hi = np.tanh((lo_z, hi_z))

        # Format results for output
        raw_data = {
            "n_obs": n_obs,
            "Correlation matrix labels": pair_names.tolist(),
            "Pearson correlation coefficient": r.tolist(),
            "p-value": prob.tolist(),
            "C.I. Lower": ci_lo.tolist(),
            "C.I. Upper": ci_hi.tolist(),
        }
        correl_tabular = []
        for i in range(sxy.shape[0]):
            for j in range(sxy.shape[1]):
                correl_tabular.append(
                    [
                        pair_names[i, j],
                        r[i, j],
                        (
                            str(prob[i, j])
                            if prob[i, j] >= P_VALUE_CUTOFF
                            else P_VALUE_CUTOFF_STR
                        ),
                        ci_lo[i, j],
                        ci_hi[i, j],
                    ]
                )
        table_output = TabularDataResource(
            fields=[
                "variables",
                "Pearson correlation coefficient",
                "p-value",
                "lower c.i.",
                "upper c.i.",
            ],
            data=correl_tabular,
            title="Pearson Correlation Summary",
        )

        highchart = CorrelationHeatmap(
            title="Pearson Correlation Heatmap", matrix=r, min_val=-1, max_val=1
        )

        self.result = AlgorithmResult(
            raw_data=raw_data, tables=[table_output], highcharts=[highchart]
        )


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
