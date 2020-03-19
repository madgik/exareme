from __future__ import division
from __future__ import print_function

import sys
from os import path
import numpy as np
import pandas as pd
import json
import itertools

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
from algorithm_utils import TransferData, PRIVACY_MAGIC_NUMBER

def add_vals(a,b):
    if a == None and b == None:
        return None
    else:
        return(a or 0 ) +( b or 0)

def DataFrameFilter(df, key, operator, value):
    if operator == '==':
        return df[df[key] == value]
    elif operator == '>':
        return df[df[key] > value]
    elif operator == '<':
        return df[df[key] < value]
    elif operator == '>=':
        return df[df[key] >= value]
    elif operator == '<=':
        return df[df[key] <= value]
    #else:
    #    raise ExaremeError('DataFrameFilter: Wrong Operator')

def add_dict(dict1,dict2):
    resultdict = dict()
    for key in dict1:
        if key in dict2:
            resultdict [key] = dict1[key] + dict2[key]
        else:
            resultdict [key] = dict1[key]
    for key in dict2:
        if key not in dict1:
            resultdict [key] = dict2[key]
    return resultdict


class Node:
    def __init__(self, criterion, colName,  gain, threshold, samples, samplesPerClass, classValue):
        self.criterion =  criterion
        self.colName = colName
        self.gain = gain
        self.threshold = threshold
        self.samples =  samples
        self.samplesPerClass = samplesPerClass
        self.classValue = classValue
        self.left = None
        self.right = None


    def tree_to_json(self):
    #TODO sto tree_to_json na mhn kanw return to samplesPerClass logw privacy
        #raise ValueError(self.criterion,self.gain,self.colName,self.threshold,self.samples,self.samplesPerClass,self.left,self.right )
        rightvalue = None
        if self.right is not None:
            rightvalue = self.right.tree_to_json()
        leftvalue = None
        if self.left is not None:
            leftvalue = self.left.tree_to_json()

        myclass = None
        samplesPerClass = None
        if self.criterion == "gini":
            samplesPerClass = dict()
            for key in self.samplesPerClass:
                if myclass is None or self.samplesPerClass[key]> self.samplesPerClass[myclass]:
                    myclass = str(key)
                samplesPerClass[str(key)] = self.samplesPerClass[key]

        myclassValue = None
        if self.criterion == "mse":
            myclassValue = self.classValue

        if self.gain == 0 :
            return { "criterion" :self.criterion,
                     "gain" :self.gain,
                     "samples" :self.samples,
                     "samplesPerClass" : samplesPerClass,
                     "classValue" : myclassValue,
                     "class" : myclass,
                     "right" : rightvalue,
                     "left" : leftvalue }
        else:
            return { "colName" : self.colName,
                     "threshold" : self.threshold,
                     "gain" :self.gain,
                     "criterion" :self.criterion,
                     "samples" :self.samples,
                     "samplesPerClass": samplesPerClass,
                     "classValue" : myclassValue,
                     "class" : myclass,
                     "right" : rightvalue,
                     "left" : leftvalue }

    def grow_tree(self, path, criterion, bestColName, parentNodeGain, bestThreshold, samples, samplesPerClass, classValue):
        if len(path) > 0:
            if path[0]["operator"] == '<=':
                if self.left == None:
                    self.left = Node(criterion, bestColName, parentNodeGain, bestThreshold, samples,samplesPerClass, classValue)
                else:
                    self.left.grow_tree(path[1:],criterion, bestColName, parentNodeGain, bestThreshold, samples, samplesPerClass, classValue)
            if path[0]["operator"] =='>':
                if self.right == None:
                    self.right = Node(criterion, bestColName, parentNodeGain, bestThreshold, samples,samplesPerClass, classValue)
                else:
                    self.right.grow_tree(path[1:],criterion, bestColName, parentNodeGain, bestThreshold, samples, samplesPerClass, classValue)


class CartInit_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 3:
            raise ValueError('Illegal number of arguments.')
        self.args_X = args[0]
        self.args_Y = args[1]
        self.CategoricalVariables = args[2]

    def get_data(self):
        return self.args_X, self.args_Y, self.CategoricalVariables

    def __add__(self, other):
        return CartInit_Loc2Glob_TD(
                self.args_X,
                self.args_Y,
                self.CategoricalVariables
        )


class Cart_Glob2Loc_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 2:
            raise ValueError('Illegal number of arguments.')
        self.globalTree = args[0]
        self.activePaths = args[1]

    def get_data(self):
        return self.globalTree, self.activePaths


