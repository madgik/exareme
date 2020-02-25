from __future__ import division
from __future__ import print_function

import sys
from os import path
import numpy as np
import pandas as pd
import json

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
from algorithm_utils import PRIVACY_MAGIC_NUMBER
from cart_lib import SAMPLING_NUMBER, DataFrameFilter
from cart_lib import Node, add_vals

def cart_init_1_local(dataFrame, dataSchema, CategoricalVariables):
    #Delete null values from DataFrame
    dataFrame = dataFrame.dropna()
    for x in dataSchema:
        if x in CategoricalVariables:
            dataFrame = dataFrame[dataFrame[x].astype(bool)]
    # if len(dataFrame) < PRIVACY_MAGIC_NUMBER:
    #     raise PrivacyError('The Experiment could not run with the input provided because there are insufficient data.')
    return dataFrame

##########################################################################################################################

def cart_init_1_global():
    globalTree = None
    activePaths = None
    return globalTree, activePaths


##########################################################################################################################

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

def cart_step_1_local(X, args_X, args_Y, CategoricalVariables, activePaths):
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

##########################################################################################################################
def cart_step_1_global():
    return 1

##########################################################################################################################

def node_computations(dataFrame, colNames, activePath, className, CategoricalVariables, flag):
    if flag == "classNumbers":
        classNumbersJ = dict()
    elif flag == "statistics":
        statisticsJ = dict()

    for colName in colNames:
        df = dataFrame[[colName,className]]
        thresholds =  activePath["thresholdsJ"][colName]
        if flag == "classNumbers":
            countsLeft = [None]*len(thresholds)
            countsRight = [None]*len(thresholds)
        elif flag == "statistics":
            ssLeft = [None]*len(thresholds)
            ssRight = [None]*len(thresholds)
            nnLeft = [None]*len(thresholds)
            nnRight = [None]*len(thresholds)

        for i in xrange(len(thresholds)):
            if colName not in CategoricalVariables:
                dfLeft = df.loc[df[colName] <= thresholds[i]]
                dfRight = df.loc[df[colName] > thresholds[i]]
            elif colName in CategoricalVariables:
                dfLeft = df.loc[df[colName] in thresholds[i]['left']]
                dfRight = df.loc[df[colName] in thresholds[i]['right']]

            if flag == "classNumbers":
                countsLeft[i] = json.loads(dfLeft.groupby(className)[className].count().to_json())
                countsRight[i] = json.loads(dfRight.groupby(className)[className].count().to_json())
            elif flag == "statistics":
                ssLeft[i] = np.sum(dfLeft[className])
                ssRight[i] = np.sum(dfRight[className])
                nnLeft[i] = len(dfLeft[className])
                nnRight[i] = len(dfRight[className])

        if flag == "classNumbers":
            classNumbersJ[colName]= { "countsLeft" : countsLeft, "countsRight": countsRight }
        elif flag == "statistics":
            statisticsJ[colName] =  { "ssLeft" : ssLeft, "ssRight" : ssRight,  "nnLeft" : nnLeft, "nnRight" : nnRight}

    if flag == "classNumbers":
        return classNumbersJ
    if flag == "statistics":
        return statisticsJ


def cart_step_2_local(dataFrame, CategoricalVariables, args_X, args_Y, activePaths):
    # Run algorithm local iteration step
    for key in activePaths:
        df = dataFrame
        # For each unfinished path, find the subset of dataFrame (df)
        for i in xrange(len(activePaths[key]['filter'])):
            df = DataFrameFilter(df, activePaths[key]['filter'][i]["variable"],
                                     activePaths[key]['filter'][i]["operator"],
                                     activePaths[key]['filter'][i]["value"])
        if  args_Y[0] in CategoricalVariables:  #Classification Algorithm
            resultJ = node_computations(df, args_X, activePaths[key],  args_Y[0], CategoricalVariables,"classNumbers")
            activePaths[key]["classNumbersJ"] = dict(activePaths[key]["classNumbersJ"].items() + resultJ.items())
        elif  args_Y[0] not in CategoricalVariables: # Regression Algorithm
            resultJ = node_computations(df, args_X, activePaths[key],  args_Y[0],  CategoricalVariables,"statistics")
            activePaths[key]["statisticsJ"] = dict(activePaths[key]["statisticsJ"].items() + resultJ.items())
    return activePaths

