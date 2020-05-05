import requests
import unittest
import os
import json
import logging
import math
from decimal import *
import re

import rpy2.robjects as robjects

import sys
from os import path
sys.path.append(path.abspath(__file__))
from tests.lib import vmUrl
endpointUrl= vmUrl+'LINEAR_REGRESSION'
folderPath = 'R_scripts'
file ='LinearRegression.Rmd'

class TestLinearRegression(unittest.TestCase):
    def setUp(self):
        filePath = os.path.join(os.path.abspath(folderPath),file)
        with open (filePath, "r") as myfile:
            data = myfile.read()
        #Execute R script
        self.RResults =  json.loads(str(robjects.r(data)))
        print (self.RResults)

    def test_LinearRegression_1_1_dummycoding(self):
        logging.info("---------- TEST 1.1: Linear Regression, one categorical regressor,dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory"},
    		    { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"}]"},
    	        { "name": "encodingparameter", "value": "dummycoding"},
		        {  "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
    			{ "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)

        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[0][1]),json.loads(self.RResults[0][2]))



    def test_LinearRegression_1_1_simplecoding(self):
        logging.info("---------- TEST 1.1: Linear Regression, one categorical regressor,simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory"},
    		    { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"}]"},
    	        { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
    			{ "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[1][1]),json.loads(self.RResults[1][2]))


    def test_LinearRegression_1_2_dummycoding(self):
        logging.info("---------- TEST 1.2: Linear Regression, two categorical regressors without interaction,dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[2][1]),json.loads(self.RResults[2][2]))


    def test_LinearRegression_1_2_simplecoding(self):
        logging.info("---------- TEST 1.2: Linear Regression, two categorical regressors without interaction,simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}
                ]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[3][1]),json.loads(self.RResults[3][2]))


    def test_LinearRegression_1_2b_dummycoding(self):
        logging.info("---------- TEST 1.2b: Linear Regression, two categorical regressors with interaction,dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[4][1]),json.loads(self.RResults[4][2]))


    def test_LinearRegression_1_2b_simplecoding(self):
        logging.info("---------- TEST 1.2b: Linear Regression, two categorical regressors with interaction,simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[5][1]),json.loads(self.RResults[5][2]))


    def test_LinearRegression_1_3_dummycoding(self):
        logging.info("---------- TEST 1.3: Linear Regression, three categorical regressors without interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+agegroup"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[6][1]),json.loads(self.RResults[6][2]))


    def test_LinearRegression_1_3_simplecoding(self):
        logging.info("---------- TEST 1.3: Linear Regression, three categorical regressors without interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+agegroup"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[7][1]),json.loads(self.RResults[7][2]))


    def test_LinearRegression_1_3b_dummycoding(self):
        logging.info("---------- TEST 1.3b: Linear Regression, three categorical regressors with all interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*agegroup"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[8][1]),json.loads(self.RResults[8][2]))


    def test_LinearRegression_1_3b_simplecoding(self):
        logging.info("---------- TEST 1.3b: Linear Regression, three categorical regressors with all interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*agegroup"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[9][1]),json.loads(self.RResults[9][2]))

    def test_LinearRegression_2_1(self):
        logging.info("---------- TEST 2_1: Linear Regression, one continuous regressor")
        data = [{ "name": "x",	"value": "csfglobal"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[10][1]),json.loads(self.RResults[10][2]))


    def test_LinearRegression_2_2(self):
        logging.info("---------- TEST 2_2: Linear Regression, two continuous regressors without interaction")
        data = [{ "name": "x",	"value": "opticchiasm+minimentalstate"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[11][1]),json.loads(self.RResults[11][2]))

    def test_LinearRegression_2_2b(self):
        logging.info("---------- TEST 2_2: Linear Regression, two continuous regressors with interaction")
        data = [{ "name": "x",	"value": "opticchiasm*minimentalstate"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[12][1]),json.loads(self.RResults[12][2]))


    def test_LinearRegression_2_3(self):
        logging.info("---------- TEST 2_3: Linear Regression, three continuous regressors without interaction")
        data = [{ "name": "x",	"value": "opticchiasm+minimentalstate+subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[13][1]),json.loads(self.RResults[13][2]))

    def test_LinearRegression_2_3b(self):
        logging.info("---------- TEST 2_3: Linear Regression, three continuous regressors with interaction")
        data = [{ "name": "x",	"value": "opticchiasm*minimentalstate*subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
                { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[14][1]),json.loads(self.RResults[14][2]))

    def test_LinearRegression_3_1_dummycoding(self):
        logging.info("---------- TEST 3_1: Linear Regression, one categorical and one continuous regressors without interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[15][1]),json.loads(self.RResults[15][2]))

    def test_LinearRegression_3_1(self):
        logging.info("---------- TEST 3_1: Linear Regression, one categorical and one continuous regressors without interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[16][1]),json.loads(self.RResults[16][2]))

    def test_LinearRegression_3_1b_dummycoding(self):
        logging.info("---------- TEST 3_1b: Linear Regression, one categorical and one continuous regressors with interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[17][1]),json.loads(self.RResults[17][2]))


    def test_LinearRegression_3_1b_simplecoding(self):
        logging.info("---------- TEST 3_1b: Linear Regression, one categorical and one continuous regressors with interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[18][1]),json.loads(self.RResults[18][2]))

    def test_LinearRegression_3_2_dummycoding(self):
        logging.info("---------- TEST 3_2: Linear Regression, one categorical and two continuous regressors without interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+subjectage+opticchiasm"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[19][1]),json.loads(self.RResults[19][2]))

    def test_LinearRegression_3_2_simplecoding(self):
        logging.info("---------- TEST 3_2: Linear Regression, one categorical and two continuous regressors without interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+subjectage+opticchiasm"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[20][1]),json.loads(self.RResults[20][2]))

    def test_LinearRegression_3_2b_dummycoding(self):
        logging.info("---------- TEST 3_2b: Linear Regression, one categorical and two continuous regressors with interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*subjectage*opticchiasm"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[21][1]),json.loads(self.RResults[21][2]))


    def test_LinearRegression_3_2b_simplecoding(self):
        logging.info("---------- TEST 3_2b: Linear Regression, one categorical and two continuous regressors with interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*subjectage*opticchiasm"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[22][1]),json.loads(self.RResults[22][2]))

    def test_LinearRegression_3_3_dummycoding(self):
        logging.info("---------- TEST 3_3: Linear Regression, two categorical and one continuous regressors without interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+subjectage"},
                    { "name": "y",  "value": "lefthippocampus"},
                    { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                    { "name": "encodingparameter", "value": "dummycoding"},
		            { "name": "pathology","value":"dementia"},
                    { "name": "dataset", "value": "desd-synthdata"},
                    { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[23][1]),json.loads(self.RResults[23][2]))

    def test_LinearRegression_3_3_simplecoding(self):
        logging.info("---------- TEST 3_3: Linear Regression, two categorical and one continuous regressors without interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[24][1]),json.loads(self.RResults[24][2]))


    def test_LinearRegression_3_3b_dummycoding(self):
        logging.info("---------- TEST 3_3b: Linear Regression,two categorical and one continuous regressors with interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[25][1]),json.loads(self.RResults[25][2]))

    def test_LinearRegression_3_3b_simplecoding(self):
        logging.info("---------- TEST 3_3b: Linear Regression,two categorical and one continuous regressors with interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[26][1]),json.loads(self.RResults[26][2]))


    def test_LinearRegression_3_4_dummycoding(self):
        logging.info("---------- TEST 3_4: Linear Regression, two categorical and two continuous regressors without interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+brainstem+opticchiasm"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[27][1]),json.loads(self.RResults[27][2]))

    def test_LinearRegression_3_4_simplecoding(self):
        logging.info("---------- TEST 3_4: Linear Regression, two categorical and two continuous regressors without interaction, simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+brainstem+opticchiasm"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[28][1]),json.loads(self.RResults[28][2]))


    def test_LinearRegression_3_4b_dummycoding(self):
        logging.info("---------- TEST 3_4b: Linear Regression, two categorical and two continuous regressors with interaction, dummycoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "dummycoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[29][1]),json.loads(self.RResults[29][2]))

    def test_LinearRegression_3_4b_simplecoding(self):
        logging.info("---------- TEST 3_4b: Linear Regression, two categorical and two continuous regressors with interaction,simplecoding")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "simplecoding"},
		        { "name": "pathology","value":"dementia"},
                { "name": "dataset", "value": "desd-synthdata"},
                { "name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
        print (r.text)
        result = json.loads(r.text)
        resultsComparison(result['result'][0]['data']['coefficients'],result['result'][0]['data']['statistics'],  json.loads(self.RResults[30][1]),json.loads(self.RResults[30][2]))



def resultsComparison(jsonExaremeResultCoeff, jsonExaremeResultStats, jsonRResultCoeff, jsonRResultStats):

    print ("E1", jsonExaremeResultCoeff)
    print ("E2", jsonExaremeResultStats)
    print ("R1", jsonRResultCoeff)
    print ("R2", jsonRResultStats)
    noofcoefficient = 0
    for i in range(len(jsonRResultCoeff)):
        noofcoefficient = noofcoefficient + 1
        exist_corr_c = False
        for j in range (len(jsonExaremeResultCoeff)):
            ExaremePredictor= re.sub("[()_]", '', jsonExaremeResultCoeff[j]['predictor'].lower())
            RPredictor= re.sub("[()_]", '', jsonRResultCoeff[i]['rn'].lower())
            print (ExaremePredictor,RPredictor)
            if (ExaremePredictor == RPredictor):
                exist_corr_c = True
                print(ExaremePredictor ,RPredictor,'estimate',jsonExaremeResultCoeff[j]['estimate'],jsonRResultCoeff[i]['Estimate'])
                assert math.isclose(float(jsonExaremeResultCoeff[j]['estimate']),jsonRResultCoeff[i]['Estimate'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultCoeff[i]['Estimate'])).as_tuple().exponent)))
                print('stderror')
                assert math.isclose(float(jsonExaremeResultCoeff[j]['stderror']),jsonRResultCoeff[i]['Std. Error'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultCoeff[i]['Std. Error'])).as_tuple().exponent)))
                print('tvalue')
                assert math.isclose(float(jsonExaremeResultCoeff[j]['tvalue']),jsonRResultCoeff[i]['t value'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultCoeff[i]['t value'])).as_tuple().exponent)))
                print('Pr')
                if type(jsonRResultCoeff[i]['Pr(>|t|)']) is str:
                    # jsonRResultCoeff[i]['Pr(>|t|)'].replace(' ','')

                    assert (float(jsonExaremeResultCoeff[j]['prvalue']) < float(jsonRResultCoeff[i]['Pr(>|t|)'].replace('<','')))
                else:
                    assert (math.isclose(float(jsonExaremeResultCoeff[j]['prvalue']),jsonRResultCoeff[i]['Pr(>|t|)'],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultCoeff[i]['Pr(>|t|)'])).as_tuple().exponent))))
        print ('c')
        assert exist_corr_c==True
    assert noofcoefficient == len(jsonRResultCoeff)


    for key in jsonRResultStats[0]:
        if key == 'sigma':
            for i in range(len(jsonExaremeResultStats)):
                if str(jsonExaremeResultStats[i]['name']) == 'residual_standard_error':
                    assert math.isclose(float(jsonExaremeResultStats[i]['value']),jsonRResultStats[0][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultStats[0][key])).as_tuple().exponent)))
        elif key == 'df':
            for i in range(len(jsonExaremeResultStats)):
                if jsonExaremeResultStats[i]['name'] == 'degrees_of_freedom':
                    assert math.isclose(float(jsonExaremeResultStats[i]['value']),jsonRResultStats[0][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultStats[0][key])).as_tuple().exponent)))
        elif key == 'r_squared':
            for i in range(len(jsonExaremeResultStats)):
                if jsonExaremeResultStats[i]['name'] == 'R-squared':
                    assert math.isclose(float(jsonExaremeResultStats[i]['value']),jsonRResultStats[0][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultStats[0][key])).as_tuple().exponent)))
        elif key == 'adj_r_squared':
            for i in range(len(jsonExaremeResultStats)):
                if jsonExaremeResultStats[i]['name'] == 'adjusted-R':
                    assert math.isclose(float(jsonExaremeResultStats[i]['value']),jsonRResultStats[0][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultStats[0][key])).as_tuple().exponent)))
        elif key == 'fstatistic':
            for i in range(len(jsonExaremeResultStats)):
                if jsonExaremeResultStats[i]['name'] == 'f-statistic':
                    assert math.isclose(float(jsonExaremeResultStats[i]['value']),jsonRResultStats[0][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultStats[0][key])).as_tuple().exponent)))
        elif key == 'numdf':
            for i in range(len(jsonExaremeResultStats)):
                if jsonExaremeResultStats[i]['name'] == 'variables_number':
                    assert math.isclose(float(jsonExaremeResultStats[i]['value']),jsonRResultStats[0][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultStats[0][key])).as_tuple().exponent)))
        elif key == 'minresiduals':
            for i in range(len(jsonExaremeResultStats)):
                if jsonExaremeResultStats[i]['name'] == 'residual_min':
                    assert math.isclose(float(jsonExaremeResultStats[i]['value']),jsonRResultStats[0][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultStats[0][key])).as_tuple().exponent)))
        elif key == 'maxresiduals':
            for i in range(len(jsonExaremeResultStats)):
                if jsonExaremeResultStats[i]['name'] == 'residual_max':
                    assert math.isclose(float(jsonExaremeResultStats[i]['value']),jsonRResultStats[0][key],rel_tol=0,abs_tol=10**(-abs(Decimal(str(jsonRResultStats[0][key])).as_tuple().exponent)))
        else:
            assert False





if __name__ == '__main__':
    unittest.main()
