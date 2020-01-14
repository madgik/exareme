from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
from scipy.special import expit

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData
from log_regr_lib import LogRegrIter_Glob2Loc_TD, LogRegrFinal_Loc2Glob_TD


def logregr_local_final(local_state, local_in):
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


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    parser.add_argument('-global_step_db', required=True,
                        help='Path to db holding global step results.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_prev_state).data
    # Load global node output
    global_out = LogRegrIter_Glob2Loc_TD.load(global_db)
    # Run algorithm local iteration step
    local_out = logregr_local_final(local_state=local_state, local_in=global_out)
    # Return
    local_out.transfer()


if __name__ == '__main__':
    main()
