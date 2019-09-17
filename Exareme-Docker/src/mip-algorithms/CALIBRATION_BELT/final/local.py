from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser

import numpy as np
from scipy.special import expit

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/CALIBRATION_BELT/')

from algorithm_utils import StateData
from cb_lib import CBIter_Loc2Glob_TD, CBIter_Glob2Loc_TD

def cb_local_final(local_state, local_in):
    # Unpack local state
    X_matrices = local_state['X_matrices']
    Y = local_state['Y']
    max_deg = local_state['max_deg']
    # Unpack local input
    coeff_dict = local_in.get_data()

    # Compute 0th, 1st and 2nd derivatives of loglikelihood
    hess_dict = dict()
    grad_dict = dict()
    ll_dict = dict()
    for deg in range(1, max_deg + 1):
        X = X_matrices[deg]
        coeff = coeff_dict[deg]
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
        hess_dict[deg] = hess
        # Gradient
        grad = np.dot(
                np.transpose(X),
                np.dot(
                        D,
                        z + np.divide(Y - s, d)
                )
        )
        grad_dict[deg] = grad
        # Log-likelihood
        ls1, ls2 = np.log(s), np.log(1 - s)
        ll = np.dot(Y, ls1) + np.dot(1 - Y, ls2)
        ll_dict[deg] = ll

    # Pack state and results
    local_state = StateData(X_matrices=X_matrices, Y=Y, max_deg=max_deg)
    local_out = CBFinal_Loc2Glob_TD(ll_dict, grad_dict, hess_dict, max_deg)
    return local_state, local_out



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
    global_out = CBIter_Glob2Loc_TD.load(global_db)
    # Run algorithm local iteration step
    local_out = cb_local_final(local_state=local_state, local_in=global_out)
    # Return
    local_out.transfer()


if __name__ == '__main__':
    main()
