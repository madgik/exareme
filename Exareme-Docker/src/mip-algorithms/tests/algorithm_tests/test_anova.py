import pytest
from pathlib import Path
import numpy as np

from mipframework.testutils import get_test_params, get_algorithm_result
from ANOVA_ONEWAY import Anova

expected_file = Path(__file__).parent / "expected" / "anova_expected.json"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80))
)
def test_anova_algorithm_local(test_input, expected):
    result = get_algorithm_result(Anova, test_input, num_workers=1)["anova_table"]
    assert set(expected.keys()) == set(result.keys())
    for key, e_val in expected.items():
        r_val = result[key]
        assert np.isclose(e_val, r_val)


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80, 95))
)
def test_anova_algorithm_federated(test_input, expected):
    result = get_algorithm_result(Anova, test_input, num_workers=10)["anova_table"]
    assert set(expected.keys()) == set(result.keys())
    for key, e_val in expected.items():
        r_val = result[key]
        assert np.isclose(e_val, r_val)
