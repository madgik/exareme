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
    data1 =[{"name": "dataset","value": "car"},
            {"name": "x", "value": " car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety"},
            {"name": "y", "value": "car_class"},
            {"name": "kfold","value": "3"},
            {"name": "dbIdentifier","value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url1,data=json.dumps(data1),headers=headers)
    result1 = json.loads(r.text)
    print (r.text)

    #NAIVE BAYES TRAINING
    data2 =[{"name": "iterationNumber","value": "0"},
            {"name": "alpha","value": "0.1"},
            {"name": "x", "value": " car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety"},
            {"name": "y", "value": "car_class"}]
    data2.append({"name": "dbIdentifier","value":result1['dbIdentifier'] })

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url2,data=json.dumps(data2),headers=headers)
    trainingResult = json.loads(r.text)
    print (r.text)

    $car_buying
       car_buying
Y              high         low         med
  acc   0.331149689 0.291844088 0.377006223
  good  0.001443001 0.665223665 0.333333333
  unacc 0.262301977 0.361839338 0.375858685
  vgood 0.001531394 0.598774885 0.399693721

$car_maint
       car_maint
Y              high         low         med       vhigh
  acc   0.321218075 0.183693517 0.259004584 0.236083824
  good  0.001440922 0.664265130 0.332853026 0.001440922
  unacc 0.248247827 0.274880852 0.274880852 0.201990468
  vgood 0.200305810 0.399082569 0.399082569 0.001529052

$car_doors
       car_doors
Y               2         3         4     5more
  acc   0.1967911 0.2655534 0.2688278 0.2688278
  good  0.2175793 0.2608069 0.2608069 0.2608069
  unacc 0.2594617 0.2440426 0.2482478 0.2482478
  vgood 0.1544343 0.2308869 0.3073394 0.3073394

$car_persons
       car_persons
Y                  2            4         more
  acc   0.0003275467 0.5178512938 0.4818211595
  good  0.0014430014 0.5209235209 0.4776334776
  unacc 0.5300714987 0.2286555447 0.2412729567
  vgood 0.0015313936 0.4609494640 0.5375191424

$car_lug_boot
       car_lug_boot
Y               big         med       small
  acc   0.354077956 0.350802489 0.295119555
  good  0.347763348 0.347763348 0.304473304
  unacc 0.297350343 0.319781298 0.382868358
  vgood 0.614088821 0.384379786 0.001531394

$car_safety
       car_safety
Y               high          low          med
  acc   0.5014739600 0.0003275467 0.4981984933
  good  0.4343434343 0.0014430014 0.5642135642
  unacc 0.1908033086 0.5384831067 0.2707135847
  vgood 0.9969372129 0.0015313936 0.0015313936


    corr_trainingResult= []

    check_trainingresult(trainingResult['results'],corr_trainingResult)


    #NAIVE BAYES TESTING
    data3 =[{"name": "iterationNumber","value": "0"},
            {"name": "alpha","value": "0.1"},
            {"name": "x", "value": " car_buying,car_maint,car_doors,car_persons,car_lug_boot,car_safety"},
            {"name": "y", "value": "car_class"},
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
