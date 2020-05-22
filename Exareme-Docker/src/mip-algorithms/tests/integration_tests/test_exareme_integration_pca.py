import json

import numpy as np
import pytest
import requests
from mipframework.testutils import get_test_params
from tests import vm_url
from tests.algorithm_tests.test_pca import expected_file, are_collinear

headers = {"Content-type": "application/json", "Accept": "text/plain"}
url = vm_url + "PCA"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(95, 100))
)
def test_pca_algorithm_exareme(test_input, expected):
    result = requests.post(url, data=json.dumps(test_input), headers=headers)
    result = json.loads(result.text)
    result = result["result"][0]["data"]

    assert int(result["n_obs"]) == int(expected["n_obs"])
    assert np.isclose(result["eigenvalues"], expected["eigen_vals"], atol=1e-3).all()
    for u, v in zip(result["eigenvectors"], expected["eigen_vecs"]):
        assert are_collinear(u, v)
