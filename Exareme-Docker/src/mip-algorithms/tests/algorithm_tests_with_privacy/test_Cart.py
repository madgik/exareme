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

from os import path
sys.path.append(path.abspath(__file__))

from tests.algorithm_tests.lib import vmUrl
endpointUrl_CartTraining= vmUrl+'CART'
endpointUrl_CartPredict= vmUrl+'CART_PREDICT'
path = '../data/dementia/'

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
    df = df[varNames]
    df = df.dropna()
    X = df.drop(args_Y, axis=1)
    y = df[args_Y]

    #4. Run scikit predictor
    mydata = []
    ydata = []
    for index, row in df.iterrows():
        rowNew = [row[x] for x in X]
        mydata.append(rowNew)
        ydata.append(y[index])
    predictedValues=list(clf.predict(mydata))

    #5. Save testing dataset with predictions
    file = open("prediction_sklearnClassifiaction.csv",'w')
    wr = csv.writer(file, dialect='excel')
    ds = [elem for elem in args_X]
    ds.append(argsY)
    ds.append("prediction")
    wr.writerow(ds)
    for i in range(len(predictedValues)):
        rowcopy = [mydata[i][j] for j in range(len(args_X))]
        rowcopy.append(ydata[i])
        rowcopy.append(predictedValues[i])
        wr.writerow(rowcopy)
    file.close()

    # 6.Create confusion matrix
    confusionMatrix = dict()
    for i in range(len(y)):
        if (ydata[i],predictedValues[i]) in confusionMatrix:
            confusionMatrix[ydata[i],predictedValues[i]] = confusionMatrix[ydata[i],predictedValues[i]] + 1
        else:
            confusionMatrix[ydata[i],predictedValues[i]] = 1
    return confusionMatrix

def evaluate_predictions(ExaremeCM, sklearnCM):
    ExaremeCorrectPredictions = 0
    ExaremeWrongPredictions = 0
    for i in range(len(ExaremeCM)):
        if ExaremeCM[i][0] == ExaremeCM[i][1]:
            ExaremeCorrectPredictions = ExaremeCorrectPredictions + ExaremeCM[i][2]
        else:
            ExaremeWrongPredictions = ExaremeWrongPredictions + ExaremeCM[i][2]

    sklearnCorrectPredictions = 0
    sklearnWrongPredictions = 0
    for key in sklearnCM:
        if key[0] == key[1]:
            sklearnCorrectPredictions = sklearnCorrectPredictions + sklearnCM[key]
        else:
            sklearnWrongPredictions = sklearnWrongPredictions + sklearnCM[key]
    #print (ExaremeCorrectPredictions, ExaremeWrongPredictions, sklearnCorrectPredictions, sklearnWrongPredictions)
    return ExaremeCorrectPredictions, ExaremeWrongPredictions, sklearnCorrectPredictions, sklearnWrongPredictions

def run_sklearn_regression(trainingDatasetPath, PredictionDatasetPath, argsX, argsY, maxDepth):
    #1. Read training dataset
    df = pd.read_csv(trainingDatasetPath, index_col ="subjectcode")
    args_X = list(argsX.replace(" ", "").split(","))
    args_Y = [argsY.replace(" ", "")][0]
    varNames = [x for x in args_X]
    varNames.append(args_Y)
    df = df[varNames]
    df = df.dropna()
    X = df.drop(args_Y, axis=1)
    y = df[args_Y]

    #2. Fit regression model
    regr = tree.DecisionTreeRegressor(max_depth = int(maxDepth))
    regr = regr.fit(X, y)

    #3. Read testing dataset
    df = pd.read_csv(PredictionDatasetPath, index_col ="subjectcode")
    df = df[varNames]
    df = df.dropna()
    X = df.drop(args_Y, axis=1)
    y = df[args_Y]

    #4. Predict
    mydata = []
    ydata = []
    for index,row in df.iterrows():
        #print index, [row[x] for x in X]
        rowNew = [row[x] for x in X]
        mydata.append(rowNew)
        ydata.append(y[index])
    predictedValues=list(regr.predict(mydata))

    #5. Save testing dataset with predictions
    file = open("prediction_sklearnRegression.csv",'w')
    wr = csv.writer(file, dialect='excel')
    ds = [elem for elem in args_X]
    ds.append(argsY)
    ds.append("prediction")
    wr.writerow(ds)
    for i in range(len(predictedValues)):
        rowcopy = [mydata[i][j] for j in range(len(args_X))]
        rowcopy.append(ydata[i])
        rowcopy.append(predictedValues[i])
        wr.writerow(rowcopy)
    file.close()

    #Model Evaluation
    mse = metrics.mean_squared_error(ydata, predictedValues)
    rmse =np.sqrt(mse)
    return rmse

