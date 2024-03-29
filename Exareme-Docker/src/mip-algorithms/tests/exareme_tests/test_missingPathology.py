import requests
import os
import json
import logging
import sys

sys.path.insert(0, "../")
from tests import linear_regression_url


def test_LINEAR_REGRESSION():
    logging.info("---------- TEST : Algorithms for User Error")
    data = [
        {"name": "x", "value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
        {"name": "y", "value": "lefthippocampus"},
        {"name": "referencevalues", "value": '[{"name":"gender","val":"M"}]'},
        {"name": "encodingparameter", "value": "simplecoding"},
        {"name": "pathology", "value": ""},
        {"name": "dataset", "value": "desd-synthdata"},
        {"name": "filter", "value": ""},
    ]
    headers = {"Content-type": "application/json", "Accept": "text/plain"}
    r = requests.post(linear_regression_url, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_result(r.text)


def check_result(result):
    assert (
        result
        == '{"result" : [{"data":"Please provide a pathology.","type":"text/plain+user_error"}]}'
    )


if __name__ == "__main__":
    unittest.main()
