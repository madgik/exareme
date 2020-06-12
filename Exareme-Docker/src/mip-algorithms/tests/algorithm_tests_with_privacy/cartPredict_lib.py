
from __future__ import division
from __future__ import print_function

import sys
from os import path
import pandas as pd
import numpy as np
import math
import json
import logging
import itertools
import csv

#sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
sys.path.append('../../utils/')
from algorithm_utils import TransferData, PRIVACY_MAGIC_NUMBER

LOGS = True

def totabulardataresourceformat(name, data, fields):
    # Tabular data resource summary 2
    result = {
        "type": "application/vnd.dataresource+json",
        "data":
            {
                "name"   : name,
                "profile": "tabular-data-resource",
                "data"   : data,
                "schema" : {
                    "fields": fields
                }
            }
    }
    return result

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

def add_vals(a,b):
    if a == None and b == None:
        return None
    else:
        return(a or 0 ) +( b or 0)

def predict(node, row, args_Y, isClassificationTree):
    if (isinstance(node['right'],dict) == False and isinstance(node['left'],dict) == False) or \
       (isinstance(node['left'],dict) == False and row[node['colName']] <= node['threshold']) or \
       (isinstance(node['right'],dict) == False and row[node['colName']] > node['threshold']):
        if isClassificationTree:
            return node['class']
        else:
            return node['classValue']
    elif row[node['colName']] <= node['threshold']:
        return predict(node['left'], row,args_Y,isClassificationTree)
    else:
        return predict(node['right'], row,args_Y,isClassificationTree)

def cart_1_local(dataFrame, dataSchema, categoricalVariables, args_X, args_Y, globalTreeJ):
    #1. Delete null values from DataFrame
    dataFrame = dataFrame.dropna()
    for x in dataSchema:
        if x in categoricalVariables:
            dataFrame = dataFrame[dataFrame[x].astype(bool)]

    #2. Check privacy
    counts = len(dataFrame)
    if counts < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('The Experiment could not run with the input provided because there are insufficient data.')

    confusionMatrix = dict() # ConfusionMatrix['ActualValue', 'PredictedValue'] = ...
    mse= 0 # mean square error

    predictions = dict()
    predictions["ids"] = []
    predictions["predictions"] = []
    if args_Y[0] in categoricalVariables:  #case of Classification tree
        for element in itertools.product(categoricalVariables[args_Y[0]],categoricalVariables[args_Y[0]]):
            confusionMatrix[element[0],element[1]] = 0
        for index, row in dataFrame.iterrows():
            predictedValue = predict(globalTreeJ, row, args_Y, True)
            if "IdForTesting" in dataSchema:
                predictions["ids"].append(row["IdForTesting"])
                predictions["predictions"].append(predictedValue)
            logging.debug(["Predictions", row, predictedValue])
            confusionMatrix[row[args_Y[0]],predictedValue] = confusionMatrix[row[args_Y[0]],predictedValue] + 1
    elif args_Y[0] not in categoricalVariables: #case of regression tree
        for index, row in dataFrame.iterrows():
            predictedValue = predict(globalTreeJ, row, args_Y, False)
            if "IdForTesting" in dataSchema:
                predictions["ids"].append(row["IdForTesting"])
                predictions["predictions"].append(predictedValue)

            logging.debug(["Predictions", row, predictedValue])
            mse = mse + (row[args_Y[0]] - predictedValue) ** 2


    return confusionMatrix, mse, counts, predictions


def cart_1_global(args_X, args_Y, categoricalVariables, confusionMatrix, mse, counts, predictions):
    if args_Y[0] in categoricalVariables:
        fields = [{"name": "Actual Value", "type": "text"},{"name": "Predicted Value", "type": "text"},{"name": "Counts", "type": "number"}]
        table = totabulardataresourceformat("Confusion Matrix", [[key[0], key[1], confusionMatrix[key]] for key in confusionMatrix], fields)
        predictions = {
            "type": "application/json",
            "data":  predictions
        }
        result = {"result": [ table, predictions ]}

    if args_Y[0] not in categoricalVariables:  #case of Classification tree
        mse = np.sqrt(mse / counts)
        fields = [{"name": "mse", "type": "number"}]
        table = totabulardataresourceformat("Mean Square Error", [mse] , fields)
        predictions = {
            "type": "application/json",
            "data":  predictions
        }
        result = {"result": [ table, predictions ]}
    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        raise ValueError('Result contains NaNs.')
    return global_out


class Cart_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 7:
            raise ValueError('Illegal number of arguments.')
        self.args_X = args[0]
        self.args_Y = args[1]
        self.categoricalVariables = args[2]
        self.confusionMatrix = args[3]
        self.mse = args[4]
        self.counts = args[5]
        self.predictions = args[6]

    def get_data(self):
        return self.args_X, self.args_Y, self.categoricalVariables, self.confusionMatrix, self.mse, self.counts, self.predictions

    def __add__(self, other):

        predictionsTotal = dict()
        predictionsTotal["ids"] = self.predictions["ids"] + other.predictions["ids"]
        predictionsTotal["predictions"] = self.predictions["predictions"] + other.predictions["predictions"]
        
        return Cart_Loc2Glob_TD(
            self.args_X,
            self.args_Y,
            self.categoricalVariables,
            add_dict(self.confusionMatrix,other.confusionMatrix),
            add_vals(self.mse, other.mse),
            add_vals(self.counts, other.counts),
            predictionsTotal
        )
