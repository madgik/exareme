from __future__ import division
from __future__ import print_function

import sys
from os import path, getcwd
from argparse import ArgumentParser
import numpy as np

_ALGORITHM_TYPE = 'python_multiple_local_global'

if _ALGORITHM_TYPE == 'python_local_global':
    dir_levels = 2
elif _ALGORITHM_TYPE == 'python_multiple_local_global':
    dir_levels = 3
elif _ALGORITHM_TYPE == 'python_iterative':
    if path.basename(getcwd()) == 'termination_condition':
        dir_levels = 3
    else:
        dir_levels = 4
else:
    raise ValueError('_ALGORITHM_TYPE unknown type.')
new_path = path.abspath(__file__)
for _ in range(dir_levels):
    new_path = path.dirname(new_path)
sys.path.append(new_path)

from utils.algorithm_utils import StateData, Global2Local_TD, TransferData


def pca_local(local_state, local_in):
    # Unpack local state
    X, schema_X = local_state['X'], local_state['schema_X']
    # Unpack local input
    data = local_in.get_data()
    mean = data['mean']

    # Substract the mean of each variable
    n_obs = len(X)
    X = X - mean
    gramian = np.dot(np.transpose(X), X)

    # Pack results
    local_out = TransferData(gramian=(gramian, 'add'), n_obs=(n_obs, 'add'), schema_X=(schema_X, 'do_nothing'))
    return local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-global_step_db', required=True, help='Path to db holding global step results.')
    args, _ = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_prev_state).get_data()
    # Load global node output
    global_out = TransferData.load(global_db)
    # Run algorithm local step
    local_out = pca_local(local_state=local_state, local_in=global_out)
    # Return the output data
    local_out.transfer()


if __name__ == '__main__':
    main()
