#https://towardsdatascience.com/random-forests-and-decision-trees-from-scratch-in-python-3e4fa5ae4249

#https://www.google.com/search?client=ubuntu&hs=oI&channel=fs&sxsrf=ALeKk02nE8-HJDupKHMW8ccD6O4FQQQkww%3A1582207015508&ei=J5BOXv7QHqaHmwXv57LoCg&q=random+forest+from+scratch+code&oq=random+forest+from+scratch+code&gs_l=psy-ab.3..0i22i30l2.4918.13259..13364...4.2..0.194.3649.1j31......0....1..gws-wiz.......0i71j35i39j35i39i19j0i67j0j0i131j0i203.hdNunVc4J8A&ved=0ahUKEwi-gPu4pODnAhWmw6YKHe-zDK0Q4dUDCAo&uact=5
#https://www.datacamp.com/community/tutorials/random-forests-classifier-python
#https://stackabuse.com/random-forest-algorithm-with-python-and-scikit-learn/
#https://rstudio-pubs-static.s3.amazonaws.com/300604_3da1e726964d47a794d3323ffb41264d.html

from __future__ import division
from __future__ import print_function

import sys
from os import path
import pandas as pd
import numpy as np
import math
import logging

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/CART/' )

from algorithm_utils import init_logger,TransferData, PRIVACY_MAGIC_NUMBER
from cart_steps import cart_init_1_local, cart_step_1_local, cart_step_2_local, cart_step_2_global, cart_step_3_local, cart_step_3_global
from cart_lib import CartIter1_Loc2Glob_TD, CartIter2_Loc2Glob_TD, CartIter3_Loc2Glob_TD

class RFInit1_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 8:
            raise ValueError('Illegal number of arguments.')
        self.args_X = args[0]
        self.args_Y = args[1]
        self.args_n_trees = args[2]
        self.args_n_features = args[3]
        self.args_sample_percentage = args[4]
        self.args_max_depth = args[5]
        self.dataSchema = args[6]
        self.categoricalVariables = args[7]

    def get_data(self):
        return self.args_X, self.args_Y, self.args_n_trees, self.args_n_features, self.args_sample_percentage, self.args_max_depth, self.dataSchema, self.categoricalVariables

    def __add__(self, other):
        return RFInit1_Loc2Glob_TD(
                self.args_X,
                self.args_Y,
                self.args_n_trees,
                self.args_n_features,
                self.args_sample_percentage,
                self.args_max_depth,
                self.dataSchema,
                self.categoricalVariables
        )

class RFInit1_Glob2Loc_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 2:
            raise ValueError('Illegal number of arguments.')
        self.rF_DataSchema = args[0]
        self.rF_categoricalVariables = args[1]

    def get_data(self):
        return self.rF_DataSchema, self.rF_categoricalVariables


class RFInit2_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        self.rF_Logs= args[0]

    def get_data(self):
        return self.rF_Logs

    def __add__(self, other):
        return RFInit2_Loc2Glob_TD(
            [dict(self.rF_Logs[i].items() + other.rF_Logs[i].items()) for i in range(len(self.rF_Logs))]
        )

class RF_Glob2Loc_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 3:
            raise ValueError('Illegal number of arguments.')
        self.rF_globalTree = args[0]
        self.rF_activePaths = args[1]
        self.rF_Logs = args[2]

    def get_data(self):
        return self.rF_globalTree, self.rF_activePaths, self.rF_Logs


