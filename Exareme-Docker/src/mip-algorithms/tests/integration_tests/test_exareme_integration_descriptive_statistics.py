import json

import pytest
import requests
from mipframework.testutils import get_test_params
from tests import vm_url
from tests.algorithm_tests.test_descriptive_statistics import (
    expected_file,
    recursive_isclose,
)

headers = {"Content-type": "application/json", "Accept": "text/plain"}
url = vm_url + "DESCRIPTIVE_STATS"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(95, 100))
)
def test_descriptive_stats_algorithm_exareme(test_input, expected):
    result = requests.post(url, data=json.dumps(test_input), headers=headers)
    result = json.loads(result.text)
    result = result["result"][0]["data"]

    recursive_isclose(result, expected)
