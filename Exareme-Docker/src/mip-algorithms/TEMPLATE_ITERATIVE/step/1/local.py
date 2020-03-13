from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from os import path

from TEMPLATE_ITERATIVE.template_lib import template_step_local
from utils.algorithm_utils import TransferAndAggregateData, StateData, parse_exareme_args


def main(args):
    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_prev_state = path.abspath(args.prev_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_prev_state).data
    # Load global node output
    global_out = TransferAndAggregateData.load(global_db)
    # Run algorithm local iteration step
    local_state, local_out = template_step_local(args, local_state=local_state, local_in=global_out)
    # Save local state
    local_state.save(fname=fname_cur_state)
    # Return
    local_out.transfer()


if __name__ == '__main__':
    main(parse_exareme_args(__file__))
