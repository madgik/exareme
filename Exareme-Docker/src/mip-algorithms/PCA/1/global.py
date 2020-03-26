from __future__ import division
from __future__ import print_function

import sys
from os import path

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

from utils.algorithm_utils import TransferAndAggregateData, parse_exareme_args
from PCA.pca_lib import global_1


def main(args):
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = TransferAndAggregateData.load(local_dbs)
    # Run algorithm global step
    global_out = global_1(args, global_in=local_out)

    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    args = parse_exareme_args(__file__)
    main(args)
