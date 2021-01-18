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
    result = get_algorithm_result(Anova, test_input, num_workers=1)
    aov = result["anova_table"]
    tukey = result["tukey_table"]
    e_aov = {k: v for k, v in expected.items() if k != "tukey_test"}
    e_tukey = expected["tukey_test"]
    assert set(e_aov) == set(aov.keys())
    for key, e_val in e_aov.items():
        r_val = aov[key]
        assert np.isclose(e_val, r_val)
    for et, rt in zip(e_tukey, tukey):
        for key, e_val in et.items():
            r_val = rt[key]
            assert e_val == r_val or np.isclose(e_val, r_val)


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80, 95))
)
def test_anova_algorithm_federated(test_input, expected):
    result = get_algorithm_result(Anova, test_input, num_workers=10)
    aov = result["anova_table"]
    tukey = result["tukey_table"]
    e_aov = {k: v for k, v in expected.items() if k != "tukey_test"}
    e_tukey = expected["tukey_test"]
    assert set(e_aov) == set(aov.keys())
    for key, e_val in e_aov.items():
        r_val = aov[key]
        assert np.isclose(e_val, r_val)
    for et, rt in zip(e_tukey, tukey):
        for key, e_val in et.items():
            r_val = rt[key]
            assert e_val == r_val or np.isclose(e_val, r_val)
