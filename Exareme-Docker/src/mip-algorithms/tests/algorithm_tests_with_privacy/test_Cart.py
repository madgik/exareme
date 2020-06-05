import requests
import unittest
import os,sys
import json
import logging
import math
from scipy.stats import binom_test
from decimal import *
import csv

import pandas as pd
from sklearn import tree
from sklearn.tree import export_graphviz
from sklearn.tree import DecisionTreeClassifier
import itertools
import numpy as np
from sklearn import metrics
#from sklearn.model_selection import train_test_split

from scipy import stats

from os import path
sys.path.append(path.abspath(__file__))

from tests import vm_url
endpointUrl_CartTraining= vm_url + 'CART'
endpointUrl_CartPredict= vm_url + 'CART_PREDICT'
path = '../data/dementia/'
sys.path.append('../../CART_PREDICT/')
from cartPredict_lib import cart_1_local, cart_1_global

# argsX = "montrealcognitiveassessment,"
# argsX +="rightpcggposteriorcingulategyrus,leftpcggposteriorcingulategyrus,"
# argsX +="rightacgganteriorcingulategyrus,"
# argsX +="leftmcggmiddlecingulategyrus,"
# argsX +="rightphgparahippocampalgyrus,"
# argsX +="rightententorhinalarea,"
# argsX +="righthippocampus,lefthippocampus,"
# argsX+= "rightthalamusproper,leftthalamusproper"

def run_sklearn_classification(trainingDatasetPath, PredictionDatasetPath, argsX, argsY, maxDepth):
    #1. Read training data
    df = pd.read_csv(trainingDatasetPath)
    args_X = list(argsX.replace(" ", "").split(","))
    args_Y = [argsY.replace(" ", "")][0]
    varNames = [x for x in args_X]
    varNames.append(args_Y)
    df = df[varNames]
    df = df.dropna()
    X = df.drop(args_Y, axis=1)
    y = df[args_Y]

    #2. Run scikit tree classifier
    clf = DecisionTreeClassifier(random_state=0, max_depth = int(maxDepth))
    clf = clf.fit(X, y)

    #3. Read testing dataset
    df = pd.read_csv(PredictionDatasetPath)
    varNames.append("IdForTesting")
    df = df[varNames]

    df = df.dropna()
    X = df.drop(args_Y, axis=1)
    y = df[args_Y]

    #4. Run scikit predictor
    mydata = []
    ydata = []
    for ind in df.index:
        rowNew =[df[x][ind] for x in args_X]
        mydata.append(rowNew)
        ydata.append(y[ind])
    predictedValues=list(clf.predict(mydata))
    df["goldStandardImplementationResult"] = predictedValues

    return df

def run_sklearn_regression(trainingDatasetPath, PredictionDatasetPath, argsX, argsY, maxDepth):
    #1. Read training dataset
    df = pd.read_csv(trainingDatasetPath)#, index_col ="subjectcode")
    args_X = list(argsX.replace(" ", "").split(","))
    args_Y = [argsY.replace(" ", "")][0]
    varNames = [x for x in args_X]
    varNames.append(args_Y)
    df = df[varNames]
    df = df.dropna()
    X = df.drop(args_Y, axis=1)
    y = df[args_Y]

    #2. Fit regression model
    regr = tree.DecisionTreeRegressor(random_state=0, max_depth = int(maxDepth))
    regr = regr.fit(X, y)

    #3. Read testing dataset
    df = pd.read_csv(PredictionDatasetPath)#, index_col ="subjectcode")
    varNames.append("IdForTesting")
    df = df[varNames]

    df = df.dropna()
    X = df.drop(args_Y, axis=1)
    y = df[args_Y]

    #4. Predict
    mydata = []
    ydata = []
    for ind in df.index:
        rowNew =[df[x][ind] for x in args_X]
        mydata.append(rowNew)
        ydata.append(y[ind])
    predictedValues=list(regr.predict(mydata))
    df["goldStandardImplementationResult"] = predictedValues

    return df


