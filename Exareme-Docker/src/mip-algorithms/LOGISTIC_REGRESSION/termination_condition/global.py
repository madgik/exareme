from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import sys
from os import path
from argparse import ArgumentParser
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) +
                '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData, set_algorithms_output_data
from log_regr_lib import PREC


def termination_condition(global_state, max_iter):
    delta = global_state['delta']
    iter = global_state['iter']
    if delta < PREC or iter >= max_iter:
        set_algorithms_output_data('STOP')
    else:
        set_algorithms_output_data('CONTINUE')


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    parser.add_argument('-max_iter', type=int, required=True, help='Maximum number of iterations.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)
    max_iter = args.max_iter

    global_state = StateData.load(fname_prev_state).data
    termination_condition(global_state, max_iter)


if __name__ == '__main__':
    main()
