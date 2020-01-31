from __future__ import division
from __future__ import print_function

import sys
from os import path
import numpy as np

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
from algorithm_utils import TransferData

SAMPLING_NUMBER = 1 #TODO: = PRIVACY_MAGIC_NUMBER

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
            for key in self.samplesPerClass:
                if myclass is None or self.samplesPerClass[key]> self.samplesPerClass[myclass]:
                    myclass = key
            samplesPerClass = self.samplesPerClass

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






#
# class CartIter3_Loc2Glob_TD(TransferData):
#     def __init__(self, *args):
#         if len(args) != 2:
#             raise ValueError('Illegal number of arguments.')
#         self.activePaths = args[0]
#         self.step = args[1] # 1 or 2 or 3
#
#     def get_data(self):
#         return self.activePaths
#
#     def __add__(self, other):
#         #run ./FINALME2.py
#         A1 = self.activePaths
#         A2 = other.activePaths
#
#         activePathsNew = dict()
#         for no in A1:
#             activePathsNew[no] = dict()
#
#             #1. ADD ["filter"]
#             activePathsNew[no]["filter"] = A1[no]["filter"]
#             if self.step == 1:
#                 #2. ADD ["thresholds"]
#                 activePathsNew[no]["thresholdsJ"] = dict()
#                 for key in A1[no]["thresholdsJ"]:
#                     activePathsNew[no]["thresholdsJ"][key] = list(set(A1[no]["thresholdsJ"][key] + A2[no]["thresholdsJ"][key])) #keep unique values
#                     activePathsNew[no]["thresholdsJ"][key].sort()
#                     #activePathsNew[no]["thresholdsJ"][key] = sorted(np.unique(A1[no]["thresholdsJ"][key] + A2[no]["thresholdsJ"][key]))
#                 #3. ADD ["samples"]
#                 activePathsNew[no]["samples"] = A1[no]["samples"] + A2[no]["samples"]
#
#                 if "classNumbersJ" in A1[no] and "classNumbersJ" in A2[no]:
#                     #4. ADD ["classNumbersJ"]["parentNode"]
#                     activePathsNew[no]["classNumbersJ"] = dict()
#                     activePathsNew[no]["classNumbersJ"]["parentNode"] = dict()
#                     activePathsNew[no]["classNumbersJ"]["parentNode"]["counts"] = dict()
#                     for key in A1[no]["classNumbersJ"]["parentNode"]["counts"]:
#                         if key in A2[no]["classNumbersJ"]["parentNode"]["counts"]:
#                             activePathsNew[no]["classNumbersJ"]["parentNode"]["counts"][key] = A1[no]["classNumbersJ"]["parentNode"]["counts"][key] + A2[no]["classNumbersJ"]["parentNode"]["counts"][key]
#                         else:
#                             activePathsNew[no]["classNumbersJ"]["parentNode"]["counts"][key] = A1[no]["classNumbersJ"]["parentNode"]["counts"][key]
#                     for key in A2[no]["classNumbersJ"]["parentNode"]["counts"]:
#                         if key not in A1[no]["classNumbersJ"]["parentNode"]["counts"]:
#                             activePathsNew[no]["classNumbersJ"]["parentNode"]["counts"][key] = A2[no]["classNumbersJ"]["parentNode"]["counts"][key]
#             if self.step == 2:
#                 #5. ADD ["classNumbersJ"][key]["countsRight"], ["classNumbersJ"][key]["countsLeft"]
#                 for key in A1[no]["thresholdsJ"]:
#                     if key in A1[no]["classNumbersJ"]:
#                         activePathsNew[no]["classNumbersJ"][key] = dict()
#                         activePathsNew[no]["classNumbersJ"][key]["countsRight"] = list()
#                         activePathsNew[no]["classNumbersJ"][key]["countsLeft"] = list()
#                         for threshold in activePathsNew[no]["thresholdsJ"][key]:
#                             countsRight = dict()
#                             countsLeft = dict()
#                             index1 = -1
#                             index2 = -1
#                             if threshold in A1[no]["thresholdsJ"][key]:
#                                 index1 = A1[no]["thresholdsJ"][key].index(threshold)
#                             if threshold in A2[no]["thresholdsJ"][key]:
#                                 index2 = A2[no]["thresholdsJ"][key].index(threshold)
#                             if index1 != -1 and index2!= -1:
#                                 try:
#                                     CR = add_dict(A1[no]["classNumbersJ"][key]["countsRight"][index1], A2[no]["classNumbersJ"][key]["countsRight"][index2])
#                                     activePathsNew[no]["classNumbersJ"][key]["countsRight"].append(CR)
#                                 except:
#                                     raise ValueError(no, key, index1, index2, threshold,A1[no]["classNumbersJ"],A2[no]["classNumbersJ"])
#
#                                 CL = add_dict(A1[no]["classNumbersJ"][key]["countsLeft"][index1], A2[no]["classNumbersJ"][key]["countsLeft"][index2])
#                                 activePathsNew[no]["classNumbersJ"][key]["countsLeft"].append(CL)
#                             elif index1 !=-1  and index2 == -1:
#                                 activePathsNew[no]["classNumbersJ"][key]["countsRight"].append(A1[no]["classNumbersJ"][key]["countsRight"][index1])
#                                 activePathsNew[no]["classNumbersJ"][key]["countsLeft"].append(A1[no]["classNumbersJ"][key]["countsLeft"][index1])
#                             elif index1 ==-1  and index2 != -1:
#                                 activePathsNew[no]["classNumbersJ"][key]["countsRight"].append(A2[no]["classNumbersJ"][key]["countsRight"][index2])
#                                 activePathsNew[no]["classNumbersJ"][key]["countsLeft"].append(A2[no]["classNumbersJ"][key]["countsLeft"][index2])
#
#                 raise ValueError (activePathsNew,A1[0]["thresholdsJ"][key],A2[0]["thresholdsJ"][key])
#         return CartIter_Loc2Glob_TD(activePathsNew)
