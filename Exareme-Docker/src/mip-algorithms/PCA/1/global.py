from __future__ import division
from __future__ import print_function

import sys
from argparse import ArgumentParser
from os import path, getcwd

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

from utils.algorithm_utils import TransferData


def pca_global(global_in):
    data = global_in.get_data()
    nn, sx = data['nn'], data['sx']
    n_cols = len(nn)
    mean = np.empty(n_cols, dtype=np.float)
    for i in xrange(n_cols):
        mean[i] = sx[i] / nn[i]

    global_out = TransferData(mean=(mean, 'do_nothing'))

    return global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = TransferData.load(local_dbs)
    # Run algorithm global step
    global_out = pca_global(global_in=local_out)

    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main()
