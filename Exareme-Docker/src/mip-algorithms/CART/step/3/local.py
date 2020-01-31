from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import pandas as pd
import numpy as np

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')

from algorithm_utils import StateData
from cart_lib import Cart_Glob2Loc_TD, CartIter3_Loc2Glob_TD, DataFrameFilter



def compute_statistics2_in_the_node(dataFrame, colNames, activePath, className, CategoricalVariables):
    statisticsJ = dict()
    for colName in colNames:
        df = dataFrame[[colName,className]]
        thresholds =  activePath["thresholdsJ"][colName]
        mseLeft = [None]*len(thresholds)
        mseRight = [None]*len(thresholds)
        for i in xrange(len(thresholds)):
            if colName not in CategoricalVariables:
                dfLeft = df.loc[df[colName] <= thresholds[i]]
                dfRight = df.loc[df[colName] > thresholds[i]]
            elif colName in CategoricalVariables:
                dfLeft = df.loc[df[colName] in thresholds[i]['left']]
                dfRight = df.loc[df[colName] in thresholds[i]['right']]


            mseLeft[i] = None
            mseRight[i] = None
            if activePath["statisticsJ"][colName]["meanLeft"][i] is not None:
                mseLeft[i] = np.sum((dfLeft[className] - activePath["statisticsJ"][colName]["meanLeft"][i])**2)
            if activePath["statisticsJ"][colName]["meanRight"][i] is not None:
                mseRight[i] = np.sum((dfRight[className] - activePath["statisticsJ"][colName]["meanRight"][i])**2)

        statisticsJ[colName] =  {"mseLeft" : mseLeft, "mseRight" : mseRight}
    #print (statisticsJ)
    return statisticsJ



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
    for key in activePaths:
        df = local_state['dataFrame']
        # For each unfinished path, find the subset of dataFrame (df)
        for i in xrange(len(activePaths[key]['filter'])):
            df = DataFrameFilter(df, activePaths[key]['filter'][i]["variable"],
                                     activePaths[key]['filter'][i]["operator"],
                                     activePaths[key]['filter'][i]["value"])

        if local_state['args_Y'][0] not in local_state['CategoricalVariables']: # Regression Algorithm
            activePaths[key]["statisticsJ"]["parentNode"]["mse"] = np.sum((df[local_state['args_Y'][0]] - activePaths[key]["statisticsJ"]["parentNode"]["mean_argsY"])**2 )
            mystat = compute_statistics2_in_the_node(df, local_state['args_X'], activePaths[key], local_state['args_Y'][0], local_state['CategoricalVariables'])
            for colName in local_state['args_X']:
                activePaths[key]["statisticsJ"][colName]["mseLeft"] = mystat[colName]["mseLeft"]
                activePaths[key]["statisticsJ"][colName]["mseRight"] = mystat[colName]["mseRight"]
    #print activePaths

    ## Finished
    local_state = StateData( args_X = local_state['args_X'],
                             args_Y = local_state['args_Y'],
                             CategoricalVariables =  local_state['CategoricalVariables'],
                             dataFrame = local_state['dataFrame'],
                             globalTree = globalTree,
                             activePaths = activePaths)

    local_out = CartIter3_Loc2Glob_TD(activePaths)

    # Save local state
    local_state.save(fname=fname_cur_state)
    # Return
    local_out.transfer()



if __name__ == '__main__':
    main()
