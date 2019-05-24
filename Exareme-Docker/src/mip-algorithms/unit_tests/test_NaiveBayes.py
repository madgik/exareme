import requests
import json
import logging
import math
from decimal import *


url1='http://192.168.90.138:9090/mining/query/CROSS_VALIDATION_K_FOLD'
url2='http://192.168.90.138:9090/mining/query/NAIVE_BAYES_TRAINING'
url3='http://192.168.90.138:9090/mining/query/NAIVE_BAYES_TESTING'

def test_NAIVEBAYES_1():
    logging.info("---------- TEST : NAIVE BAYES :PLAYTENNIS DATASET  ")
    #CROSS VALIDATION
    data1 =[{"name": "dataset","value": "playtennis"},
            {"name": "x", "value": "outlook,temperature,humidity,windy"},
            {"name": "y", "value": "play"},
            {"name": "kfold","value": "3"},
            {"name": "dbIdentifier","value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url1,data=json.dumps(data1),headers=headers)
    result1 = json.loads(r.text)
    print (r.text)

    #NAIVE BAYES TRAINING
    data2 =[{"name": "iterationNumber","value": "0"},
            {"name": "alpha","value": "0.1"},
            {"name": "x", "value": "outlook,temperature,humidity,windy"},
            {"name": "y", "value": "play"}]
    data2.append({"name": "dbIdentifier","value":result1['dbIdentifier'] })

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url2,data=json.dumps(data2),headers=headers)
    trainingResult = json.loads(r.text)
    print (r.text)

    # > classifier$tables
    # $outlook
    #      outlook
    # Y       overcast      rainy      sunny
    #   no  0.03030303 0.63636364 0.33333333
    #   yes 0.49206349 0.17460317 0.33333333
    #
    # $temperature
    #      temperature
    # Y           cool        hot       mild
    #   no  0.33333333 0.03030303 0.63636364
    #   yes 0.33333333 0.17460317 0.49206349
    #
    # $humidity
    #      humidity
    # Y          high    normal
    #   no  0.6562500 0.3437500
    #   yes 0.1774194 0.8225806
    #
    # $windy
    #      windy
    # Y         FALSE      TRUE
    #   no  0.3437500 0.6562500
    #   yes 0.6612903 0.3387097

     # >  classifier$apriori
     #    Y
     # no yes
     #  3   6

    corr_trainingResult= [  {'colname': 'outlook', 'val':'overcast', 'classval': 'no', 'probability': 0.03030303},
                            {'colname': 'outlook', 'val':'rainy', 'classval': 'no', 'probability':  0.63636364},
                            {'colname': 'outlook', 'val':'sunny', 'classval': 'no', 'probability': 0.33333333},
                            {'colname': 'outlook', 'val':'overcast', 'classval': 'yes', 'probability':  0.49206349},
                            {'colname': 'outlook', 'val':'rainy', 'classval': 'yes', 'probability':  0.17460317},
                            {'colname': 'outlook', 'val':'sunny', 'classval': 'yes', 'probability': 0.33333333},
                            {'colname': 'temperature', 'val':'cool', 'classval': 'no', 'probability': 0.33333333},
                            {'colname': 'temperature', 'val':'hot', 'classval': 'no', 'probability': 0.03030303},
                            {'colname': 'temperature', 'val':'mild', 'classval': 'no', 'probability': 0.63636364},
                            {'colname': 'temperature', 'val':'cool', 'classval': 'yes', 'probability': 0.33333333},
                            {'colname': 'temperature', 'val':'hot', 'classval': 'yes', 'probability':  0.17460317},
                            {'colname': 'temperature', 'val':'mild', 'classval': 'yes', 'probability': 0.49206349},
                            {'colname': 'humidity', 'val':'high', 'classval': 'no', 'probability': 0.6562500},
                            {'colname': 'humidity', 'val':'normal', 'classval': 'no', 'probability': 0.3437500},
                            {'colname': 'humidity', 'val':'high', 'classval': 'yes', 'probability':  0.1774194},
                            {'colname': 'humidity', 'val':'normal', 'classval': 'yes', 'probability': 0.8225806},
                            {'colname': 'windy', 'val':'FALSE', 'classval': 'no', 'probability': 0.3437500},
                            {'colname': 'windy', 'val':'TRUE', 'classval': 'no', 'probability': 0.6562500},
                            {'colname': 'windy', 'val':'FALSE', 'classval': 'yes', 'probability': 0.6612903},
                            {'colname': 'windy', 'val':'TRUE', 'classval': 'yes', 'probability': 0.3387097},
                            {'colname': 'play', 'val':'no', 'classval': 'no', 'probability':3.0/9.0},
                            {'colname': 'play', 'val':'yes', 'classval': 'yes', 'probability':6.0/9.0}]

    check_trainingresult(trainingResult['results'],corr_trainingResult)


    #NAIVE BAYES TESTING
    data3 =[{"name": "iterationNumber","value": "0"},
            {"name": "alpha","value": "0.1"},
            {"name": "x", "value": "outlook,temperature,humidity,windy"},
            {"name": "y", "value": "play"},
            {"name": "kfold","value": "3"}]
    data3.append({"name": "dbIdentifier","value":trainingResult['dbIdentifier'] })
    data3.append({"name": "model","value": json.dumps(trainingResult['results']) })

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url3,data=json.dumps(data3),headers=headers)
    testingResult = json.loads(r.text)
    print (r.text)

     # c$table
     #           Reference
     # Prediction no yes
     #        no   0   1
     #        yes  2   2
    corr_testingResult = [{"Reference":"no","Prediction":"no","val":0},
                          {"Reference":"yes","Prediction":"no","val":1},
                          {"Reference":"no","Prediction":"yes","val":2},
                          {"Reference":"yes","Prediction":"yes","val":2}]

    check_testingresult(testingResult,corr_testingResult)


def check_trainingresult(variable_data,corr_variable_data):
    no = 0
    for i in variable_data:
        check = False
        for j in corr_variable_data:
            if i['colname'] == j['colname'] and  i['val'] == j['val'] and  i['classval'] == j['classval']  and \
                    math.isclose(float(i['probability']),float(j['probability']),rel_tol=0,abs_tol=10**(-abs(Decimal(str(float(j['probability']))).as_tuple().exponent))):
                check = True
                no = no + 1
        if check == False:
            print(i)
        assert check
    assert no==len(variable_data) and no==len(corr_variable_data)


def check_testingresult(variable_data,corr_variable_data):
    no = 0
    for i in variable_data:
        check = False
        for j in corr_variable_data:
            if i['actualclass'] == j['Reference'] and  i['predictedclass'] == j['Prediction']  and int(i['val'])==int(j['val']):
                check = True
                no = no + 1
        if check == False:
            print(i)
        assert check
    assert len(variable_data)==len(corr_variable_data)

#
#
# [{"actualclass":"no","predictedclass":"no","val":0},
# {"actualclass":"no","predictedclass":"yes","val":2},
# {"actualclass":"yes","predictedclass":"no","val":1},
# {"actualclass":"yes","predictedclass":"yes","val":2}]
