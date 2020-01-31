from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import json

import numpy as np
import scipy.stats as st
from scipy.special import expit
from utils.algorithm_utils import TransferData, StateData, ExaremeError, query_from_formula, P_VALUE_CUTOFF, \
    P_VALUE_CUTOFF_STR, set_algorithms_output_data

PREC = 1e-7  # Precision used in termination_condition
_MAX_ITER = 40


def get_data(args):
    args_x = list(
            args.x
                .replace(' ', '')
                .split(',')
    )
    args_y = args.y.strip()
    variables = ([args_y], args_x)
    dataset = args.dataset
    query_filter = args.filter
    formula = args.formula
    formula = formula.replace('_', '~')  # TODO Fix tilda problem and remove
    cur_state_pkl = args.cur_state_pkl
    input_local_DB = args.input_local_DB
    data_table = args.data_table
    metadata_table = args.metadata_table
    metadata_code_column = args.metadata_code_column
    metadata_isCategorical_column = args.metadata_isCategorical_column
    # Get data from local DB
    Y, X = query_from_formula(fname_db=input_local_DB,
                              formula=formula,
                              variables=variables,
                              dataset=dataset,
                              query_filter=query_filter,
                              data_table=data_table,
                              metadata_table=metadata_table,
                              metadata_code_column=metadata_code_column,
                              metadata_isCategorical_column=metadata_isCategorical_column,
                              no_intercept=False,
                              coding=None)
    if len(Y.columns) > 2:
        raise ExaremeError('Data must contain only two categories. More where given.')
    elif len(Y.columns) == 1:
        raise ExaremeError('Data must contain two categories. Only one present.')
    local_in = Y, X
    return cur_state_pkl, local_in


def logreg_init_local(local_in):
    # Unpack local input
    Y, X = local_in
    n_obs = len(Y)
    n_cols = len(X.columns)
    Y = Y.iloc[:, 1]
    y_name = Y.name
    x_names = list(X.columns)
    Y, X = np.array(Y), np.array(X)

    # Pack state and results
    local_state = StateData(X=X, Y=Y)
    local_out = LogRegrInit_Loc2Glob_TD(n_obs, n_cols, y_name, x_names)
    return local_state, local_out


def logreg_init_global(global_in):
    n_obs, n_cols, y_name, x_names = global_in.get_data()

    # Init vars
    ll = - 2 * n_obs * np.log(2)
    coeff = np.zeros(n_cols)
    iter_ = 0

    # Pack state and results
    global_state = StateData(n_obs=n_obs, n_cols=n_cols, ll=ll, coeff=coeff, iter_=iter_,
                             y_name=y_name, x_names=x_names)
    global_out = LogRegrIter_Glob2Loc_TD(coeff)

    return global_state, global_out


def logreg_iter_local(local_state, local_in):
    # Unpack local state
    X, Y = local_state['X'], local_state['Y']
    # Unpack local input
    coeff = local_in.get_data()

    # Auxiliary quantities
    z = np.dot(X, coeff)
    s = expit(z)
    d = np.multiply(s, (1 - s))
    D = np.diag(d)
    # Hessian
    hess = np.dot(
            np.transpose(X),
            np.dot(D, X)
    )
    # Gradient
    grad = np.dot(
            np.transpose(X),
            np.dot(
                    D,
                    z + np.divide(Y - s, d)
            )
    )
    # Log-likelihood
    ls1, ls2 = np.log(s), np.log(1 - s)
    ll = np.dot(Y, ls1) + np.dot(1 - Y, ls2)

    # Pack state and results
    local_state = StateData(X=X, Y=Y)
    local_out = LogRegrIter_Loc2Glob_TD(ll, grad, hess)
    return local_state, local_out


def logreg_iter_global(global_state, global_in):
    # Unpack global state
    n_obs = global_state['n_obs']
    n_cols = global_state['n_cols']
    ll_old = global_state['ll']
    iter_ = global_state['iter_']
    y_name = global_state['y_name']
    x_names = global_state['x_names']
    # Unpack global input
    ll_new, grad, hess = global_in.get_data()

    # Compute new coefficients
    coeff = np.dot(
            np.linalg.inv(hess),
            grad
    )
    # Update termination quantities
    delta = abs(ll_new - ll_old)
    iter_ += 1

    # Pack state and results
    global_state = StateData(n_obs=n_obs, n_cols=n_cols, ll=ll_new, coeff=coeff, delta=delta,
                             iter_=iter_, y_name=y_name, x_names=x_names)
    global_out = LogRegrIter_Glob2Loc_TD(coeff)
    return global_state, global_out


