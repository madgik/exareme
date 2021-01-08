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

    # There is no way to choose which level will be the positive and which the
    # negative level in sklearn's LogisticRegression. Sometimes the choice it
    # makes agrees with our choice and sometimes it doesn't. In the latter case
    # the coefficients lie on the same axis but have opposite orientation,
    # hence we only check if the two results are collinear.
    assert are_collinear(result["Coefficients"], expected["coeff"])


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80, 95))
)
def test_logistic_regression_algorithm_federated(test_input, expected):
    result = get_algorithm_result(LogisticRegression, test_input, 10)

    assert are_collinear(result["Coefficients"], expected["coeff"])


def are_collinear(u, v):
    cosine_similarity = np.dot(v, u) / (np.sqrt(np.dot(v, v)) * np.sqrt(np.dot(u, u)))
    return np.isclose(abs(cosine_similarity), 1, rtol=1e-5)
