from __future__ import division
from __future__ import print_function

import sys
from os import path, getcwd

import numpy as np

_new_path = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(_new_path)
while True:
    try:
        import utils.algorithm_utils
    except:
        sys.path.pop()
        _new_path = path.dirname(_new_path)
        sys.path.append(_new_path)
    else:
        break
del _new_path

from utils.algorithm_utils import TransferData, parse_exareme_args


def pca_global(global_in):
    data = global_in.get_data()
    nn, sx = data['nn'], data['sx']
    n_cols = len(nn)
    mean = np.empty(n_cols, dtype=np.float)
    for i in xrange(n_cols):
        mean[i] = sx[i] / nn[i]

    global_out = TransferData(mean=(mean, 'do_nothing'))

    return global_out


def main(args):
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = TransferData.load(local_dbs)
    # Run algorithm global step
    global_out = pca_global(global_in=local_out)

    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    args = parse_exareme_args()
    main(args)
