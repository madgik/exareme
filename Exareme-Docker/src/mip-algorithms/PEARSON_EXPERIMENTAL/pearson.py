import math

import numpy as np
import scipy.special as special
import scipy.stats as st

from PEARSON_EXPERIMENTAL.mip_framework.algorithm_base import Algorithm
from PEARSON_EXPERIMENTAL.mip_framework.exceptions import AlgorithmError

P_VALUE_CUTOFF = 0.001
P_VALUE_CUTOFF_STR = '< ' + str(P_VALUE_CUTOFF)


class Pearson(Algorithm):
    def __init__(self, cli_args):
        super(Pearson, self).__init__(__file__, cli_args)

    def local_(self):
        left_vars = self.data.variables
        right_vars = self.data.covariables

        n_obs = len(right_vars)
        cm_shape = len(left_vars.columns), len(right_vars.columns)

        # Compute statistics
        sx = np.empty(cm_shape, dtype=np.float)
        sy = np.empty(cm_shape, dtype=np.float)
        sxx = np.empty(cm_shape, dtype=np.float)
        sxy = np.empty(cm_shape, dtype=np.float)
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
        self.push_and_aggree(cm_names=cm_names)
        self.push_and_aggree(lnames=list(left_vars.columns))
        self.push_and_aggree(rnames=list(right_vars.columns))

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
        for i in xrange(cm_shape[0]):
            for j in xrange(cm_shape[1]):
                d = (math.sqrt(n_obs * sxx[i, j] - sx[i, j] * sx[i, j])
                     * math.sqrt(n_obs * syy[i, j] - sy[i, j] * sy[i, j]))
                if d == 0:
                    r[i, j] = 0
                else:
                    r[i, j] = float((n_obs * sxy[i, j] - sx[i, j] * sy[i, j]) / d)
                r[i, j] = max(min(r[i, j], 1.0),
                              -1.0)  # If abs(r) > 1 correct it: artifact of floating point arithmetic.
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

        # Format output data
        # JSON raw
        result_list = []
        for i in xrange(cm_shape[0]):
            for j in xrange(cm_shape[1]):
                result_list.append({
                    'Variables'                      : cm_names[i, j],
                    'Pearson correlation coefficient': r[i, j],
                    'p-value'                        : prob[i, j] if prob[
                                                                         i, j] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
                    'C.I. Lower'                     : ci_lo[i, j],
                    'C.I. Upper'                     : ci_hi[i, j]
                })
        # Tabular summary
        tabular_data_summary = [["variables", "Pearson correlation coefficient", "p-value", "lower c.i.", "upper c.i."]]
        for i in xrange(cm_shape[0]):
            for j in xrange(cm_shape[1]):
                tabular_data_summary.append([
                    cm_names[i, j],
                    r[i, j],
                    prob[i, j] if prob[i, j] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
                    ci_lo[i, j],
                    ci_hi[i, j]
                ])
        tabular_data_summary_schema_fields = [
            {
                "name": "variables",
                "type": "string"
            }, {
                "name": "Pearson correlation coefficient",
                "type": "number"
            }, {
                "name": "p-value",
                "type": "string"
            }, {
                "name": "lower c.i.",
                "type": "number"
            }, {
                "name": "upper c.i.",
                "type": "number"
            },
        ]
        # Highchart Correlation Matrix
        correlmatr_data = []
        for i in xrange(cm_shape[0]):
            for j in xrange(cm_shape[1]):
                correlmatr_data.append({
                    'x'    : j,
                    'y'    : i,
                    'value': round(r[i, j], 4),
                    'name' : cm_names[i, j]
                })

        hichart_correl_matr = {
            'chart'    : {
                'type'           : 'heatmap',
                'plotBorderWidth': 1
            },
            'title'    : {
                'text': 'Pearson Correlation Matrix'
            },
            'xAxis'    : {
                'categories': rnames
            },
            'yAxis'    : {
                'categories': lnames,
                'title'     : 'null'
            },
            'colorAxis': {
                'stops'   : [
                    [0, '#c4463a'],
                    [0.5, '#ffffff'],
                    [0.9, '#3060cf']
                ],
                'min'     : -1,
                'max'     : 1,
                'minColor': '#FFFFFF',
                'maxColor': "#6699ff"
            },
            'legend'   : {
                'align'        : 'right',
                'layout'       : 'vertical',
                'margin'       : 0,
                'verticalAlign': 'top',
                'y'            : 25,
                'symbolHeight' : 280
            },
            'tooltip'  : {
                'headerFormat': '',
                'pointFormat' : '<b>{point.name}: {point.value}</b>',
                'enabled'     : True
            },
            'series'   : [{
                'name'       : 'coefficients',
                'borderWidth': 1,
                'data'       : correlmatr_data,
                'dataLabels' : {
                    'enabled': True,
                    'color'  : '#000000'
                }
            }]
        }
        # Write output to JSON
        self.result = {
            'result': [
                # Raw results
                {
                    "type": "application/json",
                    "data": result_list
                },
                # Tabular data resource summary
                {
                    "type": "application/vnd.dataresource+json",
                    "data":
                        {
                            "name"   : "Pearson correlation summary",
                            "profile": "tabular-data-resource",
                            "data"   : tabular_data_summary[1:],
                            "schema" : {
                                "fields": tabular_data_summary_schema_fields
                            }
                        }

                },
                # Highchart correlation matrix
                {
                    "type": "application/vnd.highcharts+json",
                    "data": hichart_correl_matr
                }
            ]
        }
