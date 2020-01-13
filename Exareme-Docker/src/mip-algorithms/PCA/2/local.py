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

from utils.algorithm_utils import StateData, TransferAndAggregateData, parse_exareme_args
from PCA.pca_lib import local_2


def main(args):
    fname_prev_state = path.abspath(args.prev_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_prev_state).get_data()
    # Load global node output
    global_out = TransferAndAggregateData.load(global_db)
    # Run algorithm local step
    local_out = local_2(local_state=local_state, local_in=global_out)
    # Return the output data
    local_out.transfer()


if __name__ == '__main__':
    args = parse_exareme_args(__file__)
    main(args)
