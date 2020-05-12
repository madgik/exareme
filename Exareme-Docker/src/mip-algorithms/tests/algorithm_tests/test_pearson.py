import json

import pytest
import requests
from pathlib import Path
import numpy as np

from mipframework.testutils import get_test_params, get_algorithm_result
from tests import vm_url
from PEARSON_CORRELATION import Pearson

expected_file = Path(__file__).parent / "expected" / "pearson_expected.json"
headers = {"Content-type": "application/json", "Accept": "text/plain"}
url = vm_url + "PEARSON_CORRELATION"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80))
)
def test_pearson_algorithm_local(test_input, expected):
    result = get_algorithm_result(Pearson, test_input, num_workers=1)

    assert np.isclose(
        result["Pearson correlation coefficient"],
        expected["Pearson correlation coefficient"],
        atol=1e-3,
    ).all()
    assert np.isclose(result["p-value"], expected["p-value"], atol=1e-3).all()
    assert int(result["n_obs"]) == int(expected["n_obs"])


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80, 95))
)
def test_pearson_algorithm_federated(test_input, expected):
    result = get_algorithm_result(Pearson, test_input, num_workers=10)

    assert np.isclose(
        result["Pearson correlation coefficient"],
        expected["Pearson correlation coefficient"],
        atol=1e-3,
    ).all()
    assert np.isclose(result["p-value"], expected["p-value"], atol=1e-3).all()
    assert int(result["n_obs"]) == int(expected["n_obs"])


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(95, 100))
)
def test_pearson_algorithm_exareme(test_input, expected):
    result = requests.post(url, data=json.dumps(test_input), headers=headers)
    result = json.loads(result.text)
    result = result["result"][0]["data"]

    assert np.isclose(
        result["Pearson correlation coefficient"],
        expected["Pearson correlation coefficient"],
        atol=1e-3,
    ).all()
    assert np.isclose(result["p-value"], expected["p-value"], atol=1e-3).all()
    assert int(result["n_obs"]) == int(expected["n_obs"])
