import json

import pytest
import requests
from pathlib import Path
import numpy as np

from mipframework.testutils import get_test_params, get_algorithm_result
from tests import vm_url
from LOGISTIC_REGRESSION import LogisticRegression

expected_file = Path(__file__).parent / "expected" / "logistic_regression_expected.json"
headers = {"Content-type": "application/json", "Accept": "text/plain"}
url = vm_url + "LOGISTIC_REGRESSION"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80))
)
def test_logistic_regression_algorithm_local(test_input, expected):
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert (
        np.isclose(result["Coefficients"], expected["coeff"], rtol=1e-3).all()
        or np.isclose(result["Coefficients"], expected["coeff"], atol=1e-3).all()
    )


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80, 95))
)
def test_logistic_regression_algorithm_federated(test_input, expected):
    result = get_algorithm_result(LogisticRegression, test_input, 10)

    assert (
        np.isclose(result["Coefficients"], expected["coeff"], rtol=1e-3).all()
        or np.isclose(result["Coefficients"], expected["coeff"], atol=1e-3).all()
    )


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(95, 100))
)
def test_logistic_regression_algorithm_exareme(test_input, expected):
    result = requests.post(url, data=json.dumps(test_input), headers=headers)
    result = json.loads(result.text)
    result = result["result"][0]["data"]

    assert (
        np.isclose(result["Coefficients"], expected["coeff"], rtol=1e-3).all()
        or np.isclose(result["Coefficients"], expected["coeff"], atol=1e-3).all()
    )
