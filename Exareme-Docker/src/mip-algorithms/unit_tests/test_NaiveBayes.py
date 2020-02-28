import requests
import json
import logging
import math
from decimal import *


url1='http://88.197.53.34:9090/mining/query/CROSS_VALIDATION_K_FOLD'
url2='http://88.197.53.34:9090/mining/query/NAIVE_BAYES_TRAINING'
url3='http://88.197.53.34:9090/mining/query/NAIVE_BAYES_TESTING'

def test_NAIVEBAYES_1():
    logging.info("---------- TEST : NAIVE BAYES :CATEGORICAL DATASET  ")
    #CROSS VALIDATION

    data1 =[{"name": "pathology","value":"dementia"},
            {"name": "dataset","value": "car"},
            {"name": "x", "value": "car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety"},
            {"name": "y", "value": "car_class"},
            {"name": "kfold","value": "3"},
            {"name": "dbIdentifier","value": ""},
            { "name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url1,data=json.dumps(data1),headers=headers)
    result1 = json.loads(r.text)
    print (r.text)

    #NAIVE BAYES TRAINING
    data2 =[{   "name": "pathology","value":"dementia"},
	    {"name": "iterationNumber","value": "0"},
            {"name": "alpha","value": "0.1"},
            {"name": "dataset","value": "car"},
            {"name": "x", "value": "car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety"},
            {"name": "y", "value": "car_class"}]
    data2.append({"name": "dbIdentifier","value":result1['result'][0]['dbIdentifier'] })

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url2,data=json.dumps(data2),headers=headers)
    trainingResult = json.loads(r.text)
    print (r.text)


#     $car_buying
#        car_buying
# Y              high         low         med
#   acc   0.331149689 0.291844088 0.377006223
#   good  0.001443001 0.665223665 0.333333333
#   unacc 0.262301977 0.361839338 0.375858685
#   vgood 0.001531394 0.598774885 0.399693721
#
# $car_maint
#        car_maint
# Y              high         low         med       vhigh
#   acc   0.321218075 0.183693517 0.259004584 0.236083824
#   good  0.001440922 0.664265130 0.332853026 0.001440922
#   unacc 0.248247827 0.274880852 0.274880852 0.201990468
#   vgood 0.200305810 0.399082569 0.399082569 0.001529052
#
# $car_doors
#        car_doors
# Y               2         3         4     5more
#   acc   0.1967911 0.2655534 0.2688278 0.2688278
#   good  0.2175793 0.2608069 0.2608069 0.2608069
#   unacc 0.2594617 0.2440426 0.2482478 0.2482478
#   vgood 0.1544343 0.2308869 0.3073394 0.3073394
#
# $car_persons
#        car_persons
# Y                  2            4         more
#   acc   0.0003275467 0.5178512938 0.4818211595
#   good  0.0014430014 0.5209235209 0.4776334776
#   unacc 0.5300714987 0.2286555447 0.2412729567
#   vgood 0.0015313936 0.4609494640 0.5375191424
#
# $car_lug_boot
#        car_lug_boot
# Y               big         med       small
#   acc   0.354077956 0.350802489 0.295119555
#   good  0.347763348 0.347763348 0.304473304
#   unacc 0.297350343 0.319781298 0.382868358
#   vgood 0.614088821 0.384379786 0.001531394
#
# $car_safety
#        car_safety
# Y               high          low          med
#   acc   0.5014739600 0.0003275467 0.4981984933
#   good  0.4343434343 0.0014430014 0.5642135642
#   unacc 0.1908033086 0.5384831067 0.2707135847
#   vgood 0.9969372129 0.0015313936 0.0015313936

# > classifier$apriori/n
# Y
#        acc       good      unacc      vgood
# 0.26475694 0.05989583 0.61892361 0.05642361

    corr_trainingResult= [{"val": "high","probability": 0.331149689,"sigma": "NA","average": "NA","colname": "car_buying","classval": "acc"},{"val": "low","probability": 0.291844088,"sigma": "NA","average": "NA","colname": "car_buying","classval": "acc"},{"val": "med","probability": 0.377006223,"sigma": "NA","average": "NA","colname": "car_buying","classval": "acc"},{"val": "high","probability": 0.001443001,"sigma": "NA","average": "NA","colname": "car_buying","classval": "good"},{"val": "low","probability": 0.665223665,"sigma": "NA","average": "NA","colname": "car_buying","classval": "good"},{"val": "med","probability": 0.333333333,"sigma": "NA","average": "NA","colname": "car_buying","classval": "good"},{"val": "high","probability": 0.262301977,"sigma": "NA","average": "NA","colname": "car_buying","classval": "unacc"},{"val": "low","probability": 0.361839338,"sigma": "NA","average": "NA","colname": "car_buying","classval": "unacc"},{"val": "med","probability": 0.375858685,"sigma": "NA","average": "NA","colname": "car_buying","classval": "unacc"},{"val": "high","probability": 0.001531394,"sigma": "NA","average": "NA","colname": "car_buying","classval": "vgood"},{"val": "low","probability": 0.598774885,"sigma": "NA","average": "NA","colname": "car_buying","classval": "vgood"},{"val": "med","probability": 0.399693721,"sigma": "NA","average": "NA","colname": "car_buying","classval": "vgood"},{"val": "high","probability": 0.321218075,"sigma": "NA","average": "NA","colname": "car_maint","classval": "acc"},{"val": "low","probability": 0.183693517,"sigma": "NA","average": "NA","colname": "car_maint","classval": "acc"},{"val": "med","probability": 0.259004584,"sigma": "NA","average": "NA","colname": "car_maint","classval": "acc"},{"val": "vhigh","probability": 0.236083824,"sigma": "NA","average": "NA","colname": "car_maint","classval": "acc"},{"val": "high","probability": 0.001440922,"sigma": "NA","average": "NA","colname": "car_maint","classval": "good"},{"val": "low","probability": 0.66426513,"sigma": "NA","average": "NA","colname": "car_maint","classval": "good"},{"val": "med","probability": 0.332853026,"sigma": "NA","average": "NA","colname": "car_maint","classval": "good"},{"val": "vhigh","probability": 0.001440922,"sigma": "NA","average": "NA","colname": "car_maint","classval": "good"},{"val": "high","probability": 0.248247827,"sigma": "NA","average": "NA","colname": "car_maint","classval": "unacc"},{"val": "low","probability": 0.274880852,"sigma": "NA","average": "NA","colname": "car_maint","classval": "unacc"},{"val": "med","probability": 0.274880852,"sigma": "NA","average": "NA","colname": "car_maint","classval": "unacc"},{"val": "vhigh","probability": 0.201990468,"sigma": "NA","average": "NA","colname": "car_maint","classval": "unacc"},{"val": "high","probability": 0.20030581,"sigma": "NA","average": "NA","colname": "car_maint","classval": "vgood"},{"val": "low","probability": 0.399082569,"sigma": "NA","average": "NA","colname": "car_maint","classval": "vgood"},{"val": "med","probability": 0.399082569,"sigma": "NA","average": "NA","colname": "car_maint","classval": "vgood"},{"val": "vhigh","probability": 0.001529052,"sigma": "NA","average": "NA","colname": "car_maint","classval": "vgood"},{"val": "2","probability": 0.1967911,"sigma": "NA","average": "NA","colname": "car_doors","classval": "acc"},{"val": "3","probability": 0.2655534,"sigma": "NA","average": "NA","colname": "car_doors","classval": "acc"},{"val": "4","probability": 0.2688278,"sigma": "NA","average": "NA","colname": "car_doors","classval": "acc"},{"val": "5more","probability": 0.2688278,"sigma": "NA","average": "NA","colname": "car_doors","classval": "acc"},{"val": "2","probability": 0.2175793,"sigma": "NA","average": "NA","colname": "car_doors","classval": "good"},{"val": "3","probability": 0.2608069,"sigma": "NA","average": "NA","colname": "car_doors","classval": "good"},{"val": "4","probability": 0.2608069,"sigma": "NA","average": "NA","colname": "car_doors","classval": "good"},{"val": "5more","probability": 0.2608069,"sigma": "NA","average": "NA","colname": "car_doors","classval": "good"},{"val": "2","probability": 0.2594617,"sigma": "NA","average": "NA","colname": "car_doors","classval": "unacc"},{"val": "3","probability": 0.2440426,"sigma": "NA","average": "NA","colname": "car_doors","classval": "unacc"},{"val": "4","probability": 0.2482478,"sigma": "NA","average": "NA","colname": "car_doors","classval": "unacc"},{"val": "5more","probability": 0.2482478,"sigma": "NA","average": "NA","colname": "car_doors","classval": "unacc"},{"val": "2","probability": 0.1544343,"sigma": "NA","average": "NA","colname": "car_doors","classval": "vgood"},{"val": "3","probability": 0.2308869,"sigma": "NA","average": "NA","colname": "car_doors","classval": "vgood"},{"val": "4","probability": 0.3073394,"sigma": "NA","average": "NA","colname": "car_doors","classval": "vgood"},{"val": "5more","probability": 0.3073394,"sigma": "NA","average": "NA","colname": "car_doors","classval": "vgood"},{"val": "2","probability": 0.0003275467,"sigma": "NA","average": "NA","colname": "car_persons","classval": "acc"},{"val": "4","probability": 0.5178512938,"sigma": "NA","average": "NA","colname": "car_persons","classval": "acc"},{"val": "more","probability": 0.4818211595,"sigma": "NA","average": "NA","colname": "car_persons","classval": "acc"},{"val": "2","probability": 0.0014430014,"sigma": "NA","average": "NA","colname": "car_persons","classval": "good"},{"val": "4","probability": 0.5209235209,"sigma": "NA","average": "NA","colname": "car_persons","classval": "good"},{"val": "more","probability": 0.4776334776,"sigma": "NA","average": "NA","colname": "car_persons","classval": "good"},{"val": "2","probability": 0.5300714987,"sigma": "NA","average": "NA","colname": "car_persons","classval": "unacc"},{"val": "4","probability": 0.2286555447,"sigma": "NA","average": "NA","colname": "car_persons","classval": "unacc"},{"val": "more","probability": 0.2412729567,"sigma": "NA","average": "NA","colname": "car_persons","classval": "unacc"},{"val": "2","probability": 0.0015313936,"sigma": "NA","average": "NA","colname": "car_persons","classval": "vgood"},{"val": "4","probability": 0.460949464,"sigma": "NA","average": "NA","colname": "car_persons","classval": "vgood"},{"val": "more","probability": 0.5375191424,"sigma": "NA","average": "NA","colname": "car_persons","classval": "vgood"},{"val": "big","probability": 0.354077956,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "acc"},{"val": "med","probability": 0.350802489,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "acc"},{"val": "small","probability": 0.295119555,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "acc"},{"val": "big","probability": 0.347763348,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "good"},{"val": "med","probability": 0.347763348,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "good"},{"val": "small","probability": 0.304473304,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "good"},{"val": "big","probability": 0.297350343,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "unacc"},{"val": "med","probability": 0.319781298,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "unacc"},{"val": "small","probability": 0.382868358,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "unacc"},{"val": "big","probability": 0.614088821,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "vgood"},{"val": "med","probability": 0.384379786,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "vgood"},{"val": "small","probability": 0.001531394,"sigma": "NA","average": "NA","colname": "car_lug_boot","classval": "vgood"},{"val": "high","probability": 0.50147396,"sigma": "NA","average": "NA","colname": "car_safety","classval": "acc"},{"val": "low","probability": 0.0003275467,"sigma": "NA","average": "NA","colname": "car_safety","classval": "acc"},{"val": "med","probability": 0.4981984933,"sigma": "NA","average": "NA","colname": "car_safety","classval": "acc"},{"val": "high","probability": 0.4343434343,"sigma": "NA","average": "NA","colname": "car_safety","classval": "good"},{"val": "low","probability": 0.0014430014,"sigma": "NA","average": "NA","colname": "car_safety","classval": "good"},{"val": "med","probability": 0.5642135642,"sigma": "NA","average": "NA","colname": "car_safety","classval": "good"},{"val": "high","probability": 0.1908033086,"sigma": "NA","average": "NA","colname": "car_safety","classval": "unacc"},{"val": "low","probability": 0.5384831067,"sigma": "NA","average": "NA","colname": "car_safety","classval": "unacc"},{"val": "med","probability": 0.2707135847,"sigma": "NA","average": "NA","colname": "car_safety","classval": "unacc"},{"val": "high","probability": 0.9969372129,"sigma": "NA","average": "NA","colname": "car_safety","classval": "vgood"},{"val": "low","probability": 0.0015313936,"sigma": "NA","average": "NA","colname": "car_safety","classval": "vgood"},{"val": "med","probability": 0.0015313936,"sigma": "NA","average": "NA","colname": "car_safety","classval": "vgood"},{"val": "acc","probability": 0.26475694,"sigma": "NA","average": "NA","colname": "car_class","classval": "acc"},{"val": "good","probability": 0.05989583,"sigma": "NA","average": "NA","colname": "car_class","classval": "good"},{"val": "unacc","probability": 0.61892361,"sigma": "NA","average": "NA","colname": "car_class","classval": "unacc"},{"val": "vgood","probability": 0.05642361,"sigma": "NA","average": "NA","colname": "car_class","classval": "vgood"}
    ]


    for result1 in trainingResult['result'][0]['data']:
            check_trainingresult(result1,corr_trainingResult)

    #NAIVE BAYES TESTING
    data3 =[{"name": "iterationNumber","value": "0"},
            {"name": "alpha","value": "0.1"},
	    {   "name": "pathology","value":"dementia"},
            {"name": "dataset","value": "car"},
            {"name": "x", "value": "car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety"},
            {"name": "y", "value": "car_class"},
            {"name": "kfold","value": "3"}]
    data3.append({"name": "dbIdentifier","value":trainingResult['result'][0]['dbIdentifier'] })
    data3.append({"name": "model","value": json.dumps(trainingResult['result'][0]['data']) })

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url3,data=json.dumps(data3),headers=headers)
    testingResult = json.loads(r.text)
    print (r.text)

