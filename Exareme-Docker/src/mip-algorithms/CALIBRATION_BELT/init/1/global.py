from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/CALIBRATION_BELT/')

from algorithm_utils import StateData, ExaremeError
from cb_lib import CBInit_Loc2Glob_TD, CBIter_Glob2Loc_TD


def cb_global_init(global_in):
    n_obs, e_name, o_name, max_deg = global_in.get_data()

    if n_obs == 0:
        raise ExaremeError('The selected variables contain 0 datapoints.')

    # Init vars
    ll_dict = dict()
    coeff_dict = dict()
    ll = - 2 * n_obs * np.log(2)
    for deg in range(1, max_deg + 1):
        ll_dict[deg] = ll
        coeff_dict[deg] = np.zeros(deg + 1)
    iter = 0

    # Pack state and results
    global_state = StateData(n_obs=n_obs, ll_dict=ll_dict, coeff_dict=coeff_dict, iter=iter,
                             e_name=e_name, o_name=o_name, max_deg=max_deg)
    global_out = CBIter_Glob2Loc_TD(coeff_dict)

    return global_state, global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-local_step_dbs', required=True,
                        help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    fname_cur_state = path.abspath(args.cur_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load local nodes output
    local_out = CBInit_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_state, global_out = cb_global_init(global_in=local_out)
    # Save global state
    global_state.save(fname=fname_cur_state)
    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main()
