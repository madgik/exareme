from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import pandas as pd
import numpy as np
import json

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')

from algorithm_utils import StateData
from cart_lib import Cart_Glob2Loc_TD, CartIter2_Loc2Glob_TD, cart_step_2_local

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    parser.add_argument('-global_step_db', required=True,
                        help='Path to db holding global step results.')
    args, unknown = parser.parse_known_args()

    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_prev_state = path.abspath(args.prev_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_prev_state).data
    # Load global node output
    globalTree, activePaths = Cart_Glob2Loc_TD.load(global_db).get_data()

    # Run algorithm local iteration step
    activePaths = cart_step_2_local(local_state['dataFrame'], local_state['CategoricalVariables'], local_state['args_X'], local_state['args_Y'], activePaths)
    #
    # # Run algorithm local iteration step
    # for key in activePaths:
    #     df = local_state['dataFrame']
    #     # For each unfinished path, find the subset of dataFrame (df)
    #     for i in xrange(len(activePaths[key]['filter'])):
    #         df = DataFrameFilter(df, activePaths[key]['filter'][i]["variable"],
    #                                  activePaths[key]['filter'][i]["operator"],
    #                                  activePaths[key]['filter'][i]["value"])
    #     if  local_state['args_Y'][0] in local_state['CategoricalVariables']:  #Classification Algorithm
    #         resultJ = node_computations(df,  local_state['args_X'], activePaths[key],  local_state['args_Y'][0],  local_state['CategoricalVariables'],"classNumbers")
    #         activePaths[key]["classNumbersJ"] = dict(activePaths[key]["classNumbersJ"].items() + resultJ.items())
    #     elif  local_state['args_Y'][0] not in local_state['CategoricalVariables']: # Regression Algorithm
    #         resultJ = node_computations(df,  local_state['args_X'], activePaths[key],  local_state['args_Y'][0],  local_state['CategoricalVariables'],"statistics")
    #         activePaths[key]["statisticsJ"] = dict(activePaths[key]["statisticsJ"].items() + resultJ.items())
    # #print activePaths
    ## Finished
    local_state = StateData( args_X = local_state['args_X'],
                             args_Y = local_state['args_Y'],
                             CategoricalVariables =  local_state['CategoricalVariables'],
                             dataFrame = local_state['dataFrame'],
                             globalTree = globalTree,
                             activePaths = activePaths)

    local_out = CartIter2_Loc2Glob_TD(activePaths)

    # Save local state
    local_state.save(fname=fname_cur_state)
    # Return
    local_out.transfer()



if __name__ == '__main__':
    main()
