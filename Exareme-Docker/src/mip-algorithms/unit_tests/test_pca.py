import pytest
import json
import requests
import math
import numpy as np
# from lib import vmUrl

endpointUrl = 'http://localhost:9090/mining/query/' + 'PCA'


def get_test_params():
    with open('runs/pca_expected.json') as json_file:
        params = json.load(json_file)['test_cases']
    params = [(p['input'], p['output']) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_eval(test_input, expected):
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    res = requests.post(endpointUrl, data=json.dumps(test_input), headers=headers)
    res = json.loads(res.text)
    res = res['result'][0]['data']
    expected = expected[0]

    assert math.isclose(res['n_obs'], expected['n_obs'], rel_tol=1e-5)
    assert np.isclose(res['eigen_vals'], expected['eigen_vals'], rtol=1e-5).all()
    for u, v in zip(res['eigen_vecs'], expected['eigen_vecs']):
        assert math.isclose(
                abs(np.dot(v, u) / (np.sqrt(np.dot(v, v)) * np.sqrt(np.dot(u, u)))),
                1,
                rel_tol=1e-5
        )