class Test_Cart(unittest.TestCase):
    def test_Cart_1(self):
        #Run Exareme Cart Training Algorithm
        logging.info("---------- TEST 1: CART - classification tree on  diabetes")
        data_Cart = [{ "name": "x", "value": "Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age"},
                {"name": "y", "value": "Outcome"},
                {"name": "max_depth", "value": "200"},
                {"name": "no_split_points","value": "20"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "diabetes_training"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl_CartTraining,data=json.dumps(data_Cart),headers=headers)
        exaremeResult_Training = json.loads(r.text)

        #########################'##########################################
        #Run Exareme Cart Prediction Algorithm
        data_CartPredict = [{ "name": "x", "value": data_Cart[0]["value"]},
                {"name": "y", "value":  data_Cart[1]["value"]},
                {"name": "treeJson", "value": ""},
                {"name": "treeFile", "value": "tree.txt"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "diabetes_testing"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl_CartPredict,data=json.dumps(data_CartPredict),headers=headers)
        exaremeResult_Predict = json.loads(r.text)

        ####################################################################
        #Run Python
        data_sklearn = {"trainingDataset": "diabetes_training",
                        "testingDataset": "diabetes_testing",
                        "x": "Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age",
                        "y": "Outcome",
                        "max_depth": 200}

        sklearnconfusionMatrix = run_sklearn_classification( path+data_sklearn['trainingDataset']+".csv",
                                                      path+data_sklearn['testingDataset']+".csv",
                                                      data_sklearn['x'], data_sklearn['y'], data_sklearn['max_depth'])

        ExaremeCorrectPredictions,ExaremeWrongPredictions,sklearnCorrectPredictions,sklearnWrongPredictions = evaluate_predictions(exaremeResult_Predict['result'][0]['data']['data'], sklearnconfusionMatrix)
        print ("ConfusionMatrices(E/S):", r.text, sklearnconfusionMatrix)
        print ("ExaremePredictions(C/W):",ExaremeCorrectPredictions,ExaremeWrongPredictions)
        print ("sklearnPredictions(C/W):",sklearnCorrectPredictions,sklearnWrongPredictions)
        assert (0)

    def test_Cart_2(self):
        #Run Exareme Cart Training Algorithm
        logging.info("---------- TEST 1: CART - classification tree with real variables -  Training and prediction applied on the same dataset")
        data_Cart = [{ "name": "x", "value": "rightententorhinalarea, lefthippocampus, righthippocampus"},
                {"name": "y", "value": "alzheimerbroadcategory"},
                {"name": "max_depth", "value": "200"},
                {"name": "no_split_points","value": "20"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "adni"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl_CartTraining,data=json.dumps(data_Cart),headers=headers)
        exaremeResult_Training = json.loads(r.text)

        #########################'##########################################
        #Run Exareme Cart Prediction Algorithm
        data_CartPredict = [{ "name": "x", "value": data_Cart[0]["value"]},
                {"name": "y", "value":  data_Cart[1]["value"]},
                {"name": "treeJson", "value": ""},
                {"name": "treeFile", "value": "tree.txt"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl_CartPredict,data=json.dumps(data_CartPredict),headers=headers)
        exaremeResult_Predict = json.loads(r.text)

        ####################################################################
        #Run Python
        data_sklearn = {"trainingDataset": "adni",
                        "testingDataset": "desd-synthdata",
                        "x": "rightententorhinalarea, lefthippocampus, righthippocampus",
                        "y": "alzheimerbroadcategory",
                        "max_depth": 200}

        data_sklearn = {"trainingDataset": "adni",
                        "testingDataset": "desd-synthdata",
                        "x": "montrealcognitiveassessment,rightpcggposteriorcingulategyrus,leftpcggposteriorcingulategyrus,rightacgganteriorcingulategyrus,leftacgganteriorcingulategyrus,rightmcggmiddlecingulategyrus,leftmcggmiddlecingulategyrus,rightphgparahippocampalgyrus,leftphgparahippocampalgyrus,rightententorhinalarea,leftententorhinalarea,righthippocampus,lefthippocampus,rightthalamusproper,leftthalamusproper",
                        "y": "alzheimerbroadcategory",
                        "max_depth": 200}


        sklearnconfusionMatrix = run_sklearn_classification( path+data_sklearn['trainingDataset']+".csv",
                                                      path+data_sklearn['testingDataset']+".csv",
                                                      data_sklearn['x'], data_sklearn['y'], data_sklearn['max_depth'])

        ExaremeCorrectPredictions,ExaremeWrongPredictions,sklearnCorrectPredictions,sklearnWrongPredictions = evaluate_predictions(exaremeResult_Predict['result'][0]['data']['data'], sklearnconfusionMatrix)
        print ("ConfusionMatrices(E/S):", r.text, sklearnconfusionMatrix)
        print ("ExaremePredictions(C/W):",ExaremeCorrectPredictions,ExaremeWrongPredictions)
        print ("sklearnPredictions(C/W):",sklearnCorrectPredictions,sklearnWrongPredictions)
        assert (0)

    def test_Cart_3(self):
        logging.info("---------- TEST 2: CART - regression tree with real variables -  Training and prediction applied on the same dataset")
        data_Cart = [{ "name": "x", "value": "rightententorhinalarea, lefthippocampus, righthippocampus"},
                {"name": "y", "value": "subjectage"},
                {"name": "max_depth", "value": "200"},
                {"name": "no_split_points","value": "20"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "adni"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl_CartTraining,data=json.dumps(data_Cart),headers=headers)
        exaremeResult_Training = json.loads(r.text)

        ####################################################################
        #Run Exareme Cart Prediction Algorithm
        data_CartPredict = [{ "name": "x", "value": "rightententorhinalarea, lefthippocampus, righthippocampus"},
                {"name": "y", "value": data_Cart[1]["value"]},
                {"name": "treeJson", "value": ""},
                {"name": "treeFile", "value": "tree.txt"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl_CartPredict,data=json.dumps(data_CartPredict),headers=headers)
        print ("ExaremeResult", r.text)
        exaremeResult_Predict = json.loads(r.text)

        ####################################################################
        #Run Python
        data_sklearn = {"trainingDataset": "adni",
                        "testingDataset": "desd-synthdata",
                        "x": "rightententorhinalarea, lefthippocampus, righthippocampus",
                        "y":"subjectage",
                        "max_depth": 200}

        rmse_sklearn = run_sklearn_regression(path+data_sklearn['trainingDataset']+".csv",
                                              path+data_sklearn['testingDataset']+".csv",
                                              data_sklearn['x'], data_sklearn['y'], data_sklearn['max_depth'])

        print ("rmse_sklearn", rmse_sklearn)
        assert(0)
