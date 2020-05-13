import pytest
from pathlib import Path
import numpy as np

from mipframework.testutils import get_test_params, get_algorithm_result
from LOGISTIC_REGRESSION import LogisticRegression

expected_file = Path(__file__).parent / "expected" / "logistic_regression_expected.json"


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
