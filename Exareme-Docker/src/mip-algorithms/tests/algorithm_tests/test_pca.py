import pytest
from pathlib import Path
import numpy as np

from mipframework.testutils import get_test_params, get_algorithm_result
from PCA import PCA

expected_file = Path(__file__).parent / "expected" / "pca_expected.json"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80))
)
def test_pca_algorithm_local(test_input, expected):
    result = get_algorithm_result(PCA, test_input, num_workers=1)

    assert int(result["n_obs"]) == int(expected["n_obs"])
    assert np.isclose(result["eigenvalues"], expected["eigen_vals"], atol=1e-3).all()
    for u, v in zip(result["eigenvectors"], expected["eigen_vecs"]):
        assert are_collinear(u, v)


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80, 90))
)
def test_pca_algorithm_federated(test_input, expected):
    result = get_algorithm_result(PCA, test_input, num_workers=10)

    assert int(result["n_obs"]) == int(expected["n_obs"])
    assert np.isclose(result["eigenvalues"], expected["eigen_vals"], atol=1e-3).all()
    for u, v in zip(result["eigenvectors"], expected["eigen_vecs"]):
        assert are_collinear(u, v)


def are_collinear(u, v):
    cosine_similarity = np.dot(v, u) / (np.sqrt(np.dot(v, v)) * np.sqrt(np.dot(u, u)))
    return np.isclose(abs(cosine_similarity), 1, rtol=1e-5)