##########################################################################################################################
def cart_step_2_global(args_X, args_Y,CategoricalVariables, activePaths):
    if  args_Y[0] not in CategoricalVariables: # Regression Algorithm
        for key in activePaths:
            activePaths[key]["statisticsJ"]["parentNode"]["mean_argsY"] = activePaths[key]["statisticsJ"]["parentNode"]["ss_argsY"]/ activePaths[key]["statisticsJ"]["parentNode"]["nn_argsY"]
            for colName in args_X:
                activePaths[key]["statisticsJ"][colName]["meanLeft"] =  [i / j if j != 0 else None for i, j in zip(activePaths[key]["statisticsJ"][colName]["ssLeft"] , activePaths[key]["statisticsJ"][colName]["nnLeft"])]
                activePaths[key]["statisticsJ"][colName]["meanRight"] = [i / j if j != 0 else None for i, j in zip(activePaths[key]["statisticsJ"][colName]["ssRight"], activePaths[key]["statisticsJ"][colName]["nnRight"])]
    return activePaths

##########################################################################################################################

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

def cart_step_3_local(dataFrame, args_X, args_Y, CategoricalVariables, activePaths):
    for key in activePaths:
        df = dataFrame
        # For each unfinished path, find the subset of dataFrame (df)
        for i in xrange(len(activePaths[key]['filter'])):
            df = DataFrameFilter(df, activePaths[key]['filter'][i]["variable"],
                                     activePaths[key]['filter'][i]["operator"],
                                     activePaths[key]['filter'][i]["value"])

        if args_Y[0] not in CategoricalVariables: # Regression Algorithm
            activePaths[key]["statisticsJ"]["parentNode"]["mse"] = np.sum((df[args_Y[0]] - activePaths[key]["statisticsJ"]["parentNode"]["mean_argsY"])**2 )
            mystat = compute_statistics2_in_the_node(df, args_X, activePaths[key],args_Y[0], CategoricalVariables)
            for colName in local_state['args_X']:
                activePaths[key]["statisticsJ"][colName]["mseLeft"] = mystat[colName]["mseLeft"]
                activePaths[key]["statisticsJ"][colName]["mseRight"] = mystat[colName]["mseRight"]
    return activePaths


##########################################################################################################################


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

def cart_step_3_global(args_X, args_Y, CategoricalVariables, globalTree , activePaths):
    if args_Y[0] not in CategoricalVariables: # Regression Algorithm
        criterion = "mse"
    elif args_Y[0] in CategoricalVariables: # Classification Algorithm
        criterion = "gini"

    activePathsNew = dict()
    no = 0
    for key in activePaths:
        if activePaths[key]["samples"] > PRIVACY_MAGIC_NUMBER and max([len(activePaths[key]['thresholdsJ'][i]) for i in args_X]) > 0 : # if activePaths[key]["samples"] > PRIVACY_MAGIC_NUMBER:
            bestColName, valueParentNode, bestValue, bestThreshold = best_splits(activePaths[key], args_X, args_Y[0], CategoricalVariables)

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
                 if args_Y[0] in CategoricalVariables: # Classification Algorithm
                     samplesPerClass = activePaths[key]["classNumbersJ"]["parentNode"]["counts"]
                 classValue = None
                 if args_Y[0] not in CategoricalVariables: # Regression Algorithm
                    classValue = activePaths[key]["statisticsJ"]["parentNode"]["ss_argsY"] /activePaths[key]["statisticsJ"]["parentNode"]["nn_argsY"]
                 if globalTree is None:
                     globalTree = Node(criterion, bestColName, valueParentNode, bestThreshold, activePaths[key]["samples"], samplesPerClass, classValue)
                     globalTreeJ = globalTree.tree_to_json()
                     #raise ValueError("AA", criterion, bestColName, valueParentNode, bestThreshold, activePaths[key]["samples"], samplesPerClass)
                 else:
                     globalTree.grow_tree(activePaths[key]['filter'], criterion, bestColName, valueParentNode, bestThreshold,activePaths[key]["samples"], samplesPerClass, classValue)
        else: #It is leaf -->TODO
            samplesPerClass = None
            if args_Y[0] in CategoricalVariables: # Classification Algorithm
                samplesPerClass = activePaths[key]["classNumbersJ"]["parentNode"]["counts"]
            classValue = None
            if args_Y[0] not in CategoricalVariables: # Regression Algorithm
                classValue = activePaths[key]["statisticsJ"]["parentNode"]["ss_argsY"] /activePaths[key]["statisticsJ"]["parentNode"]["nn_argsY"]
            globalTree.grow_tree(activePaths[key]['filter'], criterion, None, None, None,activePaths[key]["samples"], samplesPerClass, classValue) #Isws edw na dinw kai alla stoixeia. Alla logw privacy den xreiazetai
    activePaths = activePathsNew
    globalTreeJ = globalTree.tree_to_json()

    return globalTree, activePaths
    #if global_state['stepsNo'] ==1:
    #    raise ValueError(globalTreeJ)
    #print activePaths
    #print globalTreeJ