#      Reference
# Prediction acc good unacc vgood
#      acc    59    0   160     0
#      good    1    0     1     0
#      unacc   2    0   336     0
#      vgood  17    0     0     0

    corr_testingResult = [  {"Reference":"acc","Prediction":"acc","val":59},
                            {"Reference":"good","Prediction":"acc","val":0},
                            {"Reference":"unacc","Prediction":"acc","val":160},
                            {"Reference":"vgood","Prediction":"acc","val":0},

                            {"Reference":"acc","Prediction":"good","val":1},
                            {"Reference":"good","Prediction":"good","val":0},
                            {"Reference":"unacc","Prediction":"good","val":1},
                            {"Reference":"vgood","Prediction":"good","val":0},

                            {"Reference":"acc","Prediction":"unacc","val":2},
                            {"Reference":"good","Prediction":"unacc","val":0},
                            {"Reference":"unacc","Prediction":"unacc","val":336},
                            {"Reference":"vgood","Prediction":"unacc","val":0},

                            {"Reference":"acc","Prediction":"vgood","val":17},
                            {"Reference":"good","Prediction":"vgood","val":0},
                            {"Reference":"unacc","Prediction":"vgood","val":0},
                            {"Reference":"vgood","Prediction":"vgood","val":0}]

    for result1 in testingResult['result'][0]['data']:
        check_testingresult(result1,corr_testingResult)


