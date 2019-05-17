from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')

from algorithm_utils import StateData

PREC = 1e-7

def termintation_condition(global_state):
    delta = global_state['delta']
    if delta < PREC:
        sys.exit(1)
    else:
        sys.exit(0)

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)

    global_state = StateData.load(fname_prev_state).data
    termintation_condition(global_state)

if __name__ == '__main__':
    main()