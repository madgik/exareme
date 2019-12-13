from __future__ import division
from __future__ import print_function

import sys
import math
import json
from os import path
import numpy as np
import scipy.special as special
import scipy.stats as st
from argparse import ArgumentParser

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import set_algorithms_output_data, ExaremeError, P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR
from pearsonc_lib import PearsonCorrelationLocalDT


def pearsonr_global(global_in):
    """Global step in Pearson correlation coefficient. Local statistics, computed in local step, are aggregated and
    then Pearson correlation coefficient `r`, p-value `prob` and lower and upper confidence intervals at 95% `ci_lo`
    and `ci_hi` are computed. Pearson correlation is computed according to standard formula
    (see https://en.wikipedia.org/wiki/Pearson_correlation_coefficient#For_a_sample). The p-value is computed using
    the incomplete beta integral method. The lower and upper confidence intervals are computed usign the Fisher
    information (see https://en.wikipedia.org/wiki/Pearson_correlation_coefficient#Using_the_Fisher_transformation).

    Parameters
    ----------
    global_in : PearsonCorrelationLocalDT
        Object holding aggregated values of statistics computed in local step.

    Returns
    -------
    global_out : str
        JSON string containing a list of results, one for each variable pair, where each result hold the variable
        pair names, the Pearson coefficient, the p-value and the lower and upper confidence intervals.
    """
    n_obs, sx, sy, sxx, sxy, syy, cm_names, lnames, rnames = global_in.get_data()
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
            r[i, j] = max(min(r[i, j], 1.0), -1.0)  # If abs(r) > 1 correct it: artifact of floating point arithmetic.
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
                raise ExaremeError('Pearson coefficient is NaN.')

    # Format output data
    # JSON raw
    result_list = []
    for i in xrange(cm_shape[0]):
        for j in xrange(cm_shape[1]):
            result_list.append({
                'Variables'                      : cm_names[i, j],
                'Pearson correlation coefficient': r[i, j],
                'p-value'                        : prob[i, j] if prob[i, j] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
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
    result = {
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
    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        print('Result contains NaNs.')
    else:
        return global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = PearsonCorrelationLocalDT.load(local_dbs)
    # Run algorithm global step
    global_out = pearsonr_global(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
