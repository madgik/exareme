from collections import Counter
import numpy as np
import hypothesis
from hypothesis import given, settings, assume
from hypothesis.strategies import integers, floats
from hypothesis.extra.numpy import arrays

from LOGISTIC_REGRESSION.logistic_regression import update_local_model_parameters
from LOGISTIC_REGRESSION.logistic_regression import update_coefficients
from LOGISTIC_REGRESSION.logistic_regression import compute_classification_results


@settings(suppress_health_check=[hypothesis.HealthCheck.filter_too_much,
                                 hypothesis.HealthCheck.too_slow,
                                 hypothesis.HealthCheck.data_too_large])
@given(X=arrays(np.float64, (100, 10), elements=floats(-1e6, 1e6)),
       y=arrays(np.int32, (100,), elements=integers(0, 1)),
       coeff=arrays(np.float64, (10,), elements=floats(-1e6, 1e6)))
def test_update_local_model_params_not_nan(X, y, coeff):
    grad, hess, ll = update_local_model_parameters(X, y, coeff)
    count = Counter(y)
    assume(count[0] > len(coeff) and count[1] > len(coeff))
    assert not np.isnan(grad).any()
    assert not np.isnan(hess).any()
    assert not np.isnan(ll)


@settings(suppress_health_check=[hypothesis.HealthCheck.filter_too_much,
                                 hypothesis.HealthCheck.too_slow,
                                 hypothesis.HealthCheck.data_too_large])
@given(X=arrays(np.float64, (10, 1), elements=floats(-1e6, 1e6)),
       y=arrays(np.int32, (10,), elements=integers(0, 1)),
       coeff=arrays(np.float64, (1,), elements=floats(-1e6, 1e6)))
def test_update_local_model_params_not_nan_1_column(X, y, coeff):
    grad, hess, ll = update_local_model_parameters(X, y, coeff)
    count = Counter(y)
    assume(count[0] > len(coeff) and count[1] > len(coeff))
    assert not np.isnan(grad).any()
    assert not np.isnan(hess).any()
    assert not np.isnan(ll)


@given(grad=arrays(np.float, (10,), elements=floats(-1e6, 1e6)),
       hess=arrays(np.float64, (10, 10), elements=floats(-1e6, 1e6)))
def test_update_coefficients_not_nan(grad, hess):
    assume(not np.isnan(grad).any() and not np.isnan(hess).any())
    coeff = update_coefficients(grad, hess)
    assert not np.isnan(coeff).any()


@given(grad=arrays(np.float, (1,), elements=floats(-1e6, 1e6)),
       hess=arrays(np.float64, (1, 1), elements=floats(-1e6, 1e6)))
def test_update_coefficients_not_nan_1_column(grad, hess):
    assume(not np.isnan(grad).any() and not np.isnan(hess).any())
    coeff = update_coefficients(grad, hess)
    assert not np.isnan(coeff).any()


@given(y=arrays(np.int32, (100,), elements=integers(0, 1)),
       yhats=arrays(np.int32, (10, 100), elements=integers(0, 1)))
def test_compute_classification_results_not_nan(y, yhats):
    assume(not np.isnan(y).any() and not np.isnan(yhats).any())
    fn, fp, tn, tp = compute_classification_results(y, yhats)
    assert not np.isnan(fn).any()
    assert not np.isnan(fp).any()
    assert not np.isnan(tn).any()
    assert not np.isnan(tp).any()


@given(y=arrays(np.int32, (100,), elements=integers(0, 1)),
       yhats=arrays(np.int32, (10, 100), elements=integers(0, 1)))
def test_compute_classification_results_sum_to_one(y, yhats):
    assume(not np.isnan(y).any() and not np.isnan(yhats).any())
    for res in zip(*compute_classification_results(y, yhats)):
        assert sum(res) == len(y)
