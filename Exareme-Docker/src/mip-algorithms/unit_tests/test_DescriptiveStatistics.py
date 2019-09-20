import pytest
import json
import requests
import math
import logging

endpointUrl = 'http://88.197.53.38:9090/mining/query/DESCRIPTIVE_STATS'


def get_test_params():
    with open('runs/descr_stats_runs.json') as json_file:
        params = json.load(json_file)['results']
    params = [(p['input'], p['output']) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_eval(test_input, expected):
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    res = requests.post(endpointUrl, data=json.dumps(test_input), headers=headers)
    res = json.loads(res.text)
    res = res['result'][0]['data']['data'][0]
    for key, val in expected.items():
        test_val = res[key]
        if type(val) == float:
            assert math.isclose(val, test_val, rel_tol=0, abs_tol=1e-03)
        else:
            assert val == test_val

def test_Descriptive_Statistics_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{
        "name": "x",
        "value": "leftmpogpostcentralgyrusmedialsegment"
        },
        {
            "name": "dataset",
            "value": "adni_9rows"
        },
        {
            "name": "filter",
            "value": ""
        },
        {
            "name": "pathology",
            "value": "dementia"
        }
            ]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)

def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The Experiment could not run with the input provided because there are insufficient data.\",\"type\":\"text/plain+warning\"}]}"
