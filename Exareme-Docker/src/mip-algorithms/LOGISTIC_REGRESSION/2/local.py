from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
from scipy.special import expit

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData
from log_regr_lib import LogRegrIter_Loc2Glob_TD, LogRegrIter_Glob2Loc_TD


def logregr_local_iter(local_state, local_in):
    # Unpack local state
    X, Y = local_state
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


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-s', '-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-global_step_db', required=True,
                        help='Path to db holding global step results.')
    args = parser.parse_args()
    fname_cur_state = path.abspath(args.cur_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_cur_state).get_data()
    # Load global node output
    global_out = LogRegrIter_Glob2Loc_TD.load(global_db)
    # Run algorithm local iteration step
    local_state, local_out = logregr_local_iter(local_state=local_state, local_in=global_out)
    # Save local state
    local_state.save(fname=fname_cur_state)
    # Return
    local_out.transfer()


if __name__ == '__main__':
    main()