class CartIter1_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        self.activePaths = args[0]

    def get_data(self):
        return self.activePaths

    def __add__(self, other):
        #run ./FINALME2.py
        A1 = self.activePaths
        A2 = other.activePaths

        activePathsNew = dict()
        for no in A1:
            activePathsNew[no] = dict()

            #1. ADD ["filter"]
            activePathsNew[no]["filter"] = A1[no]["filter"]

            #2. ADD ["thresholds"]
            activePathsNew[no]["thresholdsJ"] = dict()
            for key in A1[no]["thresholdsJ"]:
                activePathsNew[no]["thresholdsJ"][key] = sorted(np.unique(A1[no]["thresholdsJ"][key] + A2[no]["thresholdsJ"][key]))

            #3. ADD ["samples"]
            activePathsNew[no]["samples"] = A1[no]["samples"] + A2[no]["samples"]

            #4. If args_Y[0] not in CategoricalVariables: ADD ["classNumbersJ"]["parentNode"]
            if "classNumbersJ" in A1[no] and "classNumbersJ" in A2[no]:
                activePathsNew[no]["classNumbersJ"] = dict()
                activePathsNew[no]["classNumbersJ"]["parentNode"] = dict()
                activePathsNew[no]["classNumbersJ"]["parentNode"]["counts"] = dict()
                for key in A1[no]["classNumbersJ"]["parentNode"]["counts"]:
                    if key in A2[no]["classNumbersJ"]["parentNode"]["counts"]:
                        activePathsNew[no]["classNumbersJ"]["parentNode"]["counts"][key] = A1[no]["classNumbersJ"]["parentNode"]["counts"][key] + A2[no]["classNumbersJ"]["parentNode"]["counts"][key]
                    else:
                        activePathsNew[no]["classNumbersJ"]["parentNode"]["counts"][key] = A1[no]["classNumbersJ"]["parentNode"]["counts"][key]
                for key in A2[no]["classNumbersJ"]["parentNode"]["counts"]:
                    if key not in A1[no]["classNumbersJ"]["parentNode"]["counts"]:
                        activePathsNew[no]["classNumbersJ"]["parentNode"]["counts"][key] = A2[no]["classNumbersJ"]["parentNode"]["counts"][key]
            if "statisticsJ" in A1[no] and "statisticsJ" in A2[no]:
                activePathsNew[no]["statisticsJ"] = dict()
                activePathsNew[no]["statisticsJ"]["parentNode"] = dict()
                activePathsNew[no]["statisticsJ"]["parentNode"]["ss_argsY"] = A1[no]["statisticsJ"]["parentNode"]["ss_argsY"] + A2[no]["statisticsJ"]["parentNode"]["ss_argsY"]
                activePathsNew[no]["statisticsJ"]["parentNode"]["nn_argsY"] = A1[no]["statisticsJ"]["parentNode"]["nn_argsY"] + A2[no]["statisticsJ"]["parentNode"]["nn_argsY"]


                #TODO: ADD {"ss_argsY" : np.sum(X[args_Y[0]]), "nn_argsY" : X.shape[0] }

        return CartIter1_Loc2Glob_TD(activePathsNew)



class CartIter2_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        self.activePaths = args[0]

    def get_data(self):
        return self.activePaths

    def __add__(self, other):

        #run ./FINALME2.py
        A1 = self.activePaths
        A2 = other.activePaths

        activePathsNew = dict()
        for no in A1:
            activePathsNew[no] = dict()

            #1. ADD ["filter"],["thresholds"],["samples"]
            activePathsNew[no]["filter"] = A1[no]["filter"]
            activePathsNew[no]["thresholdsJ"] = A1[no]["thresholdsJ"]
            activePathsNew[no]["samples"] = A1[no]["samples"]

            #2. ADD ["classNumbersJ"]["parentNode"]
            if "classNumbersJ" in A1[no] and "classNumbersJ" in A2[no]:
                activePathsNew[no]["classNumbersJ"] = dict()
                activePathsNew[no]["classNumbersJ"]["parentNode"] = A1[no]["classNumbersJ"]["parentNode"]
            if "statisticsJ" in A1[no] and "statisticsJ" in A2[no]:
                activePathsNew[no]["statisticsJ"]  = dict()
                activePathsNew[no]["statisticsJ"]["parentNode"] = A1[no]["statisticsJ"]["parentNode"]

            #3.  ADD ["classNumbersJ"][key] or ["statisticsJ"][key]
            for key in A1[no]["thresholdsJ"]:
                if "classNumbersJ" in A1[no] and "classNumbersJ" in A2[no]  :
                    if key in A1[no]["classNumbersJ"]:
                        activePathsNew[no]["classNumbersJ"][key] = dict()
                        activePathsNew[no]["classNumbersJ"][key]["countsRight"] = list()
                        activePathsNew[no]["classNumbersJ"][key]["countsLeft"] = list()
                        for i in xrange(len(activePathsNew[no]["thresholdsJ"][key])):
                            activePathsNew[no]["classNumbersJ"][key]["countsRight"].append(add_dict(A1[no]["classNumbersJ"][key]["countsRight"][i], A2[no]["classNumbersJ"][key]["countsRight"][i]))
                            activePathsNew[no]["classNumbersJ"][key]["countsLeft"].append(add_dict(A1[no]["classNumbersJ"][key]["countsLeft"][i], A2[no]["classNumbersJ"][key]["countsLeft"][i]))
                if "statisticsJ" in A1[no] and "statisticsJ" in A2[no]:
                    if key in A1[no]["statisticsJ"]:
                        activePathsNew[no]["statisticsJ"][key] = dict()
                        activePathsNew[no]["statisticsJ"][key]["ssLeft"] = [add_vals(A1[no]["statisticsJ"][key]["ssLeft"][i], A2[no]["statisticsJ"][key]["ssLeft"][i]) for i in xrange(len(A1[no]["statisticsJ"][key]["ssLeft"]))]
                        activePathsNew[no]["statisticsJ"][key]["ssRight"] =[add_vals(A1[no]["statisticsJ"][key]["ssRight"][i], A2[no]["statisticsJ"][key]["ssRight"][i]) for i in xrange(len(A1[no]["statisticsJ"][key]["ssRight"]))]
                        activePathsNew[no]["statisticsJ"][key]["nnLeft"] = [add_vals(A1[no]["statisticsJ"][key]["nnLeft"][i], A2[no]["statisticsJ"][key]["nnLeft"][i]) for i in xrange(len(A1[no]["statisticsJ"][key]["nnLeft"]))]
                        activePathsNew[no]["statisticsJ"][key]["nnRight"] = [add_vals(A1[no]["statisticsJ"][key]["nnRight"][i], A2[no]["statisticsJ"][key]["nnRight"][i]) for i in xrange(len(A1[no]["statisticsJ"][key]["nnRight"]))]
        return CartIter2_Loc2Glob_TD(activePathsNew)



