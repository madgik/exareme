import requests
import unittest
import os,sys
import json
import logging
import math
from decimal import *

from rpy2.robjects.packages import importr
import rpy2.robjects as robjects

endpointUrl='http://88.197.53.100:9090/mining/query/ANOVA'
folderPath = 'R_scripts'
file ='ANOVA.Rmd'



class TestANOVA(unittest.TestCase):
    def setUp(self):
        varNamesReplacements =  [
           {'oldvarname':'var_pp', 'newvarname':'ANOVA_var_pp'},
           {'oldvarname':'var_I1', 'newvarname':'ANOVA_var_I1'},
           {'oldvarname':'var_I2', 'newvarname':'ANOVA_var_I2'},
           {'oldvarname':'var_I3', 'newvarname':'ANOVA_var_I3'},
           {'oldvarname':'var_D', 'newvarname':'ANOVA_var_D'},
           {'oldvarname':'agegroup', 'newvarname':'ANOVA_agegroup'},
           {'oldvarname':'alzheimerbroadcategory', 'newvarname':'ANOVA_alzheimerbroadcategory'},
           {'oldvarname':'gender', 'newvarname':'ANOVA_gender'},
           {'oldvarname':'lefthippocampus', 'newvarname':'ANOVA_lefthippocampus'}
           ]

        filePath = os.path.join(os.path.abspath(folderPath),file)
        with open (filePath, "r") as myfile:
            data = myfile.read()
        for var in varNamesReplacements:
            data=data.replace(var['oldvarname'],var['newvarname'])
        #Execute R script
        self.Test1Result, self.Test2Result, self.Test3Result, self.Test4Result, self.Test5Result,self.Test6Result, self.Test7Result, self.Test8Result, self.Test9Result, self.Test10Result, self.Test11Result, self.Test12Result, self.Test13Result, self.Test14Result, self.Test15Result, self.Test16Result, self.Test17Result = robjects.r(data)


    def test_ANOVA_1(self):

        logging.info("---------- TEST : ANOVA - Tests With Balanced data set(data_ANOVA_Balanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type I ANOVA.  ")

        data = [
                {   "name": "iterations_max_number", "value": "20" },
                {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
                {   "name": "y", "value": "ANOVA_var_D" },
                {   "name": "sstype", "value": "1" },
                {   "name": "dataset", "value": "ANOVA_Balanced_with_inter_V1V2" },
                {   "name": "filter", "value": "" },
                {   "name": "outputformat", "value": "pfa" }
            ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)

        resultsComparison(result['result'][0]['data'], json.loads(self.Test1Result))



    def test_ANOVA_2(self):

        logging.info("---------- TEST : ANOVA - Tests With Balanced data set(data_ANOVA_Balanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type II ANOVA.  ")

        data = [
                {   "name": "iterations_max_number", "value": "20" },
                {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
                {   "name": "y", "value": "ANOVA_var_D" },
                {   "name": "sstype", "value": "2" },
                {   "name": "dataset", "value": "ANOVA_Balanced_with_inter_V1V2" },
                {   "name": "filter", "value": "" },
                {   "name": "outputformat", "value": "pfa" }
            ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)

        resultsComparison(result['result'][0]['data'], json.loads(self.Test2Result))



    def test_ANOVA_3(self):
        logging.info("---------- TEST : ANOVA - Tests With Balanced data set(data_ANOVA_Balanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type III ANOVA.  ")

        data = [
                {   "name": "iterations_max_number", "value": "20" },
                {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
                {   "name": "y", "value": "ANOVA_var_D" },
                {   "name": "sstype", "value": "3" },
                {   "name": "dataset", "value": "ANOVA_Balanced_with_inter_V1V2" },
                {   "name": "filter", "value": "" },
                {   "name": "outputformat", "value": "pfa" }
            ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)

        resultsComparison(result['result'][0]['data'], json.loads(self.Test3Result))



    def test_ANOVA_4(self):
        logging.info("---------- TEST : ANOVA - Tests With Unbalanced data set(data_ANOVA_Unbalanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type I ANOVA.  ")

        data = [
                {   "name": "iterations_max_number", "value": "20" },
                {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
                {   "name": "y", "value": "ANOVA_var_D" },
                {   "name": "sstype", "value": "1" },
                {   "name": "dataset", "value": "ANOVA_UnBalanced_with_inter_V1V2" },
                {   "name": "filter", "value": "" },
                {   "name": "outputformat", "value": "pfa" }
            ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)

        resultsComparison(result['result'][0]['data'], json.loads(self.Test4Result))



    def test_ANOVA_5(self):
        logging.info("---------- TEST : ANOVA - Tests With Unbalanced data set(data_ANOVA_Unbalanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type II ANOVA.  ")

        data = [
                {   "name": "iterations_max_number", "value": "20" },
                {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
                {   "name": "y", "value": "ANOVA_var_D" },
                {   "name": "sstype", "value": "2" },
                {   "name": "dataset", "value": "ANOVA_UnBalanced_with_inter_V1V2" },
                {   "name": "filter", "value": "" },
                {   "name": "outputformat", "value": "pfa" }
            ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)

        resultsComparison(result['result'][0]['data'], json.loads(self.Test5Result))


    def test_ANOVA_6(self):
        logging.info("---------- TEST : ANOVA - Tests With Unbalanced data set(data_ANOVA_Unbalanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type III ANOVA.  ")

        data = [
                {   "name": "iterations_max_number", "value": "20" },
                {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
                {   "name": "y", "value": "ANOVA_var_D" },
                {   "name": "sstype", "value": "3" },
                {   "name": "dataset", "value": "ANOVA_UnBalanced_with_inter_V1V2" },
                {   "name": "filter", "value": "" },
                {   "name": "outputformat", "value": "pfa" }
            ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test6Result))


    def test_ANOVA_7(self):
        logging.info("---------- TEST : ANOVA - Tests With Unbalanced data set(data_ANOVA_Unbalanced_with_inter_V1V2.csv) - Test the 3 main effects only with type III ANOVA.  ")

        data = [
                {   "name": "iterations_max_number", "value": "20" },
                {   "name": "x", "value": "ANOVA_var_I1+ANOVA_var_I2+ANOVA_var_I3" },
                {   "name": "y", "value": "ANOVA_var_D" },
                {   "name": "sstype", "value": "3" },
                {   "name": "dataset", "value": "ANOVA_UnBalanced_with_inter_V1V2" },
                {   "name": "filter", "value": "" },
                {   "name": "outputformat", "value": "pfa" }
            ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)

        resultsComparison(result['result'][0]['data'], json.loads(self.Test7Result))


    def test_ANOVA_8(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - without interaction and 2 variables - type III ")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory+ANOVA_gender" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "3" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)

        resultsComparison(result['result'][0]['data'], json.loads(self.Test8Result))


    def test_ANOVA_9(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - without interaction and 2 variables - type II")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory+ANOVA_gender" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "2" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test9Result))


    def test_ANOVA_10(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with interaction and 2 variables - type III")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "3" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test10Result))




    def test_ANOVA_11(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with interaction and 2 variables - type II")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "2" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test11Result))


    def test_ANOVA_12(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - without interaction and 3 variables - type III")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory+ANOVA_gender+ANOVA_agegroup" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "3" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test12Result))


    def test_ANOVA_13(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - without interaction and 3 variables - type II")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory+ANOVA_gender+ANOVA_agegroup" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "2" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test13Result))


    def test_ANOVA_14(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with 1 interaction and 3 variables - type III")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender+ANOVA_agegroup" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "3" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test14Result))


    def test_ANOVA_15(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with 1 interaction and 3 variables - type II")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender+ANOVA_agegroup" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "2" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test15Result))



    def test_ANOVA_16(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with full interaction and 3 variables - type III")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender*ANOVA_agegroup" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "3" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test16Result))


    def test_ANOVA_17(self):
        logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with full interaction and 3 variables - type II")

        data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender*ANOVA_agegroup" },
            {   "name": "y", "value": "ANOVA_lefthippocampus" },
            {   "name": "sstype", "value": "2" },
            {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        result = json.loads(r.text)
        print (r.text)
        resultsComparison(result['result'][0]['data'], json.loads(self.Test17Result))


        def test_ANOVA_Privacy(self):
            logging.info("---------- TEST : Algorithms for Privacy Error")

            data = [
                    {   "name": "iterations_max_number", "value": "20" },
                    {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
                    {   "name": "y", "value": "ANOVA_var_D" },
                    {   "name": "sstype", "value": "1" },
                    {   "name": "dataset", "value": "adni_9rows" },
                    {   "name": "filter", "value": "" },
                    {   "name": "outputformat", "value": "pfa" }
                  ]

            headers = {'Content-type': 'application/json', "Accept": "text/plain"}
            r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)

            result = json.loads(r.text)

            check_privacy_result(r.text)

def resultsComparison(jsonExaremeResult, jsonRResult):

    for d in range(len(jsonRResult)):
        assert (jsonRResult[d]['name'].lower() == jsonExaremeResult[d]['modelvariables'].lower())
        assert (math.isclose(jsonExaremeResult[d]['sumofsquares'],jsonRResult[d]['ss'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[d]['ss'])).as_tuple().exponent))))
        assert (jsonRResult[d]['df'] == jsonRResult[d]['df'])
        assert (math.isclose(jsonExaremeResult[d]['meansquare'],jsonRResult[d]['ms'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[d]['ms'])).as_tuple().exponent))))
        if jsonExaremeResult[d]['modelvariables'] != 'residuals':
            assert (math.isclose(jsonExaremeResult[d]['f'],jsonRResult[d]['F'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[d]['F'])).as_tuple().exponent))))
            if type(jsonRResult[d]['p']) is str:
                assert (jsonExaremeResult[d]['p'] <= float(jsonRResult[d]['p'].replace('< ','0')))
            else:
                assert (math.isclose(jsonExaremeResult[d]['p'],jsonRResult[d]['p'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[d]['p'])).as_tuple().exponent))))
            assert (math.isclose(jsonExaremeResult[d]['etasquared'],jsonRResult[d]['etaSq'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[d]['etaSq'])).as_tuple().exponent))))
            assert (math.isclose(jsonExaremeResult[d]['partetasquared'],jsonRResult[d]['etaSqP'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResult[d]['etaSqP'])).as_tuple().exponent))))
            #assert math.isclose(omegaSquared,corr_omegaSquared,rel_tol=0,abs_tol=1e-06)


def check_privacy_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"


if __name__ == '__main__':
    unittest.main()
