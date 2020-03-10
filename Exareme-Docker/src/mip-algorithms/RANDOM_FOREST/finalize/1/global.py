from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/RANDOM_FOREST/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')
from algorithm_utils import StateData,set_algorithms_output_data
from cart_lib import Node

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-local_step_dbs', required=True, help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    fname_cur_state = path.abspath(args.cur_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load global state
    global_state = StateData.load(path.abspath(args.prev_state_pkl)).data
    myresult =  {"result": []}
    for i in range(global_state['args_n_trees']):
        if 'STOP' in global_state['rF_Logs'][i]:
            globalTreeJ = global_state['rF_globalTree'][i].tree_to_json()
            myresult["result"].append({"type": "application/json", "data": globalTreeJ})
        elif 'PrivacyError'in global_state['rF_Logs'][i]:
            myresult["result"].append({"type": "application/json", "data": global_state['rF_Logs'][i]['PrivacyError']})
        else:
            myresult["result"].append({"type": "application/json", "data": dict()})

    set_algorithms_output_data(myresult)

if __name__ == '__main__':
    main()
