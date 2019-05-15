from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData
from log_regr_lib import LogRegrInit_Loc2Glob_TD, LogRegrIter_Glob2Loc_TD


def logregr_global_init(global_in):
    n_obs, n_cols, y_val_dict, schema_X, schema_Y = global_in.get_data()

    # Init vars
    ll = - 2 * n_obs * np.log(2)
    coeff = np.zeros(n_cols)

    # Pack state and results
    global_state = StateData(n_obs=n_obs, n_cols=n_cols, ll=ll, coeff=coeff,
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

    # Load local nodes output
    local_out = LogRegrInit_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_state, global_out = logregr_global_init(global_in=local_out)
    # Save global state
    global_state.save(fname=fname_cur_state)
    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main()
