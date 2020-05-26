from numbers import Number

import numpy as np
import pytest
from DESCRIPTIVE_STATS import DescriptiveStats
from mipframework.testutils import get_test_params, get_algorithm_result
from pathlib import Path

expected_file = Path(__file__).parent / "expected" / "descriptive_stats_expected.json"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80))
)
def test_descriptive_stats_algorithm_local(test_input, expected):
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)

    recursive_isclose(result, expected)


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80, 95))
)
def test_descriptive_stats_algorithm_federated(test_input, expected):
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=10)

    recursive_isclose(result, expected)


def recursive_isclose(first, second):
    assert_same_type(first, second)
    if isinstance(first, dict):
        assert sorted(first.keys()) == sorted(second.keys())
        for key in first.keys():
            recursive_isclose(first[key], second[key])
    elif isinstance(first, list):
        assert np.isclose(first, second, rtol=1e-6).all()
    elif isinstance(first, Number):
        assert np.isclose(first, second, rtol=1e-6)
    elif isinstance(first, basestring):
        assert first == second


def assert_same_type(first, second):
    assert (
        (isinstance(first, dict) and isinstance(second, dict))
        or (isinstance(first, list) and isinstance(second, list))
        or (isinstance(first, Number) and isinstance(second, Number))
    )
