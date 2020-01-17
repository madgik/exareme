from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')

from algorithm_utils import StateData,PRIVACY_MAGIC_NUMBER
from cart_lib import CartIter3_Loc2Glob_TD, Cart_Glob2Loc_TD, Node, add_vals

def compute_gini(colName, thresholds, classNumbersJOfColname, nnNode, classDistVal):
    #Compute gini index for the new nodes
    giniTotal = [0.0]*len(thresholds)
    for i in xrange(len(thresholds)):
        ccLeft = 0.0
        nnLeft = 0
        ccRight = 0.0
        nnRight = 0
        giniTotal[i] = 0.0
        for val in classDistVal:
            if val in classNumbersJOfColname["countsLeft"][i]:
                ccLeft += classNumbersJOfColname["countsLeft"][i][val]**2
                nnLeft += classNumbersJOfColname["countsLeft"][i][val]
            if val in classNumbersJOfColname["countsRight"][i]:
                #print classNumbersJOfColname["countsRight"][i][val]
                ccRight += classNumbersJOfColname["countsRight"][i][val]**2
                nnRight += classNumbersJOfColname["countsRight"][i][val]
        if nnLeft>0:
            giniLeft = (1.0 - ccLeft/(nnLeft**2))
            giniTotal[i] += (nnLeft/nnNode) * (1.0 - ccLeft/nnLeft**2)
        if nnRight>0:
            giniRight = (1.0 - ccRight/(nnRight**2))
            giniTotal[i] += (nnRight/nnNode) * (1.0 - ccRight/nnRight**2)
    bestGini = min(giniTotal)
    bestThreshold = thresholds[giniTotal.index(bestGini)]
    #print bestGini,bestThreshold
    return  colName, bestGini, bestThreshold

def compute_mse(colName, thresholds, statisticJOfColname, gainNode, nnNode):
    #Compute mse for the right and left node
    gain = [0.0]*len(thresholds)
    for i in xrange(len(thresholds)):
        gain[i] = None
        if nnNode!=0 and (statisticJOfColname['mseLeft'][i] is not None or statisticJOfColname['mseRight'][i] is not None):
            gain[i] = add_vals(statisticJOfColname['mseLeft'][i],statisticJOfColname['mseRight'][i])/ nnNode
    bestGain = min(gain)
    bestThreshold = thresholds[gain.index(bestGain)]
    return  colName, bestGain, bestThreshold

def best_splits(activePath, colNames, className, CategoricalVariables):
    bestGain = None
    #Compute mse for the parent node
    if className in CategoricalVariables: # Classification Algorithm
        gainNode = 0.0
        for key in activePath["classNumbersJ"]["parentNode"]["counts"]:
            gainNode += activePath["classNumbersJ"]["parentNode"]["counts"][key]**2
        gainNode = 1.0 - gainNode / activePath["samples"]**2
    elif className not in CategoricalVariables: # Regression Algorithm
        gainNode =  activePath["statisticsJ"]["parentNode"]["mse"]/activePath["statisticsJ"]["parentNode"]["nn_argsY"]

    for colName in colNames:
        if className in CategoricalVariables: # Classification Algorithm
             colName, gain, threshold = compute_gini(colName, activePath["thresholdsJ"][colName],  activePath["classNumbersJ"][colName], activePath["samples"], CategoricalVariables[className])
        elif className not in CategoricalVariables: # Regression Algorithm
            colName, gain, threshold = compute_mse(colName, activePath["thresholdsJ"][colName], activePath["statisticsJ"][colName], gainNode, activePath["statisticsJ"]["parentNode"]["nn_argsY"])
        if bestGain == None or bestGain > gain:
            bestColName = colName
            bestGain = gain
            bestThreshold = threshold

    return  bestColName, gainNode, bestGain, bestThreshold


def main():
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
    # Load local nodes output
    activePaths = CartIter3_Loc2Glob_TD.load(local_dbs).get_data()

    # Run algorithm global steps
    if  global_state['args_Y'][0] not in global_state['CategoricalVariables']: # Regression Algorithm
        criterion = "mse"
    elif global_state['args_Y'][0] in global_state['CategoricalVariables']: # Classification Algorithm
        criterion = "gini"

    activePathsNew = dict()
    no = 0
    for key in activePaths:
        if activePaths[key]["samples"] > PRIVACY_MAGIC_NUMBER:
            bestColName, valueParentNode, bestValue, bestThreshold = best_splits( activePaths[key], global_state['args_X'], global_state['args_Y'][0], global_state['CategoricalVariables'])

            if valueParentNode > 0 : #If the number of samples>PRIVACY_NYMBER then I have privacy issues or #If GiniNode = 0 then I am in a leaf wih pure class.
                 #print ("MYKEY", key,activePaths[key]["samples"])
                 activePathsNew[no]=dict()
                 activePathsNew[no]["thresholdsJ"] = None
                 activePathsNew[no]["filter"] = activePaths[key]['filter'][:]
                 activePathsNew[no]["filter"].append( { "variable" : bestColName,"operator" : "<=","value" : bestThreshold })

                 activePathsNew[no+1]=dict()
                 activePathsNew[no+1]["thresholdsJ"] = None
                 activePathsNew[no+1]["filter"] = activePaths[key]['filter'][:]
                 activePathsNew[no+1]["filter"].append( { "variable" : bestColName, "operator" : ">", "value" : bestThreshold })

                 no = no + 2
                 samplesPerClass = None
                 if global_state['args_Y'][0] in global_state['CategoricalVariables']: # Classification Algorithm
                     samplesPerClass = activePaths[key]["classNumbersJ"]["parentNode"]["counts"]
                 if global_state['globalTree'] is None:
                     global_state['globalTree'] = Node(criterion, bestColName, valueParentNode, bestThreshold, activePaths[key]["samples"], samplesPerClass)
                     globalTreeJ = global_state['globalTree'].tree_to_json()
                     #raise ValueError("AA", criterion, bestColName, valueParentNode, bestThreshold, activePaths[key]["samples"], samplesPerClass)
                 else:
                     global_state['globalTree'].grow_tree(activePaths[key]['filter'], criterion, bestColName, valueParentNode, bestThreshold,activePaths[key]["samples"], samplesPerClass)
        else: #It is leaf -->TODO
            samplesPerClass = None
            if global_state['args_Y'][0] in global_state['CategoricalVariables']: # Classification Algorithm
                samplesPerClass = activePaths[key]["classNumbersJ"]["parentNode"]["counts"]
            global_state['globalTree'].grow_tree(activePaths[key]['filter'], criterion, None, None, None,activePaths[key]["samples"], samplesPerClass) #Isws edw na dinw kai alla stoixeia. Alla logw privacy den xreiazetai
    activePaths = activePathsNew
    globalTreeJ = global_state['globalTree'].tree_to_json()
    #if global_state['stepsNo'] ==1:
    #    raise ValueError(globalTreeJ)
    #print activePaths
    #print globalTreeJ
    ## Finish

    global_out = Cart_Glob2Loc_TD(global_state['globalTree'], activePaths)
    # Save global state
    global_state = StateData(   stepsNo = global_state['stepsNo'] ,
                                args_X = global_state['args_X'],
                                args_Y = global_state['args_Y'],
                                CategoricalVariables = global_state['CategoricalVariables'],
                                globalTree = global_state['globalTree'],
                                activePaths = activePaths )
    global_state.save(fname=fname_cur_state)
    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main()
