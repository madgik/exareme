import requests
import unittest
import os,sys
import json
import logging
import math
from decimal import *

from rpy2.robjects.packages import importr
import rpy2.robjects as robjects

endpointUrl='http://88.197.53.34:9090/mining/query/HISTOGRAMS'

folderPath = 'R_scripts'
file ='Histograms.Rmd'


class TestHistogram(unittest.TestCase):
    def setUp(self):
        filePath = os.path.join(os.path.abspath(folderPath),file)
        with open (filePath, "r") as myfile:
            data = myfile.read()
        #Execute R script
        self.Test1Result, self.Test2Result, self.Test3Result, self.Test4Result, self.Test5Result = robjects.r(data)


    def test_Histogram_1(self):
        logging.info("---------- TEST 1: Histogram of right ententorhinal area ")
        data = [{ "name": "x", "value": "rightententorhinalarea"},
                {"name": "y", "value": ""},
                {"name": "bins", "value": "35"},
		        {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print ("AAAA", r.text)
        print ("BBBB1", self.Test1Result)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test1Result))

    def test_Histogram_2(self):
        logging.info("---------- TEST 2: Histogram of right ententorhinal area by gender ")
        data = [{ "name": "x", "value": "rightententorhinalarea"},
                {"name": "y", "value": "gender"},
                {"name": "bins", "value": "24"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print ("AAAA", r.text)
        print ("BBBB2", self.Test2Result)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test2Result))

    def test_Histogram_3(self):
        logging.info("---------- TEST 3: Histogram of right ententorhinal area by alzheimer broad category ")
        data = [{ "name": "x", "value": "rightententorhinalarea"},
                {"name": "y", "value": "alzheimerbroadcategory"},
                {"name": "bins", "value": "19"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test3Result))

    def test_Histogram_4(self):
        logging.info("---------- TEST 4: Bar graph of alzheimer broad category ")
        data = [{ "name": "x", "value": "alzheimerbroadcategory"},
                {"name": "y", "value": ""},
                {"name": "bins", "value": ""},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": "{\"condition\": \"AND\", \"rules\": [{\"id\": \"rightententorhinalarea\",\"field\": \"rightententorhinalarea\",\"type\": \"double\", \"input\": \"number\", \"operator\": \"is_not_null\", \"value\": null}],\"valid\": true}"}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test4Result))

    def test_Histogram_5(self):
        logging.info("---------- TEST 5: Bar graph of alzheimer broad category by gender ")
        data = [{ "name": "x", "value": "alzheimerbroadcategory"},
                {"name": "y", "value": "gender"},
                {"name": "bins", "value": ""},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": "{\"condition\": \"AND\", \"rules\": [{\"id\": \"rightententorhinalarea\",\"field\": \"rightententorhinalarea\",\"type\": \"double\", \"input\": \"number\", \"operator\": \"is_not_null\", \"value\": null}],\"valid\": true}"}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test5Result))


    def test_Histogram_Privacy(self):
        logging.info("---------- TEST : Algorithms for Privacy Error")
        data = [{"name": "x", "value": "alzheimerbroadcategory"},
                {"name": "y", "value": "gender"},
                {"name": "bins", "value": ""},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "adni_9rows"},
                {"name": "filter", "value": ""}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
        result = json.loads(r.text)
        check_privacy_result(r.text)



def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The Experiment could not run with the input provided because there are insufficient data.\",\"type\":\"text/plain+warning\"}]}"

def resultsComparison(jsonExaremeResult, jsonRResult):
    minNumberOfData = 10
    for d in range(len(jsonRResult)):
        for key in jsonRResult[d]:
            # print ("KEY1:", key, jsonRResult[d][key] )
            if key =='xmin' or key =='xmax':
                assert (math.isclose(jsonExaremeResult[d][key],jsonRResult[d][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[d][key])).as_tuple().exponent))))
            elif key == 'y':
                ExaremeVal = json.loads(jsonExaremeResult[d][key])[0][""]
                if int(jsonRResult[d][key]) >= minNumberOfData:
                    assert int(ExaremeVal) == int(jsonRResult[d][key])
                else:
                    assert int(ExaremeVal) == 0
                # print("KEY2:", key, ExaremeVal,jsonRResult[d][key])
            else:
                _,grouping = key.split('_',1)
                exaremejson = json.loads(jsonExaremeResult[d]['y'])
                print ("exaremejson", exaremejson)
                for i in range(len(exaremejson)):
                    if grouping in exaremejson[i]:
                        if int(jsonRResult[d][key]) >= minNumberOfData:
                            assert int(exaremejson[i][grouping]) == int(jsonRResult[d][key])
                        else:
                            assert int(exaremejson[i][grouping]) == 0
                        print("KEY3:", key, exaremejson[i][grouping],jsonRResult[d][key])


if __name__ == '__main__':
    unittest.main()
