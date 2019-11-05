from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser

import numpy as np

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/CALIBRATION_BELT/')

from algorithm_utils import StateData
from cb_lib import CBIter_Loc2Glob_TD, CBIter_Glob2Loc_TD


def cb_global_iter(global_state, global_in):
    # Unpack global state
    n_obs = global_state['n_obs']
    ll_old_dict = global_state['ll_dict']
    coeff_dict = global_state['coeff_dict']
    iter = global_state['iter']
    e_name = global_state['e_name']
    o_name = global_state['o_name']
    max_deg = global_state['max_deg']
    # Unpack global input
    ll_dict, grad_dict, hess_dict = global_in.get_data()

    delta_dict = dict()
    for deg in range(1, max_deg + 1):
        # Compute new coefficients
        coeff_dict[deg] = np.dot(
                np.linalg.inv(hess_dict[deg]),
                grad_dict[deg]
        )
        # Update termination quantities
        delta_dict[deg] = abs(ll_dict[deg] - ll_old_dict[deg])
    iter += 1

    # Pack state and results
    global_state = StateData(n_obs=n_obs, ll_dict=ll_dict, coeff_dict=coeff_dict,
                             delta_dict=delta_dict, iter=iter,
                             e_name=e_name, o_name=o_name, max_deg=max_deg)
    global_out = CBIter_Glob2Loc_TD(coeff_dict)
    return global_state, global_out


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
    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_prev_state = path.abspath(args.prev_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load global state
    global_state = StateData.load(fname_prev_state).data
    # Load local nodes output
    local_out = CBIter_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_state, global_out = cb_global_iter(global_state=global_state, global_in=local_out)
    # Save global state
    global_state.save(fname=fname_cur_state)
    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main()