class RFIter1_Loc2Glob_TD(TransferData):
    def __init__(self,*args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        A = args[0]
        self.rF_activePaths = [CartIter1_Loc2Glob_TD(A[i]) for i in range(len(A))]

    def get_data(self):
        return [self.rF_activePaths[i].get_data().get_data() for i in xrange(len(self.rF_activePaths))]

    def __add__(self, other):
        for i in xrange(len(self.rF_activePaths)):
            self.rF_activePaths[i].__add__(other.rF_activePaths[i])
        return RFIter1_Loc2Glob_TD(self.rF_activePaths)


class RFIter2_Loc2Glob_TD(TransferData):
    def __init__(self,*args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        A = args[0]
        self.rF_activePaths = [CartIter2_Loc2Glob_TD(A[i]) for i in range(len(A))]

    def get_data(self):
        return [self.rF_activePaths[i].get_data().get_data() for i in xrange(len(self.rF_activePaths))]

    def __add__(self, other):
        for i in xrange(len(self.rF_activePaths)):
            self.rF_activePaths[i].__add__(other.rF_activePaths[i])
        return RFIter2_Loc2Glob_TD(self.rF_activePaths)


class RFIter3_Loc2Glob_TD(TransferData):
    def __init__(self,*args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        A = args[0]
        self.rF_activePaths = [CartIter3_Loc2Glob_TD(A[i]) for i in range(len(A))]

    def get_data(self):
        return [self.rF_activePaths[i].get_data().get_data() for i in xrange(len(self.rF_activePaths))]

    def __add__(self, other):
        for i in xrange(len(self.rF_activePaths)):
            self.rF_activePaths[i].__add__(other.rF_activePaths[i])
        return RFIter3_Loc2Glob_TD(self.rF_activePaths)


###################################################################################################

def RF_init_1_global(args_X, args_Y, dataSchema, categoricalVariables, n_trees, n_features): # Select the features of trees
    rF_categoricalVariables = [None] * n_trees
    rF_DataSchema = [None] * n_trees
    for i in xrange(n_trees):
        rF_f_idxs  = np.append(np.random.permutation(len(args_X))[:n_features], dataSchema.index(args_Y[0]))
        rF_DataSchema[i] = [dataSchema[j] for j in rF_f_idxs]
        rF_categoricalVariables[i] = dict()
        for key, value in categoricalVariables.iteritems():
            if key in rF_DataSchema[i]:
                rF_categoricalVariables[i][key] = value
        #dict((key,value) for key, value in categoricalVariables.iteritems() if key in rF_DataSchema[i])
    return rF_DataSchema, rF_categoricalVariables

def RF_init_2_local(args_Y, dataFrame, sample_percentage, n_trees, rF_DataSchema, rF_categoricalVariables):
    rF_dataFrame = [None] * n_trees
    rF_Logs = [dict()] * n_trees
    for i in xrange(n_trees):
        sample_size = int(math.floor(len(dataFrame)*sample_percentage))
        idxs = np.random.permutation(len(dataFrame))[:sample_size]
        df = cart_init_1_local(dataFrame.loc[idxs,rF_DataSchema[i]], rF_DataSchema[i], rF_categoricalVariables[i])
        if len(df) < PRIVACY_MAGIC_NUMBER:
            rF_Logs[i]["PrivacyError"] = "The Experiment could not run with the input provided because there are insufficient data."
        else:
            rF_dataFrame[i] = df
    return rF_dataFrame, rF_Logs

def RF_init_2_global(n_trees):
    rF_globalTree = [None] * n_trees
    rF_activePaths = [None] * n_trees
    return rF_globalTree, rF_activePaths


def RF_step_1_local(args_Y, n_trees, rF_dataFrame, rF_DataSchema, rF_categoricalVariables, rF_activePaths, rF_Logs):
    for i in xrange(n_trees):
        if "PrivacyError" not in rF_Logs[i]:
            init_logger()
            logging.warning("RF_step_1_local: i, rF_dataFrame[i][:-1], args_Y, rF_categoricalVariables[i], rF_activePaths[i]")
            logging.warning([i, rF_dataFrame[i][:-1], args_Y, rF_categoricalVariables[i], rF_activePaths[i]])
            rF_activePaths[i] = cart_step_1_local(rF_dataFrame[i], rF_DataSchema[i][:-1], args_Y, rF_categoricalVariables[i], rF_activePaths[i])
    return rF_activePaths


def RF_step_2_local(args_Y, n_trees, rF_dataFrame, rF_DataSchema, rF_categoricalVariables, rF_activePaths, rF_Logs):
    for i in xrange(n_trees):
        if "PrivacyError" not in rF_Logs[i]:
            init_logger()
            logging.warning("RF_step_1_local: i, rF_dataFrame[i][:-1], args_Y, rF_categoricalVariables[i], rF_activePaths[i]")
            logging.warning([i, rF_dataFrame[i][:-1], args_Y, rF_categoricalVariables[i], rF_activePaths[i]])

            rF_activePaths[i] = cart_step_2_local(rF_dataFrame[i], rF_categoricalVariables[i], rF_DataSchema[i][:-1], args_Y, rF_activePaths[i])
    return rF_activePaths

def RF_step_2_global(args_Y, n_trees, rF_DataSchema, rF_categoricalVariables, rF_activePaths, rF_Logs):
    for i in xrange(n_trees):
        if "PrivacyError" not in rF_Logs[i]:
            init_logger()
            logging.warning("RF_step_2_global: i, rF_DataSchema[i][:-1], args_Y,  rF_categoricalVariables[i], rF_activePaths[i]")
            logging.warning([i, rF_DataSchema[i][:-1], args_Y,  rF_categoricalVariables[i], rF_activePaths[i]])
            rF_activePaths[i] = cart_step_2_global(rF_DataSchema[i][:-1], args_Y,  rF_categoricalVariables[i], rF_activePaths[i])
    return rF_activePaths


def RF_step_3_local(args_Y, n_trees, rF_dataFrame, rF_DataSchema, rF_categoricalVariables, rF_activePaths, rF_Logs):
    for i in xrange(n_trees):
        if "PrivacyError" not in rF_Logs[i]:
            init_logger()
            logging.warning("RF_step_1_local: i, rF_dataFrame[i][:-1], args_Y, rF_categoricalVariables[i], rF_activePaths[i]")
            logging.warning([i, rF_dataFrame[i][:-1], args_Y, rF_categoricalVariables[i], rF_activePaths[i]])
            rF_activePaths[i] = cart_step_3_local(rF_dataFrame[i], rF_DataSchema[i][:-1], args_Y, rF_categoricalVariables[i], rF_activePaths[i])
    return rF_activePaths


def RF_step_3_global(args_Y, n_trees, rF_DataSchema, rF_categoricalVariables, rF_activePaths, rF_globalTree, rF_Logs, stepsNo,args_max_depth ):
    for i in xrange(n_trees):
        if "PrivacyError" not in rF_Logs[i]:
            init_logger()
            logging.warning("RF_step_2_global: i, rF_DataSchema[i][:-1], args_Y,  rF_categoricalVariables[i], rF_activePaths[i]")
            logging.warning([i, rF_DataSchema[i][:-1], args_Y,  rF_categoricalVariables[i], rF_activePaths[i]])
            rF_globalTree[i], rF_activePaths[i] = cart_step_3_global(rF_DataSchema[i][:-1], args_Y,  rF_categoricalVariables[i], rF_globalTree[i], rF_activePaths[i])
        if bool(rF_activePaths[i]) == False or stepsNo > args_max_depth:
            rF_Logs[i]["STOP"] = 1
    return rF_globalTree, rF_activePaths, rF_Logs
