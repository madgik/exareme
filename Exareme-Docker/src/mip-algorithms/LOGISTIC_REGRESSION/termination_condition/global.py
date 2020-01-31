from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from os import path

from LOGISTIC_REGRESSION.log_regr_lib import termination_condition
from utils.algorithm_utils import StateData, parse_exareme_args


def main(args):
    fname_prev_state = path.abspath(args.prev_state_pkl)

    global_state = StateData.load(fname_prev_state).data
    termination_condition(global_state)


if __name__ == '__main__':
    main(parse_exareme_args(__file__))
