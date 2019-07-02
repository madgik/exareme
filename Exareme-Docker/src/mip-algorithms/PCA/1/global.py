from __future__ import division
from __future__ import print_function

import sys
import math
import json
from os import path
import numpy as np
from argparse import ArgumentParser

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/PCA/')


from algorithm_utils import Global2Local_TD, ExaremeError
from lib import PCA1_Loc2Glob_DT


def pca_global(global_in):
    nn, sx = global_in.get_data()
    n_cols = len(nn)
    mean = np.empty(n_cols, dtype=np.float)
    for i in xrange(n_cols):
        mean[i] = sx[i] / nn[i]

    global_out = Global2Local_TD(mean=mean)

    return global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = PCA1_Loc2Glob_DT.load(local_dbs)
    # Run algorithm global step
    global_out = pca_global(global_in=local_out)

    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main()
