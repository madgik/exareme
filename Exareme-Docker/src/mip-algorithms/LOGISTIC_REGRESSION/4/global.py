from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData
from log_regr_lib import LogRegrIter_Loc2Glob_TD, LogRegrIter_Glob2Loc_TD


def logregr_global_iter(global_state, global_in):
    # Unpack global state
    n_obs, n_cols, ll_old, coeff, y_val_dict, schema_X, schema_Y = global_state
    # Unpack global input
    ll_new, grad, hess = global_in.get_data()

    # Compute new coefficients
    coeff = np.dot(
            np.linalg.inv(hess),
            grad
    )
    # Compute delta
    delta = abs(ll_new - ll_old)

    # Pack state and results
    global_state = StateData(n_obs=n_obs, n_cols=n_cols, ll=ll_new, coeff=coeff, delta=delta,
                             y_val_dict=y_val_dict, schema_X=schema_X, schema_Y=schema_Y)
    global_out = LogRegrIter_Glob2Loc_TD(coeff)
    return global_state, global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-local_step_dbs', required=True,
                        help='Path to db holding local step results.')
    args = parser.parse_args()
    fname_cur_state = path.abspath(args.cur_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load global state
    global_state = StateData.load(fname_cur_state).get_data()
    # Load local nodes output
    local_out = LogRegrIter_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_state, global_out = logregr_global_iter(global_state=global_state, global_in=local_out)
    # Save global state
    global_state.save(fname=fname_cur_state)
    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main()
