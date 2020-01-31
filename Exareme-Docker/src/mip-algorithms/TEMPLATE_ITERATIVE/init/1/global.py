from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from os import path

from TEMPLATE_ITERATIVE.template_lib import template_init_global
from utils.algorithm_utils import TransferAndAggregateData, parse_exareme_args


def main(args):
    fname_cur_state = path.abspath(args.cur_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load local nodes output
    local_out = TransferAndAggregateData.load(local_dbs)
    # Run algorithm global step
    global_state, global_out = template_init_global(args, global_in=local_out)
    # Save global state
    global_state.save(fname=fname_cur_state)
    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main(parse_exareme_args(__file__))
