from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from os import path

from LOGISTIC_REGRESSION.log_regr_lib import LogRegrFinal_Loc2Glob_TD, logreg_finalize_global
from utils.algorithm_utils import StateData, set_algorithms_output_data, parse_exareme_args


def main(args):
    fname_prev_state = path.abspath(args.prev_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load global state
    global_state = StateData.load(fname_prev_state).data
    # Load local nodes output
    local_out = LogRegrFinal_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_out = logreg_finalize_global(global_state=global_state, global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main(parse_exareme_args(__file__))
