from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json
import time
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')

from algorithm_utils import StateData,set_algorithms_output_data,init_logger
from cart_lib import Node

def main(args):
    sys.argv =args
    init_logger()
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    parser.add_argument('-local_step_dbs', required=True,
                        help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_prev_state = path.abspath(args.prev_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load global state
    global_state = StateData.load(fname_prev_state).data
    globalTreeJ = global_state['globalTree'].tree_to_json()

    myresult =  {"result": [{"type": "application/json", "data": globalTreeJ}]}
    t1 = global_state['t1']
    t2 = time.localtime(time.time())
    t0 = [ 'yy','mm','dd','hh','min','sec','wday','yday','isdst']
    logging.info(" Time: ")
    for i in range(len(t1)):
        logging.info([t0[i], t2[i], t1[i], t2[i]- t1[i]])
    set_algorithms_output_data(myresult)

if __name__ == '__main__':   main()
