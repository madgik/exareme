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

from utils.algorithm_utils import set_algorithms_output_data, TransferAndAggregateData, StateData, parse_exareme_args
from TEMPLATE_MULTIPLE_LOCAL_GLOBAL.algorithm_lib import global_2


def main(args):
    fname_prev_state = path.abspath(args.prev_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load local state
    global_state = StateData.load(fname_prev_state).get_data()
    # Load local nodes output
    local_out = TransferAndAggregateData.load(local_dbs)
    # Run algorithm global step
    global_out = global_2(global_state=global_state, global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    args = parse_exareme_args(__file__)
    main(args)
