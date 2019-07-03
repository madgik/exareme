from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/PCA/')

from algorithm_utils import StateData, Global2Local_TD
from lib import PCA2_Loc2Glob_TD


def pca_local(local_state, local_in):
    # Unpack local state
    X, schema_X = local_state['X'], local_state['schema_X']
    # Unpack local input
    mean = local_in['mean']

    # Substract the mean of each variable
    n_obs = len(X)
    X = X - mean
    gramian = np.dot(np.transpose(X), X)

    # Pack results
    local_out = PCA2_Loc2Glob_TD(gramian, n_obs, schema_X)
    return local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-global_step_db', required=True, help='Path to db holding global step results.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_prev_state).get_data()
    # Load global node output
    global_out = Global2Local_TD.load(global_db).get_data()
    # Run algorithm local step
    local_out = pca_local(local_state=local_state, local_in=global_out)
    # Return the output data
    local_out.transfer()


if __name__ == '__main__':
    main()
