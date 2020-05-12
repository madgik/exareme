import json
from numbers import Number

import numpy as np
import pytest
import requests
from DESCRIPTIVE_STATS import DescriptiveStats
from mipframework.testutils import get_test_params, get_algorithm_result
from pathlib import Path
from tests import vm_url

expected_file = Path(__file__).parent / "expected" / "descriptive_stats_expected.json"
headers = {"Content-type": "application/json", "Accept": "text/plain"}
url = vm_url + "DESCRIPTIVE_STATS"


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


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(95, 100))
)
def test_descriptive_stats_algorithm_exareme(test_input, expected):
    result = requests.post(url, data=json.dumps(test_input), headers=headers)
    result = json.loads(result.text)
    result = result["result"][0]["data"]

    recursive_isclose(result, expected)


def recursive_isclose(first, second):
    assert_same_type(first, second)
    if isinstance(first, dict):
        assert sorted(first.keys()) == sorted(second.keys())
        for key in first.keys():
            recursive_isclose(first[key], second[key])
    elif isinstance(first, list):
        assert np.isclose(first, second, rtol=1e-6).all()
    else:
        assert np.isclose(first, second, rtol=1e-6)


def assert_same_type(first, second):
    assert (
        (isinstance(first, dict) and isinstance(second, dict))
        or (isinstance(first, list) and isinstance(second, list))
        or (isinstance(first, Number) and isinstance(second, Number))
    )
