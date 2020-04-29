from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import pandas as pd

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')

from algorithm_utils import StateData, ExaremeError, init_logger
from cart_lib import CartInit_Loc2Glob_TD, Cart_Glob2Loc_TD, cart_init_1_global

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-local_step_dbs', required=True,
                        help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    fname_cur_state = path.abspath(args.cur_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Merge local nodes output
    args_X, args_Y, CategoricalVariables, t1 = CartInit_Loc2Glob_TD.load(local_dbs).get_data()

    # Run algorithm global step
    globalTree, activePaths = cart_init_1_global()

    # Save global state
    global_state = StateData(   stepsNo  = 0 ,
                                args_X = args_X,
                                args_Y = args_Y,
                                CategoricalVariables = CategoricalVariables,
                                globalTree = globalTree,
                                activePaths = activePaths,
                                t1 = t1)
    global_state.save(fname=fname_cur_state)

    # Transfer local output
    global_out = Cart_Glob2Loc_TD( globalTree, activePaths )
    global_out.transfer()


if __name__ == '__main__':
    main()