#PredictionDatasetPath, argsX, argsY, args_globalTreeJ, CategoricalVariables = path+"diabetes_testing.csv", data_CartPredict[0]['value'], data_CartPredict[1]['value'],exaremeResult_Training, {'Outcome': [u'0', u'1']}
def ExaremePredictSimulation(PredictionDatasetPath, argsX, argsY, args_globalTreeJ, categoricalVariables):
    df = pd.read_csv(PredictionDatasetPath)#, index_col ="subjectcode")
    args_X = list(argsX.replace(" ", "").split(","))
    args_Y = [argsY.replace(" ", "")][0]
    varNames = [x for x in args_X]
    varNames.append(args_Y)
    df = df[varNames]
    df = df.dropna()
    X = df.drop(args_Y, axis=1)
    y = df[args_Y]
    #CategoricalVariables =  {'Outcome': [u'0', u'1']}
    dataSchema = varNames
    if args_Y in categoricalVariables:
        df[args_Y] = df[args_Y].astype(str)
    confusionMatrix, mse, counts, predictions = cart_1_local(df, dataSchema, categoricalVariables, args_X, [args_Y], args_globalTreeJ)
    global_out = cart_1_global(args_X, args_Y, categoricalVariables, confusionMatrix, mse, counts, predictions)
    return global_out