def test_NAIVEBAYES_privacy():
    logging.info("---------- TEST : NAIVE BAYES :CATEGORICAL DATASET  ")
    #CROSS VALIDATION

    data1 =[{"name": "pathology","value":"dementia"},
            {"name": "dataset","value": "alzheimerbroadcategory"},
            {"name": "x", "value": "righthippocampus,lefthippocampus"},
            {"name": "y", "value": "desd-synthdata"},
            {"name": "kfold","value": "100"},
            {"name": "dbIdentifier","value": ""},
            { "name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url1,data=json.dumps(data1),headers=headers)
    result1 = json.loads(r.text)
    return result1
    check_privacy_result(r.text)


def check_privacy_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"


def check_testingresult(myresult,corr_variable_data):
        exist = False
        for j in corr_variable_data:
            print (myresult,j)
            if myresult['actualclass'] == j['Reference'] and  myresult['predictedclass'] == j['Prediction']:
                exist = True
                assert int(myresult['val'])==int(j['val'])
        assert exist

def check_trainingresult(myresult,corr_variable_data):
    exist = False
    for j in corr_variable_data:
        if  myresult['colname'] == j['colname'] and  myresult['val'] == j['val'] and myresult['classval'] == j['classval']:
            exist = True
            assert math.isclose(float(myresult['probability']),float(j['probability']),rel_tol=0,abs_tol=10**(-abs(Decimal(str(float(j['probability']))).as_tuple().exponent)))
    assert exist

# "sigma": "NA","average": "NA"
