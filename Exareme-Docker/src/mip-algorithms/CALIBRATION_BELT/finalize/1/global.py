from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/CALIBRATION_BELT/')

from algorithm_utils import StateData, set_algorithms_output_data
from cb_lib import CBIter_Loc2Glob_TD


def cb_global_final(global_state, global_in):
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

    # likelihood-ratio test
    D = dict()
    for deg in range(2, max_deg + 1):
        D[deg] = 2 * (ll_dict[deg] - ll_dict[deg - 1])

    # Format output data
    # JSON raw
    raw_data = {
        'Model Parameters': [
            {
                'deg'  : deg,
                'coeff': list(coeff_dict[deg]),
                'log-likelihood': ll_dict[deg]
            }
            for deg in range(1, max_deg + 1)
        ],
        'Likelihood ration test': D
    }

    # Write output to JSON
    result = {
        'result': [
            # Raw results
            {
                "type": "application/json",
                "data": [
                    raw_data
                ]
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
    local_out = CBIter_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_out = cb_global_final(global_state=global_state, global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
