from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json
import scipy.stats as st

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData, set_algorithms_output_data, ExaremeError, P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR
from log_regr_lib import LogRegrFinal_Loc2Glob_TD


def logregr_global_final(global_state, global_in):
    # Unpack global state
    n_obs = global_state['n_obs']
    n_cols = global_state['n_cols']
    coeff = global_state['coeff']
    y_name = global_state['y_name']
    x_names = global_state['x_names']
    # y_val_dict = global_state['y_val_dict']
    # Unpack global input
    ll, grad, hess, y_sum, y_sqsum, ssres, posneg, FP_rate_frac, TP_rate_frac = global_in.get_data()

    # Output summary
    # stderr
    stderr = np.sqrt(
            np.diag(
                    np.linalg.inv(hess)
            )
    )
    # z scores
    z_scores = np.divide(coeff, stderr)
    # p-values
    z_to_p = lambda z: st.norm.sf(abs(z)) * 2
    p_values = z_to_p(z_scores)
    # Confidence intervals for 95%
    lci = np.array(
            [st.norm.ppf(0.025, loc=coeff[i], scale=stderr[i]) for i in range(len(coeff))]
    )
    rci = np.array(
            [st.norm.ppf(0.975, loc=coeff[i], scale=stderr[i]) for i in range(len(coeff))]
    )
    # Degrees of freedom
    df_mod = n_cols - 1
    df_resid = n_obs - df_mod - 1
    # Null model log-likelihood
    y_mean = y_sum / n_obs
    ll0 = y_sum * np.log(y_mean) + (n_obs - y_sum) * np.log(1.0 - y_mean)
    # AIC
    aic = 2 * n_cols - 2 * ll
    # BIC
    bic = np.log(n_obs) * n_cols - 2 * ll
    # R^2 etc.
    sstot = n_obs * y_mean * (1 - y_mean)  # Using binomial variable variance formula
    r2 = 1 - ssres / sstot
    r2_adj = 1 - (1 - r2) * (n_obs - 1) / (n_obs - n_cols - 1)
    r2_mcf = 1 - ll / ll0
    r2_cs = 1 - np.exp(-ll0 * 2 * r2_mcf / n_obs)
    # Confusion matrix etc.
    TP, TN, FP, FN = posneg['TP'], posneg['TN'], posneg['FP'], posneg['FN']
    confusion_mat = [[TP, FP], [FN, TN]]
    accuracy = (TP + TN) / n_obs
    precision = TP / (TP + FP)
    recall = TP / (TP + FN)
    F1 = 2 * (precision * recall) / (precision + recall)
    # ROC curve
    FP_rate = [fpr[0] / fpr[1] for fpr in FP_rate_frac]
    TP_rate = [tpr[0] / tpr[1] for tpr in TP_rate_frac]
    AUC = 0.0
    for t in range(1, len(FP_rate)):
        AUC += 0.5 * (FP_rate[t] - FP_rate[t - 1]) * (TP_rate[t] + TP_rate[t - 1])
    gini = 2 * AUC - 1
    # F-statistic
    F_stat = ((sstot - ssres) / n_cols) / (ssres / (n_obs - n_cols - 1))

    # Format output data
    # JSON raw
    raw_data = {
        'Covariates'                 : [
            {
                'Variable'   : x_names[i],
                'Coefficient': coeff[i],
                'std.err.'   : stderr[i],
                'z score'    : z_scores[i],
                'p value'    : p_values[i] if p_values[i] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
                'Lower C.I.' : lci[i],
                'Upper C.I.' : rci[i]
            }
            for i in range(len(x_names))
        ],
        'Model degrees of freedom'   : df_mod,
        'Residual degrees of freedom': df_resid,
        'Log-likelihood'             : ll,
        'Null model log-likelihood'  : ll0,
        'AIC'                        : aic,
        'BIC'                        : bic,
        'R^2'                        : r2,
        'Adjusted R^2'               : r2_adj,
        'McFadden pseudo-R^2'        : r2_mcf,
        'Cox-Snell pseudo-R^2'       : r2_cs,
        'Confusion matrix'           : confusion_mat,
        'Accuracy'                   : accuracy,
        'Precision'                  : precision,
        'Recall'                     : recall,
        'F1 score'                   : F1,
        'AUC'                        : AUC,
        'Gini coefficient'           : gini,
        'F statistic'                : F_stat
    }
    # Tabular summary 1
    tabular_data_summary1 = [["variable", "coefficient", "std.err.", "z-score", "p-value", "lower c.i.", "upper c.i."]]
    for i in range(len(x_names)):
        tabular_data_summary1.append([
            x_names[i],
            coeff[i],
            stderr[i],
            z_scores[i],
            str(p_values[i]) if p_values[i] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
            lci[i],
            rci[i]
        ])
    tabular_data_summary1_schema_fields = [
        {
            "name": "variable",
            "type": "string"
        }, {
            "name": "coefficient",
            "type": "number"
        }, {
            "name": "std.err.",
            "type": "number"
        }, {
            "name": "z-score",
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
    # Tabular summary 2
    tabular_data_summary2 = [["model degrees of freedom", "residual degrees of freedom",
                              "log-likelihood", "null model log-likelihood", "AIC", "BIC",
                              "R^2", "adjusted R^2", "McFadden pseudo-R^2", "Cox-Snell pseudo-R^2", "F statistic"]]
    tabular_data_summary2.append([df_mod, df_resid, ll, ll0, aic, bic, r2, r2_adj, r2_mcf, r2_cs, F_stat])
    tabular_data_summary2_schema_fields = [{"name": name, "type": "number"} for name in tabular_data_summary2[0]]
    # Highchart ROC
    highchart_roc = {
        "chart"  : {
            "type"    : "area",
            "zoomType": "xy"
        },
        "title"  : {
            "text": "ROC"
        },
        "xAxis"  : {
            "min"  : -0.05,
            "max"  : 1.05,
            "title": {
                "text": "False Positive Rate"
            }
        },
        "yAxis"  : {
            "title": {
                "text": "True Positive Rate"
            }
        },
        "legend" : {
            "enabled": False
        },
        "series" : [{
            "useHTML": True,
            "name"   : "AUC " + str(AUC) + "<br/>Gini Coefficient " + str(gini),
            "label"  : {
                "onArea": True
            },
            "data"   : list(zip(FP_rate, TP_rate))
        }],
        'tooltip': {
            'enabled'     : True,
            'headerFormat': '',
            'pointFormat' : '{point.x}, {point.y}'
        }
    }
    # Highchart confusion matrix
    # highchart_conf_matr = {
    #
    #     "chart"    : {
    #         "type": "heatmap",
    #
    #     },
    #     "title"    : {
    #         "useHTML": True,
    #         "text"   : "Confusion Matrix<br/><center><font size='2'>Binary categories:<br/>" +
    #                    " ".join([key + ' :' + str(y_val_dict[key]) for key in list(y_val_dict.keys())]) +
    #                    "</font></center>"
    #     },
    #     "xAxis"    : {
    #         "categories": ["Condition Positives", "Condition Negatives"]
    #     },
    #     "yAxis"    : {
    #         "categories": ["Prediction Negatives", "Prediction Positives"],
    #         "title"     : "null"
    #     },
    #     "colorAxis": {
    #         "min"     : 0,
    #         "minColor": "#FFFFFF",
    #         "maxColor": "#6699ff"
    #     },
    #     "legend"   : {
    #         "enabled": False,
    #     },
    #     "tooltip"  : {
    #         "enabled": False
    #     },
    #     "series"   : [{
    #         "dataLabels" : [{
    #             "format" : '{point.name}: {point.value}',
    #             "enabled": True,
    #             "color"  : '#333333'
    #         }],
    #         "name"       : 'Confusion Matrix',
    #         "borderWidth": 1,
    #         "data"       : [{
    #             "name" : 'True Positives',
    #             "x"    : 0,
    #             "y"    : 1,
    #             "value": TP
    #         }, {
    #             "name" : 'False Positives',
    #             "x"    : 1,
    #             "y"    : 1,
    #             "value": FP
    #         }, {
    #             "name" : 'False Negatives',
    #             "x"    : 0,
    #             "y"    : 0,
    #             "value": FN
    #         }, {
    #             "name" : 'True Negatives',
    #             "x"    : 1,
    #             "y"    : 0,
    #             "value": TN
    #         }]
    #     }]
    # }

    # Write output to JSON
    result = {
        'result': [
            # Raw results
            {
                "type": "application/json",
                "data": raw_data
            },
            # Tabular data resource Logistic Regression summary 1
            {
                "type": "application/vnd.dataresource+json",
                "data":
                    {
                        "name"   : "logistic regression summary 1",
                        "profile": "tabular-data-resource",
                        "data"   : tabular_data_summary1,
                        "schema" : {
                            "fields": tabular_data_summary1_schema_fields
                        }
                    }
            },
            # Tabular data resource summary 2
            {
                "type": "application/vnd.dataresource+json",
                "data":
                    {
                        "name"   : "logistic regression summary 2",
                        "profile": "tabular-data-resource",
                        "data"   : tabular_data_summary2[1:],
                        "schema" : {
                            "fields": tabular_data_summary2_schema_fields
                        }
                    }
            },
            # Highchart ROC
            {
                "type": "application/vnd.highcharts+json",
                "data": highchart_roc
            },
            # Highchart confusion matrix
            {
                "type": "application/vnd.highcharts+json",
                # "data": highchart_conf_matr
                "data": None
            }
        ]
    }
    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        raise ValueError('Result contains NaNs.')
    return global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    parser.add_argument('-local_step_dbs', required=True,
                        help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load global state
    global_state = StateData.load(fname_prev_state).data
    # Load local nodes output
    local_out = LogRegrFinal_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_out = logregr_global_final(global_state=global_state, global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
