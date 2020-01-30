from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import json
import math
import sys
from os import path

import numpy as np
import scipy.special as special
import scipy.stats as st

_new_path = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(_new_path)
while True:
    try:
        import utils.algorithm_utils
    except:
        sys.path.pop()
        _new_path = path.dirname(_new_path)
        sys.path.append(_new_path)
    else:
        break
del _new_path

from utils.algorithm_utils import query_from_formula, TransferData, ExaremeError, P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR


def get_data(args):
    args_y = list(
            args.y
                .replace(' ', '')
                .split(',')
    )
    if args.x == '':
        variables = (args_y,)
    else:
        args_x = list(
                args.x
                    .replace(' ', '')
                    .split(',')
        )
        variables = (args_y, args_x)

    dataset = args.dataset
    query_filter = args.filter
    # formula = args.formula
    # formula = formula.replace('_', '~')  # TODO Fix tilda problem and remove
    # no_intercept = json.loads(args.no_intercept)
    input_local_DB = args.input_local_DB
    data_table = args.data_table
    metadata_table = args.metadata_table
    metadata_code_column = args.metadata_code_column
    metadata_isCategorical_column = args.metadata_isCategorical_column
    # coding = None if args.coding == 'null' else args.coding
    left_vars, right_vars = query_from_formula(fname_db=input_local_DB, formula='', variables=variables,
                                               dataset=dataset,
                                               query_filter=query_filter,
                                               data_table=data_table, metadata_table=metadata_table,
                                               metadata_code_column=metadata_code_column,
                                               metadata_isCategorical_column=metadata_isCategorical_column,
                                               no_intercept=True, coding=None)

    if left_vars is None:
        left_vars = right_vars
    return left_vars, right_vars


def pearson_local(local_in):
    # TODO Rewrite docstring
    # Unpack data
    left_vars, right_vars = local_in

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

    local_out = PearsonCorrelationLocalDT(
            n_obs, sx, sy, sxx, sxy, syy, cm_names, list(left_vars.columns), list(right_vars.columns))

    return local_out


def pearson_global(global_in):
    # TODO Rewrite docstring
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


# Set the data class that will transfer the data between local-global
class PearsonCorrelationLocalDT(TransferData):
    def __init__(self, *args):
        self.n_obs = args[0]
        self.sx = args[1]
        self.sy = args[2]
        self.sxx = args[3]
        self.sxy = args[4]
        self.syy = args[5]
        self.cm_names = args[6]
        self.lnames = args[7]
        self.rnames = args[8]

    def get_data(self):
        return (
            self.n_obs, self.sx, self.sy,
            self.sxx, self.sxy, self.syy,
            self.cm_names, self.lnames, self.rnames
        )

    def __add__(self, other):
        result = PearsonCorrelationLocalDT(
                self.n_obs + other.n_obs,
                self.sx + other.sx,
                self.sy + other.sy,
                self.sxx + other.sxx,
                self.sxy + other.sxy,
                self.syy + other.syy,
                self.cm_names,
                self.lnames,
                self.rnames
        )
        return result
