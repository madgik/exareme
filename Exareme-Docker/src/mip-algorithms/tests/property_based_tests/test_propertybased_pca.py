import numpy as np
import hypothesis
from hypothesis import given, settings, assume
from hypothesis.strategies import integers, floats, composite, tuples
from hypothesis.extra.numpy import arrays, array_shapes

from PCA.pca import get_local_sums
from PCA.pca import get_moments
from PCA.pca import get_standardized_gramian
from PCA.pca import get_eigenstuff

PROP_TESTING_MAX_EXAMPLES = 1000


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(
    X=arrays(
        np.float64,
        tuples(integers(3, 10000), integers(1, 100)),
        elements=floats(-1e6, 1e6),
    )
)
def test_get_local_sums(X):
    sx, sxx = get_local_sums(X)
    assert not np.isnan(sx).any()
    assert not np.isnan(sxx).any()
    assert not np.isinf(sx).any()
    assert not np.isinf(sxx).any()
    assert len(sx) == len(sxx)


@composite
def moments(draw):
    n_obs = draw(integers(3, 10000))
    n_cols = draw(integers(1, 100))
    sx = draw(arrays(np.float64, (n_cols,), elements=floats(-1e6, 1e6)))
    sxx = draw(arrays(np.float64, (n_cols,), elements=floats(-1e6, 1e6)))
    return n_obs, sx, sxx


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(moments())
def test_get_moments(moment):
    n_obs, sx, sxx = moment
    assume(((sxx - n_obs * (sx / n_obs) ** 2) >= 0).all())
    means, sigmas = get_moments(n_obs, sx, sxx)
    assert not np.isnan(means).any()
    assert not np.isnan(sigmas).any()
    assert not np.isinf(means).any()
    assert not np.isinf(sigmas).any()


@settings(
    max_examples=PROP_TESTING_MAX_EXAMPLES,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
    deadline=None,
)
@given(
    X=arrays(
        np.float64,
        tuples(integers(3, 10000), integers(1, 100)),
        elements=floats(-1e6, 1e6),
    )
)
def test_get_standardized_gramian(X):
    assume(not any(len(set(column)) == 1 for column in X.T))
    sx, sxx = get_local_sums(X)
    means, sigmas = get_moments(len(X), sx, sxx)
    gramian = get_standardized_gramian(X, means, sigmas)
    assert not np.isnan(gramian).any()
    assert not np.isinf(gramian).any()
    assert gramian.shape[0] == gramian.shape[1]


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(
    n_obs=integers(2, 10000),
    gramian=arrays(
        np.float64,
        array_shapes(min_dims=2, max_dims=2, min_side=1, max_side=100).filter(
            lambda x: x[0] == x[1]
        ),
        elements=floats(-1e6, 1e6),
    ),
)
def test_get_eigenstuff(gramian, n_obs):
    eigenvalues, eigenvectors = get_eigenstuff(gramian, n_obs)
    assert not np.isnan(eigenvalues).any()
    assert not np.isnan(eigenvectors).any()
    assert not np.isinf(eigenvalues).any()
    assert not np.isinf(eigenvectors).any()
    assert len(eigenvalues) == len(eigenvectors)
