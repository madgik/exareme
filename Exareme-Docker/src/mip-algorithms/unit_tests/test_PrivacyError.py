import requests
import json
import logging

# Required datasets: adni_9rows

endpointUrl = 'http://88.197.53.100:9090'

def test_Privacy_Error_PEARSON_CORRELATION():
    """
    
    """

    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [{"name" : "x","value": "lefthippocampus"},
            {"name" : "y","value": "righthippocampus"},
            {"name" : "dataset","value": "adni_9rows"},
            {"name" : "filter","value": ""},
    	  ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(r.text)


def test_Privacy_Error_ANOVA():
    """
    
    """

    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
            {   "name": "y", "value": "ANOVA_var_D" },
            {   "name": "type", "value": "1" },
            {   "name": "dataset", "value": "adni_9rows" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
          ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/ANOVA', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(r.text)


def test_Privacy_Error_LINEAR_REGRESSION():
    """
    
    """

    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}
           ]


    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/LINEAR_REGRESSION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(r.text)


def check_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"




