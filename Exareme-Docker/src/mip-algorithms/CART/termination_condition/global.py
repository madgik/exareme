from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/CART/')

from algorithm_utils import StateData, set_algorithms_output_data

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    #parser.add_argument('-max_iter', type=int, required=True, help='Maximum number of iterations.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)
    #max_iter = args.max_iter

    global_state = StateData.load(fname_prev_state).data

    if bool(global_state['activePaths']) == False or global_state['stepsNo'] > 3 :
        set_algorithms_output_data('STOP')
    else:
        set_algorithms_output_data('CONTINUE')

if __name__ == '__main__':
    main()