def logreg_finalize_local(local_state, local_in):
    # Unpack local state
    X, Y = local_state['X'], local_state['Y']
    # Unpack local input
    coeff = local_in.get_data()

    # Auxiliary quantities
    z = np.dot(X, coeff)
    s = expit(z)
    d = np.multiply(s, (1 - s))
    D = np.diag(d)
    # Hessian
    hess = np.dot(
            np.transpose(X),
            np.dot(D, X)
    )
    # Gradient
    grad = np.dot(
            np.transpose(X),
            np.dot(
                    D,
                    z + np.divide(Y - s, d)
            )
    )
    # Log-likelihood
    ls1, ls2 = np.log(s), np.log(1 - s)
    ll = np.dot(Y, ls1) + np.dot(1 - Y, ls2)
    # sum Y, sum Y**2, ssres (residual sum of squares), sstot (total sum of squares)
    y_sum = np.sum(Y)
    y_sqsum = y_sum  # Because Y takes values in {0, 1}
    yhat = predict(X, coeff)
    ssres = np.dot(Y - yhat, Y - yhat)
    # True positives, false positives, etc.
    posneg = {'TP': 0, 'FP': 0, 'TN': 0, 'FN': 0}
    for yi, yhi in zip(Y, yhat):
        if yi == yhi == 1:
            posneg['TP'] += 1
        elif yi == 0 and yhi == 1:
            posneg['FP'] += 1
        elif yi == 1 and yhi == 0:
            posneg['FN'] += 1
        elif yi == yhi == 0:
            posneg['TN'] += 1
    # ROC curve
    FP_rate_frac = []
    TP_rate_frac = []
    for thres in np.linspace(1.0, 0.0, num=101):
        TP, TN, FP, FN = 0, 0, 0, 0
        yhat = predict(X, coeff, threshold=thres)
        for yi, yhi in zip(Y, yhat):
            if yi == yhi == 1:
                TP += 1
            elif yi == 0 and yhi == 1:
                FP += 1
            elif yi == 1 and yhi == 0:
                FN += 1
            elif yi == yhi == 0:
                TN += 1
        FP_rate_frac.append((FP, TN + FP))
        TP_rate_frac.append((TP, TP + FN))

    # Pack state and results
    local_out = LogRegrFinal_Loc2Glob_TD(ll, grad, hess, y_sum, y_sqsum, ssres, posneg, FP_rate_frac, TP_rate_frac)
    return local_out


def predict(x, coeff, threshold=0.5):
    return np.array([1 if prob >= threshold else 0 for prob in expit(np.dot(x, coeff))])


def logreg_finalize_global(global_state, global_in):
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
                              "R^2", "adjusted R^2", "McFadden pseudo-R^2", "Cox-Snell pseudo-R^2", "F statistic"],
                             [df_mod, df_resid, ll, ll0, aic, bic, r2, r2_adj, r2_mcf, r2_cs, F_stat]]
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


def termination_condition(global_state):
    delta = global_state['delta']
    iter_ = global_state['iter_']
    if delta < PREC or iter_ >= _MAX_ITER:
        set_algorithms_output_data('STOP')
    else:
        set_algorithms_output_data('CONTINUE')


class LogRegrInit_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        self.n_obs = args[0]
        self.n_cols = args[1]
        self.y_name = args[2]
        self.x_names = args[3]

    def get_data(self):
        return self.n_obs, self.n_cols, self.y_name, self.x_names

    def __add__(self, other):
        assert self.n_cols == other.n_cols, "Local n_cols do not agree."
        return LogRegrInit_Loc2Glob_TD(
                self.n_obs + other.n_obs,
                self.n_cols,
                self.y_name,
                self.x_names,
        )


class LogRegrIter_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 3:
            raise ValueError('Illegal number of arguments.')
        self.ll = args[0]
        self.gradient = args[1]
        self.hessian = args[2]

    def get_data(self):
        return self.ll, self.gradient, self.hessian

    def __add__(self, other):
        assert len(self.gradient) == len(other.gradient), "Local gradient sizes do not agree."
        assert self.hessian.shape == other.hessian.shape, "Local Hessian sizes do not agree."
        return LogRegrIter_Loc2Glob_TD(
                self.ll + other.ll,
                self.gradient + other.gradient,
                self.hessian + other.hessian
        )


class LogRegrIter_Glob2Loc_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        self.coeffs = args[0]

    def get_data(self):
        return self.coeffs


class LogRegrFinal_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 9:
            raise ValueError('Illegal number of arguments.')
        self.ll = args[0]
        self.gradient = args[1]
        self.hessian = args[2]
        self.y_sum = args[3]
        self.y_sqsum = args[4]
        self.ssres = args[5]
        self.posneg = args[6]
        self.FP_rate_frac = args[7]
        self.TP_rate_frac = args[8]

    def get_data(self):
        return self.ll, self.gradient, self.hessian, \
               self.y_sum, self.y_sqsum, self.ssres, \
               self.posneg, self.FP_rate_frac, self.TP_rate_frac

    def __add__(self, other):
        assert len(self.gradient) == len(other.gradient), "Local gradient sizes do not agree."
        assert self.hessian.shape == other.hessian.shape, "Local Hessian sizes do not agree."
        return LogRegrFinal_Loc2Glob_TD(
                self.ll + other.ll,
                self.gradient + other.gradient,
                self.hessian + other.hessian,
                self.y_sum + other.y_sum,
                self.y_sqsum + other.y_sqsum,
                self.ssres + other.ssres,
                {k: v + other.posneg[k] for k, v in self.posneg.items()},
                [(s[0] + o[0], s[1] + o[1]) for s, o in zip(self.FP_rate_frac, other.FP_rate_frac)],
                [(s[0] + o[0], s[1] + o[1]) for s, o in zip(self.TP_rate_frac, other.TP_rate_frac)]
        )
