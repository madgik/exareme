from __future__ import division
from __future__ import print_function

import sys
from os import path
import os
import errno
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
    y_val_dict = global_state['y_val_dict']
    schema_X = global_state['schema_X']
    schema_Y = global_state['schema_Y']
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
    sstot = n_obs * y_mean * (1 - y_mean) # Using binomial variable variance formula
    r2 = 1 - ssres / sstot
    r2_adj = 1 - (1 - r2) * (n_obs - 1) / (n_obs - n_cols - 1)
    r2_mcf = 1 - ll / ll0
    r2_cs = 1 - np.exp(-ll0 * 2 * r2_mcf / n_obs)
    # Confusion matrix etc.
    TP, TN, FP, FN = posneg['TP'], posneg['TN'], posneg['FP'], posneg['FN']
    confusion_mat = [[TP, FP], [FN, TP]]
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
    # raise ValueError(sstot, ssres, n_obs, n_cols, F_stat, y_mean, y_sqsum, y_sum)

    # Write output to JSON
    result = {
        'result': {
            'Covariates'                 : [
                {
                    'Name'       : schema_X[i],
                    'Coefficient': coeff[i],
                    'std.err.'   : stderr[i],
                    'z score'    : z_scores[i],
                    'p value'    : p_values[i] if p_values[i] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
                    'Lower C.I.' : lci[i],
                    'Upper C.I.' : rci[i]
                }
                for i in range(len(schema_X))
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
            'ROC coordinates'            : list(zip(FP_rate, TP_rate)),
            'AUC'                        : AUC,
            'Gini coefficient'           : gini,
            'F statistic'                : F_stat
        }
    }
    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        raise ExaremeError('Result contains NaNs.')
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
