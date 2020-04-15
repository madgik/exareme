import pytest
import json
from pathlib import Path
import numpy as np

from PCA import PCA
from mipframework import create_runner
from mipframework.runner.runner import capture_stdout


def get_test_params():
    path = Path(__file__).parent / "expected" / "pca_expected.json"
    with path.open() as json_expected:
        params = json.load(json_expected)["test_cases"]
    params = [(p["input"], p["output"]) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_pearson_algorithm_local(test_input, expected):
    alg_args = sum([["-" + p["name"], p["value"]] for p in test_input], [])
    runner = create_runner(
        PCA, alg_type="multiple-local-global", num_workers=1, algorithm_args=alg_args,
    )
    result = capture_stdout(runner.run)()
    result = json.loads(result)["result"][0]["data"]
    expected = expected[0]
    assert int(result["n_obs"]) == int(expected["n_obs"])
    assert np.isclose(result["eigenvalues"], expected["eigen_vals"], atol=1e-3).all()
    for u, v in zip(result["eigenvectors"], expected["eigen_vecs"]):
        assert are_collinear(u, v)


def are_collinear(u, v):
    cosine_similarity = np.dot(v, u) / (np.sqrt(np.dot(v, v)) * np.sqrt(np.dot(u, u)))
    return np.isclose(abs(cosine_similarity), 1, rtol=1e-5)
