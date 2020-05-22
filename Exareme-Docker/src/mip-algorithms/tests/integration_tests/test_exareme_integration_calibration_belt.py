import json

import numpy as np
import pytest
import requests
from mipframework.testutils import get_test_params
from tests import vm_url
from tests.algorithm_tests.test_calibration_belt import expected_file

headers = {"Content-type": "application/json", "Accept": "text/plain"}
url = vm_url + "CALIBRATION_BELT"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(33, 38))
)
def test_calibrationbelt_algorithm_exareme(test_input, expected):
    result = requests.post(url, data=json.dumps(test_input), headers=headers)
    result = json.loads(result.text)
    result = result["result"][0]["data"]

    expected = expected[0]
    assert int(result["n_obs"]) == int(expected["n_obs"])
    assert int(result["Model degree"]) == int(expected["Model degree"])
    assert np.isclose(result["p value"], expected["p value"], atol=1e-3)
