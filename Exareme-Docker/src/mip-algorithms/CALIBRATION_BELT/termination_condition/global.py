from __future__ import division
from __future__ import print_function

import sys
from argparse import ArgumentParser
from os import path

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) +
                '/CALIBRATION_BELT/')

from algorithm_utils import StateData, set_algorithms_output_data
from cb_lib import PREC

_MAX_ITER = 40

def termination_condition(global_state):
    delta_dict = global_state['delta_dict']
    iter = global_state['iter']
    if reduce((lambda x, y: x and y),
              [delta < PREC for delta in delta_dict.values()]) or iter >= _MAX_ITER:
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
