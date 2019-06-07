import requests
import json
import logging
import math
from decimal import *

url2='http://localhost:9090/mining/query/NAIVE_BAYES_TRAINING_SIMPLE'

def test_NAIVEBAYES_1():
    logging.info("---------- TEST : NAIVE BAYES SIMPLE  ")
    #CROSS VALIDATION

    #NAIVE BAYES TRAINING
    data2 =[{"name": "dataset","value": "desd-synthdata"},
            {"name": "alpha","value": "0.1"},
            {"name": "x", "value": "lefthippocampus,righthippocampus,leftententorhinalarea,rightententorhinalarea"},
            {"name": "y", "value": "alzheimerbroadcategory"},
            {"name": "filter",  "value": ""}]


    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url2,data=json.dumps(data2),headers=headers)
    trainingResult = json.loads(r.text)
    print (r.text)

    '''
        $lefthippocampus
           lefthippocampus
    Y           [,1]      [,2]
      AD    2.828335 0.4299329
      CN    3.128828 0.3089273
      Other 2.942277 0.3591660


    $righthippocampus
           righthippocampus
    Y           [,1]      [,2]
      AD    3.031867 0.4062008
      CN    3.336138 0.3006037
      Other 3.141716 0.3877696


    $leftententorhinalarea
           leftententorhinalarea
    Y           [,1]      [,2]
      AD    1.428712 0.2436910
      CN    1.632501 0.1778063
      Other 1.508855 0.2410986

    $rightententorhinalarea
           rightententorhinalarea
    Y           [,1]      [,2]
      AD    1.456952 0.2298346
      CN    1.677754 0.1749192
      Other 1.542697 0.2677356

    > classifier$apriori/(274+298+146)
    Y
       AD        CN     Other
       0.3816156 0.4150418 0.2033426
    '''

    corr_trainingResult=[
    ["lefthippocampus","None","AD", 2.828335, 0.4299329,"None"],
    ["lefthippocampus","None","CN",3.128828, 0.3089273,"None"],
    ["lefthippocampus","None","Other", 2.942277, 0.3591660,"None"],
    ["righthippocampus","None","AD", 3.031867 ,0.4062008,"None"],
    ["righthippocampus","None","CN",3.336138 , 0.3006037,"None"],
    ["righthippocampus","None","Other",3.141716, 0.3877696,"None"],
    ["leftententorhinalarea","None","AD", 1.428712, 0.2436910,"None"],
    ["leftententorhinalarea","None","CN", 1.632501, 0.177806,"None"],
    ["leftententorhinalarea","None","Other",1.508855, 0.2410986,"None"],
    ["rightententorhinalarea","None","AD",1.456952 ,0.229834,"None"],
    ["rightententorhinalarea","None","CN",1.677754, 0.1749192,"None"],
    ["rightententorhinalarea","None","Other",1.542697 , 0.2677356,"None"],
    ["alzheimerbroadcategory","AD","AD","None","None",0.3816156],
    ["alzheimerbroadcategory","CN","CN","None","None",0.4150418],
    ["alzheimerbroadcategory","Other","Other","None","None",0.2033426]]


    for result1 in trainingResult['resources'][0]['data'][1:]:
        check_trainingresult(result1,corr_trainingResult)



def check_trainingresult(myresult,corr_variable_data):
    exist = False
    for j in corr_variable_data:
        print(j)
        if  myresult[0] == j[0] and  myresult[1] == j[1] and myresult[2] == j[2]:
            exist = True
            if str(j[3]) == "None":
                assert str(myresult[3] == 'None')
            else:
                assert math.isclose(float(myresult[3]),float(j[3]),rel_tol=0,abs_tol=10**(-abs(Decimal(str(float(j[3]))).as_tuple().exponent)))
            if str(j[4]) == "None":
                assert str(myresult[4] == 'None')
            else:
                assert math.isclose(float(myresult[4]),float(j[4]),rel_tol=0,abs_tol=10**(-abs(Decimal(str(float(j[4]))).as_tuple().exponent)))
            if str(j[5]) == "None":
                assert str(myresult[5] == 'None')
            else:
                assert math.isclose(float(myresult[5]),float(j[5]),rel_tol=0,abs_tol=10**(-abs(Decimal(str(float(j[5]))).as_tuple().exponent)))
    assert exist
