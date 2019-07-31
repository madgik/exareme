import requests
import unittest
import os,sys
import json
import logging
import math
from decimal import *

from rpy2.robjects.packages import importr
import rpy2.robjects as robjects

endpointUrl='http://88.197.53.38:9090/mining/query/TTEST_INDEPENDENT'
folderPath = 'R_scripts'
file ='ttest_independent.Rmd'


class TestTTESTIndependent(unittest.TestCase):
    def setUp(self):
        filePath = os.path.join(os.path.abspath(folderPath),file)
        with open (filePath, "r") as myfile:
            data = myfile.read()
        #Execute R script
        self.Test1aResult, self.Test1bResult, self.Test2Result, self.Test3Result = robjects.r(data)
        print ("1", self.Test1aResult)
        print ("2", self.Test1bResult)
        print ("3", self.Test2Result)
        print ("4", self.Test3Result)


    def test_UnpairedTtest_1a(self):
        logging.info("---------- TEST 1: We check if the means are different (M,F). ")
        data = [{"name": "x", "value": "lefthippocampus"},
                {"name": "y", "value": "gender"    },
                {"name": "ylevels",  "value": "M,F"},
                {"name": "hypothesis", "value": "different"},
                {"name": "effectsize", "value": "1" },
                {"name": "ci","value": "0"  },
                {"name": "meandiff", "value": "0"  },
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(data, result['result'][0]['data'], json.loads(self.Test1aResult))

    def test_UnpairedTtest_1b(self):
        logging.info("---------- TEST 1: We check if the means are different (F,M). ")
        data = [{"name": "x", "value": "lefthippocampus"},
                {"name": "y", "value": "gender"    },
                {"name": "ylevels",  "value": "M,F"},
                {"name": "hypothesis", "value": "different"},
                {"name": "effectsize", "value": "1" },
                {"name": "ci","value": "1"  },
                {"name": "meandiff", "value": "1"  },
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(data, result['result'][0]['data'], json.loads(self.Test1bResult))

    def test_UnpairedTtest_2(self):
        logging.info("---------- TEST 2: We check if the mean volumes are greater for men than for women. ")
        data = [{"name": "x", "value": "lefthippocampus"},
                {"name": "y", "value": "gender"    },
                {"name": "ylevels",  "value": "M,F"},
                {"name": "hypothesis", "value": "greaterthan"},
                {"name": "effectsize", "value": "1" },
                {"name": "ci","value": "1"  },
                {"name": "meandiff", "value": "1"  },
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(data, result['result'][0]['data'], json.loads(self.Test2Result))

    def test_UnpairedTtest_3(self):
        logging.info("---------- TEST 3: twoGreater  ")
        data = [{"name": "x", "value": "lefthippocampus"},
                {"name": "y", "value": "gender"    },
                {"name": "ylevels",  "value": "M,F"},
                {"name": "hypothesis", "value": "lessthan"},
                {"name": "effectsize", "value": "1" },
                {"name": "ci","value": "1"  },
                {"name": "meandiff", "value": "1"  },
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(data, result['result'][0]['data'], json.loads(self.Test3Result))

    def test_UnpairedTtest__Privacy(self):
        logging.info("---------- TEST : Algorithms for Privacy Error")
        data = [{"name": "x", "value": "lefthippocampus"},
                {"name": "y", "value": "gender"    },
                {"name": "ylevels",  "value": "M,F"},
                {"name": "hypothesis", "value": "lessthan"},
                {"name": "effectsize", "value": "1" },
                {"name": "ci","value": "1"  },
                {"name": "meandiff", "value": "1"  },
                {"name": "dataset", "value": "adni_9rows"},
                {"name": "filter","value": ""}
               ]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
        result = json.loads(r.text)
        check_privacy_result(r.text)

def check_privacy_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"


def resultsComparison(data, jsonExaremeResult, jsonRResult):
    assert (len(jsonExaremeResult)==len(jsonRResult))
    for i in range(len(jsonRResult)):
        varR = str(jsonRResult[i]['var[stud]'])
        variableExist = 0
        for j in range(len(jsonExaremeResult)):
            if jsonExaremeResult[j]['colname'] == varR:
                variableExist +=1
                assert (math.isclose(jsonExaremeResult[j]['statistics'],jsonRResult[i]['stat[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['stat[stud]'])).as_tuple().exponent))))
                assert (math.isclose(jsonExaremeResult[j]['df'],jsonRResult[i]['df[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['df[stud]'])).as_tuple().exponent))))
                if int(data[4]['value']) == 1: #effectsize
                    print("effectsize")
                    assert (math.isclose(jsonExaremeResult[j]['Cohens_d'],jsonRResult[i]['es[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['es[stud]'])).as_tuple().exponent))))
                if int(data[5]['value']) == 1:  #ci
                    print("ci")
                    if str(jsonRResult[i]['ciu[stud]']) =='Inf' or str(jsonRResult[i]['ciu[stud]']) =='-Inf':
                        assert str(jsonExaremeResult[j]['Upper'])  == str(jsonRResult[i]['ciu[stud]'])
                    else:
                        assert (math.isclose(jsonExaremeResult[j]['Upper'],jsonRResult[i]['ciu[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['ciu[stud]'])).as_tuple().exponent))))
                    if str(jsonRResult[i]['cil[stud]']) =='Inf' or str(jsonRResult[i]['cil[stud]']) =='-Inf':
                        assert str(jsonExaremeResult[j]['Lower'])  == str(jsonRResult[i]['cil[stud]'])
                    else:
                        assert (math.isclose(jsonExaremeResult[j]['Lower'],jsonRResult[i]['cil[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['cil[stud]'])).as_tuple().exponent))))
                if int(data[6]['value']) == 1:  #meandiff
                    print("meandiff")
                    assert (math.isclose(jsonExaremeResult[j]['Meandifference'],jsonRResult[i]['md[stud]'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[i]['md[stud]'])).as_tuple().exponent))))


if __name__ == '__main__':
    unittest.main()
