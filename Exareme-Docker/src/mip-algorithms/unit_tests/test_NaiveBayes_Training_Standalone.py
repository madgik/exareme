import requests
import unittest
import os,sys
import json
import logging
import math
from decimal import *

from rpy2.robjects.packages import importr
import rpy2.robjects as robjects

endpointUrl='http://88.197.53.100:9090/mining/query/NAIVE_BAYES_TRAINING_STANDALONE'
folderPath = 'R_scripts'
file ='NaiveBayes_Training_Standalone.Rmd'


class TestNaiveBayesStandalone(unittest.TestCase):
    def setUp(self):
        filePath = os.path.join(os.path.abspath(folderPath),file)
        with open (filePath, "r") as myfile:
            data = myfile.read()
        #Execute R script
        self.Test1Result, self.Test2Result= robjects.r(data)


    def test_NaiveBayesStandalone_1(self):
        logging.info("---------- TEST 1: Naive Bayes training ")
        data = [{"name": "pathology","value":"dementia"},
                {"name": "dataset","value": "desd-synthdata"},
                {"name": "x", "value": "lefthippocampus,righthippocampus"},
                {"name": "y", "value": "alzheimerbroadcategory"},
                {"name": "alpha","value": "0.1"},
                { "name": "filter", "value": ""}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        # print ("AAAA", r.text)
        print ("BBBB1", self.Test1Result)
        # print (type(self.Test1Result))
        resultsComparison( result['result'][0]['data']['data'], json.loads(str(self.Test1Result))[0],'alzheimerbroadcategory')

    def test_NaiveBayesStandalone_2(self):
        logging.info("---------- TEST 2: Naive Bayes training ")
        data = [{"name": "pathology","value":"dementia"},
                {"name": "dataset","value": "desd-synthdata"},
                {"name": "x", "value": "rightmtgmiddletemporalgyrus,leftttgtransversetemporalgyrus"},
                {"name": "y", "value": "alzheimerbroadcategory"},
                {"name": "alpha","value": "0.1"},
                { "name": "filter", "value": ""}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print ("AAAA", r.text)
        print ("BBBB1", self.Test2Result)
        # print (type(self.Test2Result))
        resultsComparison( result['result'][0]['data']['data'],  json.loads(str(self.Test2Result))[0],'alzheimerbroadcategory')


    def test_NaiveBayesStandalone_Privacy(self):
        logging.info("---------- TEST : Algorithms for Privacy Error")
        data = [{"name": "pathology","value":"dementia"},
                {"name": "dataset","value": "adni_9rows"},
                {"name": "x", "value": "lefthippocampus,righthippocampus"},
                {"name": "y", "value": "alzheimerbroadcategory"},
                {"name": "alpha","value": "0.1"},
                { "name": "filter", "value": ""}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
        result = json.loads(r.text)
        check_privacy_result(r.text)

def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The Experiment could not run with the input provided because there are insufficient data.\",\"type\":\"text/plain+warning\"}]}"

def resultsComparison(jsonExaremeResult, RResult, y):
    jsonRResult1 = json.loads(RResult[1])
    jsonRResult2 = json.loads(RResult[2])
    jsonRReresult3 = int(RResult[3])
    print ("1:",jsonExaremeResult)
    print ("2:",jsonRResult1)
    print ("2:",jsonRResult2)

    no = 0
    for rowExareme in jsonExaremeResult:
        if rowExareme[0] == y: #Compare a-priori probabilities
            for rowR in jsonRResult2:
                if rowR['Y'] == rowExareme[2]:
                    #print (rowExareme[5], rowR['Freq'])
                    assert math.isclose(float(rowExareme[5]),float(rowR['Freq']),rel_tol=0,abs_tol=10**(-abs(Decimal(str(rowR['Freq'])).as_tuple().exponent)))
                    no = no + 1
        else: #Compare Conditional probabilities
            for rowR in jsonRResult1:
                if rowR['_row'] == rowExareme[2]:
                    #print (rowExareme[0], rowExareme[3],rowR[str(rowExareme[0])+'.1'])
                    assert math.isclose(float(rowExareme[3]),float(rowR[str(rowExareme[0])+'.1']),rel_tol=0,abs_tol=10**(-abs(Decimal(str(rowR[str(rowExareme[0])+'.1'])).as_tuple().exponent)))
                    assert math.isclose(float(rowExareme[4]),float(rowR[str(rowExareme[0])+'.2']),rel_tol=0,abs_tol=10**(-abs(Decimal(str(rowR[str(rowExareme[0])+'.2'])).as_tuple().exponent)))
                    no = no + 2
    assert (jsonRReresult3  == no)
if __name__ == '__main__':
    unittest.main()
