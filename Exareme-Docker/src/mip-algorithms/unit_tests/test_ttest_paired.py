import requests
import unittest
import os,sys
import json
import logging
import math
from decimal import *

from rpy2.robjects.packages import importr
import rpy2.robjects as robjects

endpointUrl='http://88.197.53.100:9090/mining/query/TTEST_PAIRED'
folderPath = 'R_scripts'
file ='ttest_paired.Rmd'

class TestTTESTPaired(unittest.TestCase):
    def setUp(self):
        filePath = os.path.join(os.path.abspath(folderPath),file)
        with open (filePath, "r") as myfile:
            data = myfile.read()
        #Execute R script
        self.Test1Result, self.Test2aResult, self.Test2bResult, self.Test3Result, self.Test4Result = robjects.r(data)
        print ("1", self.Test1Result)
        print ("2a", self.Test2aResult)
        print ("2b", self.Test2bResult)
        print ("3", self.Test3Result)
        print ("4", self.Test4Result)

    def test_pairedttest_1(self):
        logging.info("---------- TEST 1:  ")
        data = [{"name": "y", "value": "lefthippocampus-righthippocampus"},
                {"name": "hypothesis", "value": "different"},
		{   "name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(data, result['result'][0]['data'], json.loads(self.Test1Result))


    def test_pairedttest_2(self):
        logging.info("---------- TEST 2: ")
        data = [{"name": "y", "value": "lefthippocampus-righthippocampus,leftententorhinalarea - rightententorhinalarea"},
                {"name": "hypothesis", "value": "different"},
		{   "name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(data, [result['result'][0]['data'][1]], json.loads(self.Test2aResult))
        resultsComparison(data, [result['result'][0]['data'][0]], json.loads(self.Test2bResult))

    def test_pairedttest_3(self):
        logging.info("---------- TEST 3: ")
        data = [{"name": "y", "value": "lefthippocampus-righthippocampus"},
                {"name": "hypothesis", "value": "greaterthan"},
		{   "name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(data, result['result'][0]['data'], json.loads(self.Test3Result))


    def test_pairedttest_4(self):
        logging.info("---------- TEST 4: ")
        data = [{"name": "y", "value": "lefthippocampus-righthippocampus"},
                {"name": "hypothesis", "value": "lessthan"},
		{   "name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(data, result['result'][0]['data'], json.loads(self.Test4Result))

    def test_pairedttest_Privacy(self):
        logging.info("---------- TEST : Algorithms for Privacy Error")
        data = [{"name": "y", "value": "lefthippocampus-righthippocampus"},
                    {"name": "hypothesis", "value": "different"},
		            {"name": "pathology","value":"dementia"},
                    {"name": "dataset", "value": "adni_9rows"},
                    {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
        result = json.loads(r.text)
        check_privacy_result(r.text)

def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The Experiment could not run with the input provided because there are insufficient data.\",\"type\":\"text/plain+warning\"}]}"


def resultsComparison(data, jsonExaremeResult, jsonRResult):
    print (jsonExaremeResult)
    print (jsonRResult)
    assert (len(jsonExaremeResult)==len(jsonRResult))
    variableExist = 0
    for i in range(len(jsonRResult)):
        varR = str(jsonRResult[i]['var1[stud]']) +"-" + str(jsonRResult[i]['var2[stud]'])
        print ("varR", varR)
        for j in range(len(jsonExaremeResult)):
            if jsonExaremeResult[j]['colname'] == varR:
                variableExist +=1
                assert (math.isclose(jsonExaremeResult[j]['t_value'],jsonRResult[i]['stat[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['stat[stud]'])).as_tuple().exponent))))
                assert (math.isclose(jsonExaremeResult[j]['df'],jsonRResult[i]['df[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['df[stud]'])).as_tuple().exponent))))
            #if int(data[2]['value']) == 1: #effectsize
                print("effectsize")
                assert (math.isclose(jsonExaremeResult[j]['Cohens_d'],jsonRResult[i]['es[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['es[stud]'])).as_tuple().exponent))))
            #if int(data[3]['value']) == 1:  #ci
                print("ci")
                if str(jsonRResult[i]['ciu[stud]']) =='Inf' or str(jsonRResult[i]['ciu[stud]']) =='-Inf':
                    assert str(jsonExaremeResult[j]['Upper'])  == str(jsonRResult[i]['ciu[stud]'])
                else:
                    assert (math.isclose(jsonExaremeResult[j]['Upper'],jsonRResult[i]['ciu[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['ciu[stud]'])).as_tuple().exponent))))
                if str(jsonRResult[i]['cil[stud]']) =='Inf' or str(jsonRResult[i]['cil[stud]']) =='-Inf':
                    assert str(jsonExaremeResult[j]['Lower'])  == str(jsonRResult[i]['cil[stud]'])
                else:
                    assert (math.isclose(jsonExaremeResult[j]['Lower'],jsonRResult[i]['cil[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['cil[stud]'])).as_tuple().exponent))))
            #if int(data[4]['value']) == 1:  #meandiff
                print("meandiff")
                assert (math.isclose(jsonExaremeResult[j]['Meandifference'],jsonRResult[i]['md[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['md[stud]'])).as_tuple().exponent))))
            #if int(data[5]['value']) == 1:  #sediff
                print("sediff")
                assert (math.isclose(jsonExaremeResult[j]['SEdifference'],jsonRResult[i]['sed[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['sed[stud]'])).as_tuple().exponent))))
    assert (variableExist == len(jsonExaremeResult))

if __name__ == '__main__':
    unittest.main()
