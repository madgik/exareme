import requests
import unittest
import os
import json
import logging
import math
from decimal import *

import rpy2.robjects as robjects

import sys
from os import path
sys.path.append(path.abspath(__file__))
from tests.algorithm_tests.lib import vmUrl
endpointUrl= vmUrl+'MULTIPLE_HISTOGRAMS'
folderPath = 'R_scripts'
file ='MultipleHistograms.Rmd'

PRIVACY_MAGIC_NUMBER = 10

class TestHistogram(unittest.TestCase):
    def setUp(self):
        filePath = os.path.join(os.path.abspath(folderPath),file)
        with open (filePath, "r") as myfile:
            data = myfile.read()
        #Execute R script
        self.TestResult = robjects.r(data)


    def test_Histogram_1(self):
        logging.info("---------- TEST 1: Histogram of right ententorhinal area ")
        data = [{ "name": "y", "value": "rightententorhinalarea,righthippocampus"},
                {"name": "x", "value": "gender, alzheimerbroadcategory"},
                {"name": "bins", "value": "{ \"rightententorhinalarea\" : 35, \"righthippocampus\" : 35 }"},
		        {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print ("AAAA", r.text)
        print ("RRRR",self.TestResult[0:5])
        resultsComparison(result, self.TestResult[0:5], 0)

    def test_Histogram_2(self):
        logging.info("---------- TEST 4: Bar graph of alzheimer broad category ")
        data = [{ "name": "y", "value": "alzheimerbroadcategory"},
                {"name": "x", "value": "gender"},
                {"name": "bins", "value": "{}"},
                {"name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": "{\"condition\": \"AND\", \"rules\": [{\"id\": \"rightententorhinalarea\",\"field\": \"rightententorhinalarea\",\"type\": \"double\", \"input\": \"number\", \"operator\": \"is_not_null\", \"value\": null}],\"valid\": true}"}]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        print (self.TestResult[6])
        resultsComparison(result, self.TestResult[6:7], 1)



def resultsComparison(result, TestResult, variableIsCategorical): #Exareme - R
    for i in range(len(TestResult)):
        Rtest = json.loads(TestResult[i])
        histogramExists = 0
        for j in range(len(result['result'])):
            if  Rtest["1"][0] == result['result'][j]['data']['title']['text']:
                histogramExists = 1
                print("YEEEEES", Rtest["1"][0],result['result'][j]['data']['title']['text'] )

                for k in range(len(result['result'][j]['data']['series'])):
                    ykey = None
                    if  result['result'][j]['data']['series'][k]['name'] =="All":
                        ykey ='y'
                    else:
                        ykey = 'y_' + result['result'][j]['data']['series'][k]['name']
                        # if variableIsCategorical == 1 :


                    #print (Rtest[ykey], Rtest['xmin'], Rtest['xmax'])
                    #print (result['result'][j]['data']['series'][k]['data'])
                    #print (result['result'][j]['data']['xAxis']['categories'])
                    if variableIsCategorical == 0 :
                        if ykey in Rtest:
                            for z in range(len(Rtest[ykey])):
                                if int(Rtest[ykey][z]) > PRIVACY_MAGIC_NUMBER:
                                    assert int(Rtest[ykey][z]) == int(result['result'][j]['data']['series'][k]['data'][z])
                                else:
                                    assert int(result['result'][j]['data']['series'][k]['data'][z]) == 0
                                #print (result['result'][j]['data']['xAxis']['categories'][z])
                                minmaxvalues = result['result'][j]['data']['xAxis']['categories'][z].split("-")
                                #print (minmaxvalues)
                                assert (math.isclose(float(minmaxvalues[0]),Rtest['xmax'][z],rel_tol=0,abs_tol=10**(-abs(Decimal(str(Rtest['xmax'][z])).as_tuple().exponent))))
                                assert (math.isclose(float(minmaxvalues[1]),Rtest['xmin'][z],rel_tol=0,abs_tol=10**(-abs(Decimal(str(Rtest['xmin'][z])).as_tuple().exponent))))
                        else:
                            for z in range(len(result['result'][j]['data']['series'][k]['data'])):
                                assert int(result['result'][j]['data']['series'][k]['data'][z]) == 0
                    elif variableIsCategorical == 1 :
                        print ("Categories" , result['result'][j]['data']['xAxis']['categories'])
                        print ("Key", ykey)
                        for z in range(len(result['result'][j]['data']['xAxis']['categories'])):
                            ykeynew = ykey+"_"+ result['result'][j]['data']['xAxis']['categories'][z]
                            print (ykeynew)
                            if ykeynew in Rtest:
                                print (Rtest[ykeynew], result['result'][j]['data']['series'][k]['data'][z] )
                                if int(Rtest[ykeynew][0]) > PRIVACY_MAGIC_NUMBER:
                                    assert int(Rtest[ykeynew][0]) == int(result['result'][j]['data']['series'][k]['data'][z])
                                else:
                                    assert int(result['result'][j]['data']['series'][k]['data'][z]) == 0
                            else:
                                print(int(result['result'][j]['data']['series'][k]['data'][z]) )
                                assert int(result['result'][j]['data']['series'][k]['data'][z]) == 0
        assert (histogramExists==1)



if __name__ == '__main__':
    unittest.main()
