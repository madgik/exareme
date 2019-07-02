from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/PCA/')


from algorithm_utils import StateData, set_algorithms_output_data, make_json_raw
from lib import PCA2_Loc2Glob_DT


def pca_global(global_in):
    # Unpack global input
    gramian, n_obs, schema_X = global_in.get_data()
    raise ValueError(gramian)
    gramian = np.divide(gramian, n_obs - 1)

    eigen_vals, eigen_vecs = np.linalg.eig(gramian)

    result = make_json_raw(eigen_vals=eigen_vals, eigen_vecs=eigen_vecs)
    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        raise ValueError('Result contains NaNs.')
    return global_out


def main():
    # Parse arguments
    parser = ArgumentParser()

    parser.add_argument('-local_step_dbs', required=True, help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    # Load local nodes output
    local_out = PCA2_Loc2Glob_DT.load(local_dbs)
    # Run algorithm global step
    global_out = pca_global(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
