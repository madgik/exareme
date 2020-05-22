import numpy as np
import hypothesis
from hypothesis import given, settings, assume
from hypothesis.strategies import integers, floats, composite, text, tuples
from hypothesis.extra.numpy import arrays

from PEARSON_CORRELATION.pearson import get_local_sums
from PEARSON_CORRELATION.pearson import get_var_pair_names
from PEARSON_CORRELATION.pearson import get_correlation_and_pvalue
from PEARSON_CORRELATION.pearson import get_confidence_intervals

PROP_TESTING_MAX_EXAMPLES = 1000


@composite
def local_data(draw):
    n_obs = draw(integers(3, 10000))
    x_n_cols = draw(integers(1, 100))
    y_n_cols = draw(integers(1, 100))
    X = draw(arrays(np.float64, (n_obs, x_n_cols), elements=floats(-1e6, 1e6)))
    Y = draw(arrays(np.float64, (n_obs, y_n_cols), elements=floats(-1e6, 1e6)))
    # Assume no column is constant as Pearson correlation is not defined in this case
    assume(not any(len(set(column)) == 1 for column in X.T))
    assume(not any(len(set(column)) == 1 for column in Y.T))
    return X, Y


@settings(
    max_examples=PROP_TESTING_MAX_EXAMPLES,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
    deadline=None,
)
@given(local_data())
def test_get_local_sums(local_datum):
    sx, sxx, sxy, sy, syy = get_local_sums(*local_datum)
    assert not np.isnan(sx).any()
    assert not np.isnan(sxx).any()
    assert not np.isnan(sxy).any()
    assert not np.isnan(sy).any()
    assert not np.isnan(syy).any()
    assert not np.isinf(sx).any()
    assert not np.isinf(sxx).any()
    assert not np.isinf(sxy).any()
    assert not np.isinf(sy).any()
    assert not np.isinf(syy).any()
    assert len(sx) == len(sxx)
    assert len(sy) == len(syy)
    assert sxy.shape == (len(sy), len(sx))


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(
    x_names=arrays(object, tuples(integers(1, 100)), elements=text(min_size=1)),
    y_names=arrays(object, tuples(integers(1, 100)), elements=text(min_size=1)),
)
def test_get_var_pair_names(x_names, y_names):
    pair_names = get_var_pair_names(x_names, y_names)
    assert pair_names.shape == (len(y_names), len(x_names))
    assert all(isinstance(n, str) for n in pair_names.ravel())


@settings(
    max_examples=PROP_TESTING_MAX_EXAMPLES,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
    deadline=None,
)
@given(local_data())
def test_get_correlation_and_pvalue(local_datum):
    X, Y = local_datum
    sx, sxx, sxy, sy, syy = get_local_sums(X, Y)
    r, prob = get_correlation_and_pvalue(len(X), sx, sxx, sxy, sy, syy)
    assert not np.isnan(r).any()
    assert not np.isnan(prob).any()
    assert not np.isinf(r).any()
    assert not np.isinf(prob).any()
    assert r.shape == prob.shape
    assert all(-1 <= x <= 1 for x in r.ravel())
    assert all(0 <= x <= 1 for x in prob.ravel())


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(
    n_obs=integers(3, 10000),
    r=arrays(
        np.float, tuples(integers(1, 100), integers(1, 100)), elements=floats(-1, 1)
    ),
)
def test_get_confidence_intervals(n_obs, r):
    ci_hi, ci_lo = get_confidence_intervals(n_obs, r)
    assert not np.isnan(ci_hi).any()
    assert not np.isnan(ci_lo).any()
    assert not np.isinf(ci_hi).any()
    assert not np.isinf(ci_lo).any()
    assert len(ci_hi) == len(ci_lo)
