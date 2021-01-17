import json

import numpy as np
import pytest
import requests
from mipframework.testutils import get_test_params
from tests import vm_url
from tests.algorithm_tests.test_anova import expected_file

headers = {"Content-type": "application/json", "Accept": "text/plain"}
url = vm_url + "ANOVA_ONEWAY"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(95, 100))
)
def test_pearson_algorithm_exareme(test_input, expected):
    result = requests.post(url, data=json.dumps(test_input), headers=headers)
    result = json.loads(result.text)
    result = result["result"][0]["data"]

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
