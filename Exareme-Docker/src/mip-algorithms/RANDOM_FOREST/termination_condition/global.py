from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/RANDOM_FOREST/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/CART/')

from algorithm_utils import StateData, set_algorithms_output_data

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    #parser.add_argument('-max_depth', type=int, required=True, help='Maximum depth of tree')
    args, unknown = parser.parse_known_args()

    global_state = StateData.load(path.abspath(args.prev_state_pkl)).data
    count  = 0
    for i in xrange(global_state['args_n_trees']):
        if "STOP" in global_state['rF_Logs'][i] or "PrivacyError" in global_state['rF_Logs'][i]:
            count = count + 1
    if count == global_state['args_n_trees'] :
        set_algorithms_output_data('STOP')
    else:
        set_algorithms_output_data('CONTINUE')

if __name__ == '__main__':
    main()
