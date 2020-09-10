import requests
import os
import json
import logging
import sys

sys.path.insert(0, "../")
from tests import vm_url


def test_valueEnumerationsParameter():

    endpointUrl1 = vm_url + "LINEAR_REGRESSION"

    logging.info("---------- TEST : valueEnumerations throwing error.")
    data = [
        {"name": "x", "value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
        {"name": "y", "value": "lefthippocampus"},
        {"name": "referencevalues", "value": '[{"name":"gender","val":"M"}]'},
        {"name": "encodingparameter", "value": "abcd"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "desd-synthdata"},
        {"name": "filter", "value": ""},
    ]
    headers = {"Content-type": "application/json", "Accept": "text/plain"}
    r = requests.post(endpointUrl1, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    assert (
        r.text
        == '{"result" : [{"data":"The value \'abcd\' of the parameter \'encodingparameter\' is not included in the valueEnumerations [dummycoding, sumscoding, simplecoding] .","type":"text/plain+user_error"}]}'
    )


def test_parameter_max_value():

    endpointUrl = vm_url + "ANOVA"

    logging.info("---------- TEST : Algorithms for User Error")
    data = [
        {"name": "iterations_max_number", "value": "20"},
        {"name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3"},
        {"name": "y", "value": "ANOVA_var_D"},
        {"name": "sstype", "value": "5"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "ANOVA_Balanced_with_inter_V1V2"},
        {"name": "filter", "value": ""},
        {"name": "outputformat", "value": "pfa"},
    ]
    headers = {"Content-type": "application/json", "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)

    assert (
        r.text
        == '{"result" : [{"data":"The value(s) of the parameter \'sstype\' should be less than 3.0 .","type":"text/plain+user_error"}]}'
    )


if __name__ == "__main__":
    unittest.main()