class Test_Cart(unittest.TestCase):
    def test_Cart_1(self):
        #Run Exareme Cart Training Algorithm
        logging.info("---------- TEST 1: CART - classification tree on  diabetes")
        data_Cart = [{ "name": "x", "value": "Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age"},
                {"name": "y", "value": "Outcome"},
                {"name": "max_depth", "value": "200"},
                {"name": "no_split_points","value": "50"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "diabetes_training"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl_CartTraining,data=json.dumps(data_Cart),headers=headers)
        exaremeResult_Training = json.loads(r.text)
        print ("exaremeResult_Training", r.text)

        #########################'##########################################
        # #Run Exareme Cart Prediction Algorithm
        data_CartPredict = [{ "name": "x", "value": "Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,IdForTesting"},
                 {"name": "y", "value":  "Outcome"},
                 {"name": "treeJson", "value": ""},
                 {"name": "treeFile", "value": "tree.txt"},
                 {"name": "pathology","value":"dementia"},
                 {"name": "dataset", "value": "diabetes_testing"},
                 {"name": "filter", "value": ""}]
        # headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        # r = requests.post(endpointUrl_CartPredict,data=json.dumps(data_CartPredict),headers=headers)
        # exaremeResult_Predict = json.loads(r.text)
        r = ExaremePredictSimulation(path+"diabetes_testing.csv", data_CartPredict[0]['value'], data_CartPredict[1]['value'],exaremeResult_Training['result'][0]['data'], {'Outcome': [u'0', u'1']})
        exaremeResult_Predict = json.loads(r)
        print (exaremeResult_Predict)
        ####################################################################
        #Run Python
        data_sklearn = {"trainingDataset": "diabetes_training",
                        "testingDataset": "diabetes_testing",
                        "x": "Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age",
                        "y": "Outcome",
                        "max_depth": 200}

        sklearnResult = run_sklearn_classification( path+data_sklearn['trainingDataset']+".csv",
                                                    path+data_sklearn['testingDataset']+".csv",
                                                    data_sklearn['x'], data_sklearn['y'], data_sklearn['max_depth'])

        #CompareResults
        exaremeResult = pd.DataFrame.from_dict(exaremeResult_Predict["result"][1]["data"])
        joinTbls = pd.merge(exaremeResult, sklearnResult, left_on = "ids", right_on = "IdForTesting")

        TT = TF = FT = FF = 0
        for ind in joinTbls.index:
            if int(joinTbls['predictions'][ind]) == joinTbls[data_sklearn['y']][ind] : #MIP correct
                if joinTbls['goldStandardImplementationResult'][ind] == joinTbls[data_sklearn['y']][ind]: #GS correct
                    TT = TT + 1
                else: TF =TF + 1
            else: #MIP not correct
                if joinTbls['goldStandardImplementationResult'][ind] == joinTbls[data_sklearn['y']][ind]: #GS correct
                    FT = FT + 1
                else: FF = FF + 1
        print (TT,TF,FT,FF)
        p_value = stats.binom_test(x=FT, n=(TF+FT), p=0.5, alternative="greater")
        if p_value > 0.05 :
            print (p_value)
            assert (1)
        else:
            print (p_value)
            assert (0)


    def test_Cart_2(self):
        logging.info("---------- TEST 2: CART - regression tree with real variables -  Training and prediction applied on the same dataset")
        data_Cart = [{ "name": "x", "value": "Cement,BlastFurnaceSlag,FlyAsh,Water,Superplasticizer,CoarseAggregate,FineAggregate,Age"},
                {"name": "y", "value": "ConcreteCompressiveStrength"},
                {"name": "max_depth", "value": "200"},
                {"name": "no_split_points","value": "20"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "concrete_data_training"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl_CartTraining,data=json.dumps(data_Cart),headers=headers)
        exaremeResult_Training = json.loads(r.text)
        print ("exaremeResult_Training", r.text)

        ####################################################################
        #Run Exareme Cart Prediction Algorithm
        data_CartPredict = [{ "name": "x", "value":"Cement,BlastFurnaceSlag,FlyAsh,Water,Superplasticizer,CoarseAggregate,FineAggregate,Age,IdForTesting"},
                {"name": "y", "value": "ConcreteCompressiveStrength"},
                {"name": "treeJson", "value": ""},
                {"name": "treeFile", "value": "tree.txt"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "concrete_data_testing"},
                {"name": "filter", "value": ""}]
        #headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        #r = requests.post(endpointUrl_CartPredict,data=json.dumps(data_CartPredict),headers=headers)
        #print ("ExaremeResult", r.text)
        #exaremeResult_Predict = json.loads(r.text)
        r = ExaremePredictSimulation(path+"concrete_data_testing.csv", data_CartPredict[0]['value'], data_CartPredict[1]['value'],exaremeResult_Training['result'][0]['data'], {})
        exaremeResult_Predict = json.loads(r)
        print (exaremeResult_Predict)
        ####################################################################
        #Run Python
        data_sklearn = {"trainingDataset": "concrete_data_training",
                        "testingDataset": "concrete_data_testing",
                        "x": "Cement,BlastFurnaceSlag,FlyAsh,Water,Superplasticizer,CoarseAggregate,FineAggregate,Age",
                        "y":"ConcreteCompressiveStrength",
                        "max_depth": 200}

        sklearnResult = run_sklearn_regression(path+data_sklearn['trainingDataset']+".csv",
                                              path+data_sklearn['testingDataset']+".csv",
                                              data_sklearn['x'], data_sklearn['y'], data_sklearn['max_depth'])

        #CompareResults
        exaremeResult = pd.DataFrame.from_dict(exaremeResult_Predict["result"][1]["data"])
        joinTbls = pd.merge(exaremeResult, sklearnResult, left_on = "ids", right_on = "IdForTesting")

        mseMIP = []
        mseGS = []
        for ind in joinTbls.index:
            mseMIP.append((joinTbls[data_sklearn['y']][ind] - joinTbls['predictions'][ind]) ** 2)
            mseGS.append((joinTbls[data_sklearn['y']][ind] - joinTbls['goldStandardImplementationResult'][ind]) ** 2)

        statistic, p_value = stats.wilcoxon(mseMIP,mseGS, zero_method = "wilcox" , correction =True, alternative = 'greater')
        if  p_value > 0.05 :
            print (p_value)
            assert (1)
        else:
            print (p_value)
            assert (0)
