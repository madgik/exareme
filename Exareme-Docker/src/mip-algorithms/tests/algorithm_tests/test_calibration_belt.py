import json

import pytest
import requests
from pathlib import Path
import numpy as np

from mipframework.testutils import get_test_params, get_algorithm_result
from tests import vm_url
from CALIBRATION_BELT import CalibrationBelt

expected_file = Path(__file__).parent / "expected" / "calibration_belt_expected.json"
headers = {"Content-type": "application/json", "Accept": "text/plain"}
url = vm_url + "CALIBRATION_BELT"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(28))
)
def test_calibrationbelt_algorithm_local(test_input, expected):
    result = get_algorithm_result(CalibrationBelt, test_input, num_workers=1)

    expected = expected[0]
    assert int(result["n_obs"]) == int(expected["n_obs"])
    assert int(result["Model degree"]) == int(expected["Model degree"])
    assert np.isclose(result["p value"], expected["p value"], atol=1e-3)


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(28, 33))
)
def test_calibrationbelt_algorithm_federated(test_input, expected):
    result = get_algorithm_result(CalibrationBelt, test_input, num_workers=2)

    expected = expected[0]
    assert int(result["n_obs"]) == int(expected["n_obs"])
    assert int(result["Model degree"]) == int(expected["Model degree"])
    assert np.isclose(result["p value"], expected["p value"], atol=1e-3)


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
