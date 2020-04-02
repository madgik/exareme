from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import numpy as np
import scipy.special as special
import scipy.stats as st

from mipframework import Algorithm, AlgorithmResult, TabularDataResource, AlgorithmError
from mipframework.constants import P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR
from mipframework.highcharts import CorrelationHeatmap


class Pearson(Algorithm):
    def __init__(self, cli_args):
        super(Pearson, self).__init__(__file__, cli_args, intercept=False)

    def local_(self):
        left_vars = right_vars = self.data.variables
        if self.data.covariables is not None:
            right_vars = self.data.covariables

        n_obs = len(right_vars)
        cm_shape = len(left_vars.columns), len(right_vars.columns)

        # Compute statistics
        sx = np.empty(cm_shape, dtype=np.float)
        sy = np.empty(cm_shape, dtype=np.float)
        sxx = np.empty(cm_shape, dtype=np.float)
        sxy = np.empty(cm_shape, dtype=np.float)
        # sxy = (X * X.T[:, :, None]).sum(axis=1)
        syy = np.empty(cm_shape, dtype=np.float)
        cm_names = np.array([[''] * cm_shape[1]] * cm_shape[0], dtype=object)
        for i in xrange(cm_shape[0]):
            for j in xrange(cm_shape[1]):
                y, x = left_vars.iloc[:, i], right_vars.iloc[:, j]
                sx[i, j] = x.sum()
                sy[i, j] = y.sum()
                sxx[i, j] = (x * x).sum()
                sxy[i, j] = (x * y).sum()
                syy[i, j] = (y * y).sum()
                cm_names[i, j] = y.name + ' ~ ' + x.name

        self.push_and_add(n_obs=n_obs)
        self.push_and_add(sx=sx)
        self.push_and_add(sy=sy)
        self.push_and_add(sxx=sxx)
        self.push_and_add(sxy=sxy)
        self.push_and_add(syy=syy)
        self.push_and_agree(cm_names=cm_names)
        self.push_and_agree(lnames=list(left_vars.columns))
        self.push_and_agree(rnames=list(right_vars.columns))

    def global_(self):
        n_obs = self.fetch('n_obs')
        sx = self.fetch('sx')
        sy = self.fetch('sy')
        sxx = self.fetch('sxx')
        sxy = self.fetch('sxy')
        syy = self.fetch('syy')
        cm_names = self.fetch('cm_names')
        lnames = self.fetch('lnames')
        rnames = self.fetch('rnames')

        cm_shape = sx.shape
        r = np.zeros(cm_shape, dtype=np.float)
        prob = np.zeros(cm_shape, dtype=np.float)
        ci_lo = np.zeros(cm_shape, dtype=np.float)
        ci_hi = np.zeros(cm_shape, dtype=np.float)

        df = n_obs - 2
        for i in range(cm_shape[0]):
            for j in range(cm_shape[1]):
                d = (np.sqrt(n_obs * sxx[i, j] - sx[i, j] * sx[i, j])
                     * np.sqrt(n_obs * syy[i, j] - sy[i, j] * sy[i, j]))
                if d == 0:
                    r[i, j] = 0
                else:
                    r[i, j] = float((n_obs * sxy[i, j] - sx[i, j] * sy[i, j]) / d)
                # If abs(r) > 1 artifact of floating point arithmetic.
                r[i, j] = max(min(r[i, j], 1.0), -1.0)
                if abs(r[i, j]) == 1.0:
                    prob[i, j] = 0.0
                else:
                    t_squared = r[i, j] ** 2 * (df / ((1.0 - r[i, j]) * (1.0 + r[i, j])))
                    prob[i, j] = special.betainc(
                        0.5 * df, 0.5, np.fmin(np.asarray(df / (df + t_squared)), 1.0)
                    )
                # Compute 95% confidence intervals
                alpha = 0.05  # Two-tail test with confidence intervals 95%
                if r[i, j] is not None:
                    r_z = np.arctanh(r[i, j])
                    se = 1 / np.sqrt(n_obs - 3)
                    z = st.norm.ppf(1 - alpha / 2)
                    lo_z, hi_z = r_z - z * se, r_z + z * se
                    ci_lo[i, j], ci_hi[i, j] = np.tanh((lo_z, hi_z))
                else:
                    raise AlgorithmError('Pearson coefficient is NaN.')

        # Format results for output
        raw_data = {
            'n_obs'                          : n_obs,
            'Correlation matrix labels'      : cm_names.tolist(),
            'x axis names'                   : rnames,
            'y axis names'                   : lnames,
            'Pearson correlation coefficient': r.tolist(),
            'p-value'                        : prob.tolist(),
            'C.I. Lower'                     : ci_lo.tolist(),
            'C.I. Upper'                     : ci_hi.tolist()
        }
        correl_tabular = []
        for i in range(cm_shape[0]):
            for j in range(cm_shape[1]):
                correl_tabular.append([
                    cm_names[i, j],
                    r[i, j],
                    (str(prob[i, j])
                     if prob[i, j] >= P_VALUE_CUTOFF
                     else P_VALUE_CUTOFF_STR),
                    ci_lo[i, j],
                    ci_hi[i, j]
                ])
        table_output = TabularDataResource(
            fields=[
                'variables',
                'Pearson correlation coefficient',
                'p-value',
                'lower c.i.',
                'upper c.i.'
            ],
            data=correl_tabular,
            title='Pearson Correlation Summary')

        highchart = CorrelationHeatmap(title='Pearson Correlation Heatmap', matrix=r,
                                       min_val=-1, max_val=1)

        self.result = AlgorithmResult(
            raw_data=raw_data,
            tables=[table_output],
            highcharts=[highchart]
        )


if __name__ == '__main__':
    import time
    from mipframework import create_runner

    algorithm_args = [
        '-x', 'rightioginferioroccipitalgyrus,rightmfcmedialfrontalcortex',
        '-y', 'subjectage,rightventraldc,rightaccumbensarea',
        '-pathology', 'dementia, leftaccumbensarea',
        '-dataset', 'adni',
        '-filter', '',
        '-formula', '',
        '-coding', '',
    ]
    runner = create_runner(for_class='Pearson', found_in='PEARSON_CORRELATION/pearson',
                           alg_type='local-global', algorithm_args=algorithm_args,
                           num_workers=3)
    start = time.time()
    runner.run()
    end = time.time()
    print('Completed in ', end - start)
