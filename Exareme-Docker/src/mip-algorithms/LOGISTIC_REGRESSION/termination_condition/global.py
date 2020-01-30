from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from os import path

from argparse import ArgumentParser
from utils.algorithm_utils import StateData, set_algorithms_output_data

from LOGISTIC_REGRESSION.log_regr_lib import PREC

_MAX_ITER = 40


def termination_condition(global_state):
    delta = global_state['delta']
    iter_ = global_state['iter_']
    if delta < PREC or iter_ >= _MAX_ITER:
        set_algorithms_output_data('STOP')
    else:
        set_algorithms_output_data('CONTINUE')


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)

    global_state = StateData.load(fname_prev_state).data
    termination_condition(global_state)


if __name__ == '__main__':
    main()
