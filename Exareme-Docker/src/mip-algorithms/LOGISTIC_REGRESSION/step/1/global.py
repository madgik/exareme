from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from os import path

from LOGISTIC_REGRESSION.log_regr_lib import LogRegrIter_Loc2Glob_TD, logreg_iter_global
from utils.algorithm_utils import StateData, parse_exareme_args


def main(args):
    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_prev_state = path.abspath(args.prev_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load global state
    global_state = StateData.load(fname_prev_state).data
    # Load local nodes output
    local_out = LogRegrIter_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_state, global_out = logreg_iter_global(global_state=global_state, global_in=local_out)
    # Save global state
    global_state.save(fname=fname_cur_state)
    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main(parse_exareme_args(__file__))
