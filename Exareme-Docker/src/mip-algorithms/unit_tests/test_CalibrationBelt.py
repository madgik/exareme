import pytest
import json
import requests
import math
import logging

endpointUrl = 'http://localhost:9090/mining/query/CALIBRATION_BELT'


def get_test_params():
    with open('runs/calibration_expected_io.json') as json_file:
        params = json.load(json_file)['results']
    params = [(p['input'], p['output']) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_eval(test_input, expected):
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    if expected[0] is None:
        assert True
    else:
        res = requests.post(endpointUrl, data=json.dumps(test_input), headers=headers)
        res = json.loads(res.text)
        res = res['result'][0]['data'][0]
        expected = expected[0]
        assert math.isclose(res['n_obs'], expected['n_obs'], rel_tol=1e-5)
        assert math.isclose(res['Model degree'], expected['Model degree'], rel_tol=1e-5)
        assert math.isclose(res['p value'], expected['p value'], rel_tol=1e-5)

    # for i, e in enumerate(expected):
    #     for key, val in e.items():
    #         print(key, val)
    #         test_val = res[i][key]
    #         if type(val) == float:
    #             assert math.isclose(val, test_val, rel_tol=0, abs_tol=1e-03)
    #         else:
    #             assert val == test_val
