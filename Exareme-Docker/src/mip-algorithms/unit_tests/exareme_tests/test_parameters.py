import requests
import os
import json
import logging

endpointUrl='http://88.197.53.100:9090/mining/query/LINEAR_REGRESSION'

def test_valueEnumerationsParameter():
    logging.info("---------- TEST : valueEnumerations throwing error.")
    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "abcd"},
            { "name": "pathology","value":"dementia"},
            { "name": "dataset", "value": "desd-synthdata"},
            { "name": "filter", "value": ""}]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    assert r.text == "{\"result\" : [{\"data\":\"The value 'abcd' of the parameter 'encodingparameter' is not included in the valueEnumerations [dummycoding, sumscoding, simplecoding] .\",\"type\":\"text/plain+error\"}]}"


def test_valueMinParameter():
    logging.info("---------- TEST : valueMin throwing error.")
    data = [
        {
            "name" : "x",
            "value": "leftententorhinalarea_logreg_test, rightententorhinalarea_logreg_test, lefthippocampus_logreg_test, righthippocampus_logreg_test"
        },
        {
            "name" : "y",
            "value": "alzheimerbroadcategory_logreg_test"
        },
        {
            "name": "pathology",
            "value": "dementia"
         },
        {
            "name" : "dataset",
            "value": "data_logisticRegression"
        },
        {
            "name" : "filter",
            "value": ""
        },
        {
            "name" : "max_iter",
            "value": "0"
        }
    ]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    assert r.text == "{\"result\" : [{\"data\":\"The value(s) of the parameter 'max_iter' should be greater than 1.0 .\",\"type\":\"text/plain+error\"}]}"
    

def test_valueMaxParameter():
    logging.info("---------- TEST : valueMax throwing error.")
    data = [
        {
            "name" : "x",
            "value": "leftententorhinalarea_logreg_test, rightententorhinalarea_logreg_test, lefthippocampus_logreg_test, righthippocampus_logreg_test"
        },
        {
            "name" : "y",
            "value": "alzheimerbroadcategory_logreg_test"
        },
        {
            "name": "pathology",
            "value": "dementia"
         },
        {
            "name" : "dataset",
            "value": "data_logisticRegression"
        },
        {
            "name" : "filter",
            "value": ""
        },
        {
            "name" : "max_iter",
            "value": "101"
        }
    ]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    assert r.text == "{\"result\" : [{\"data\":\"The value(s) of the parameter 'max_iter' should be less than 100.0 .\",\"type\":\"text/plain+error\"}]}"
    


if __name__ == '__main__':
    unittest.main()
