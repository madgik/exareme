# Forward compatibility
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

from utils.algorithm_utils import parse_exareme_args
from PCA.pca_lib import get_data, local_1


def main(args):
    fname_cur_state = path.abspath(args.cur_state_pkl)

    local_in = get_data(args)
    # Run algorithm local step
    local_state, local_out = local_1(args, local_in=local_in)
    # Save local state
    local_state.save(fname=fname_cur_state)
    # Transfer local output
    local_out.transfer()


if __name__ == '__main__':
    args = parse_exareme_args(__file__)
    main(args)
