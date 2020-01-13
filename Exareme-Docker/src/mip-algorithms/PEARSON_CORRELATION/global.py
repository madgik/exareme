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

from utils.algorithm_utils import parse_exareme_args, set_algorithms_output_data
from pearsonc_lib import pearson_global, PearsonCorrelationLocalDT


def main(args):
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = PearsonCorrelationLocalDT.load(local_dbs)
    # Run algorithm global step
    global_out = pearson_global(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    args = parse_exareme_args(__file__)
    main(args)
