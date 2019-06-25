from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) +
                '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData, set_algorithms_output_data
from log_regr_lib import PREC, MAX_ITER


def termination_condition(global_state):
    delta = global_state['delta']
    iter = global_state['iter']
    if delta < PREC or iter > MAX_ITER:
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
