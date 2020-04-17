import pytest
from pathlib import Path
import numpy as np

from mipframework.testutils import get_test_params, get_algorithm_result
from PEARSON_CORRELATION import Pearson

expected_file = Path(__file__).parent / "expected" / "pearson_expected.json"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(85))
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
    "test_input, expected", get_test_params(expected_file, slice(85, 95))
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
