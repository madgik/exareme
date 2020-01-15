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
from cart_lib import Cart_Glob2Loc_TD, CartIter1_Loc2Glob_TD, SAMPLING_NUMBER, DataFrameFilter



def compute_local_thresholds(X, args_X, CategoricalVariables):
    thresholdsJ = dict()
    for varx in args_X:
        thresholdsJ[varx] = []
        if varx not in CategoricalVariables: # For numeric only variables
            X_sorted = sorted(X[varx])
            nn = len(X_sorted)
            indexThresholds = []
            for i in range(SAMPLING_NUMBER-1, nn - SAMPLING_NUMBER, SAMPLING_NUMBER):
                thresholdsJ[varx].append((X_sorted[i]+X_sorted[i+1])/2.0)
        elif varx in CategoricalVariables: # For categorical only variables
            L = []
            for combs in (itertools.combinations(CategoricalVariables[varx], r) for r in range(len(CategoricalVariables[varx])+1))  :
                for comb in combs:
                    diff = list(set( CategoricalVariables[varx][:]) - set(comb))
                    #print diff, list(comb)
                    if diff != [] and list(comb) != []:
                        L.append({"left" : diff, "right" : list(comb)})
            thresholdsJ[varx] = L[0:len(L)/2]

        thresholdsJ[varx] = set(thresholdsJ[varx]) #keep unique values
        thresholdsJ[varx] = list(thresholdsJ[varx])
        thresholdsJ[varx].sort()
    return  thresholdsJ


def run_local_step(X, args_X, args_Y, CategoricalVariables, activePaths):
    # Compute local thresholds for each activePath and variable,
    # Compute samples per class (for classification) , ss and nn (for regression )
    if activePaths == None:
        activePaths = dict()
        activePaths[0] = dict()
        activePaths[0]["filter"] = []
        activePaths[0]["thresholdsJ"] = compute_local_thresholds(X, args_X, CategoricalVariables)
        activePaths[0]["samples"] = X.shape[0]
        if args_Y[0] not in CategoricalVariables: # Regression Algorithm
            activePaths[0]["statisticsJ"] = dict()
            activePaths[0]["statisticsJ"]["parentNode"] = {"ss_argsY" : np.sum(X[args_Y[0]]), "nn_argsY" : X.shape[0] }
        elif args_Y[0] in CategoricalVariables: # Classification Algorithm
            activePaths[0]["classNumbersJ"] = dict()
            activePaths[0]["classNumbersJ"]["parentNode"] = {"counts": json.loads(X.groupby(args_Y[0])[args_Y[0]].count().to_json())}
            #["samplesPerClass"] = json.loads(X.groupby(args_Y[0])[args_Y[0]].count().to_json())
        else :
            raise ValueError("Error-local1", activePaths)
    else:
        for key in activePaths:
            dX = X
            # For each unfinished path, find the subset of dataFrame (df)
            for i in xrange(len(activePaths[key]['filter'])):
                dX = DataFrameFilter(dX, activePaths[key]['filter'][i]["variable"],
                                         activePaths[key]['filter'][i]["operator"],
                                         activePaths[key]['filter'][i]["value"])
            activePaths[key]["thresholdsJ"] = compute_local_thresholds(dX, args_X, CategoricalVariables)
            activePaths[key]["samples"] = dX.shape[0]
            if args_Y[0] not in CategoricalVariables: # Regression Algorithm
                activePaths[key]["statisticsJ"] = dict()
                activePaths[key]["statisticsJ"]["parentNode"] = {"ss_argsY" : np.sum(dX[args_Y[0]]), "nn_argsY" : dX.shape[0] }
            elif args_Y[0] in CategoricalVariables: # Classification Algorithm
                activePaths[key]["classNumbersJ"] = dict()
                activePaths[key]["classNumbersJ"]["parentNode"] = {"counts": json.loads(dX.groupby(args_Y[0])[args_Y[0]].count().to_json())}
            else:
                raise ValueError ("ERROR2", activePaths)
    return activePaths

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
    activePaths = run_local_step( local_state['dataFrame'],
                                  local_state['args_X'],
                                  local_state['args_Y'],
                                  local_state['CategoricalVariables'],
                                  activePaths)
    # Save local state
    local_state = StateData( dataFrame = local_state['dataFrame'],
                             args_X = local_state['args_X'],
                             args_Y = local_state['args_Y'],
                             CategoricalVariables =  local_state['CategoricalVariables'])
    local_state.save(fname=fname_cur_state)

    # Transfer local output
    local_out = CartIter1_Loc2Glob_TD(activePaths)
    local_out.transfer()

if __name__ == '__main__':
    main()
