import pytest
import json
import requests
import math
from tests.algorithm_tests.lib import vmUrl
endpointUrl= vmUrl+'CALIBRATION_BELT'

def get_test_params():
    with open('expected/calibration_expected_io.json') as json_file:
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

        assert math.isclose(res['n_obs'], expected['n_obs'], abs_tol=1e-2)
        assert math.isclose(res['Model degree'], expected['Model degree'], abs_tol=1e-2)
        assert math.isclose(res['p value'], expected['p value'], abs_tol=1e-2)