class CartIter3_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        self.activePaths = args[0]

    def get_data(self):
        return self.activePaths

    def __add__(self, other):
        #run ./FINALME2.py
        A1 = self.activePaths
        A2 = other.activePaths

        activePathsNew = dict()
        for no in A1:
            activePathsNew[no] = dict()

            #1. ADD ["filter"],["thresholds"],["samples"]
            activePathsNew[no]["filter"] = A1[no]["filter"]
            activePathsNew[no]["thresholdsJ"] = A1[no]["thresholdsJ"]
            activePathsNew[no]["samples"] = A1[no]["samples"]

            #2. ADD ["classNumbersJ"]
            if "classNumbersJ" in A1[no] and "classNumbersJ" in A2[no]:
                activePathsNew[no]["classNumbersJ"] = A1[no]["classNumbersJ"]

            if "statisticsJ" in A1[no] and "statisticsJ" in A2[no]:
                activePathsNew[no]["statisticsJ"]  = dict()
                activePathsNew[no]["statisticsJ"]["parentNode"] = A1[no]["statisticsJ"]["parentNode"] #TODO:  Einai swsto? Ti alla exei?
                activePathsNew[no]["statisticsJ"]["parentNode"]["mse"] = add_vals(A1[no]["statisticsJ"]["parentNode"]["mse"], A2[no]["statisticsJ"]["parentNode"]["mse"])

            #3.  ADD ["classNumbersJ"][key] or ["statisticsJ"][key]
            for key in A1[no]["thresholdsJ"]:
                if "statisticsJ" in A1[no] and "statisticsJ" in A2[no]:
                    if key in A1[no]["statisticsJ"]:
                        activePathsNew[no]["statisticsJ"][key] = dict()
                        activePathsNew[no]["statisticsJ"][key]["ssLeft"] = A1[no]["statisticsJ"][key]["ssLeft"]
                        activePathsNew[no]["statisticsJ"][key]["ssRight"] = A1[no]["statisticsJ"][key]["ssLeft"]
                        activePathsNew[no]["statisticsJ"][key]["nnLeft"] = A1[no]["statisticsJ"][key]["ssLeft"]
                        activePathsNew[no]["statisticsJ"][key]["nnRight"] = A1[no]["statisticsJ"][key]["ssLeft"]
                        activePathsNew[no]["statisticsJ"][key]["meanLeft"]= A1[no]["statisticsJ"][key]["meanLeft"]
                        activePathsNew[no]["statisticsJ"][key]["meanRight"]= A1[no]["statisticsJ"][key]["meanRight"]

                        activePathsNew[no]["statisticsJ"][key]["mseLeft"] = [add_vals(A1[no]["statisticsJ"][key]["mseLeft"][i], A2[no]["statisticsJ"][key]["mseLeft"][i]) for i in xrange(len(A1[no]["statisticsJ"][key]["mseLeft"]))]
                        activePathsNew[no]["statisticsJ"][key]["mseRight"] = [add_vals(A1[no]["statisticsJ"][key]["mseRight"][i], A2[no]["statisticsJ"][key]["mseRight"][i]) for i in xrange(len(A1[no]["statisticsJ"][key]["mseRight"]))]


        return CartIter3_Loc2Glob_TD(activePathsNew)



##########################################################################################################################

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
            for i in range(0, nn-1, PRIVACY_MAGIC_NUMBER):
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
            for colName in args_X:
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
                     #globalTreeJ = globalTree.tree_to_json()
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
    #globalTreeJ = globalTree.tree_to_json()

    return globalTree, activePaths
    #if global_state['stepsNo'] ==1:
    #    raise ValueError(globalTreeJ)
    #print activePaths
    #print globalTreeJ
