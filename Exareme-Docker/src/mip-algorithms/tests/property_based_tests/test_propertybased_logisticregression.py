from collections import Counter
from collections import Mapping
import numpy as np
import hypothesis
from hypothesis import given, settings, assume, note
from hypothesis.strategies import integers, floats
from hypothesis.extra.numpy import arrays

from LOGISTIC_REGRESSION.logistic_regression import init_model
from LOGISTIC_REGRESSION.logistic_regression import update_local_model_parameters
from LOGISTIC_REGRESSION.logistic_regression import update_coefficients
from LOGISTIC_REGRESSION.logistic_regression import compute_classification_results
from LOGISTIC_REGRESSION.logistic_regression import compute_summary
from LOGISTIC_REGRESSION.logistic_regression import compute_confusion_matrix
from LOGISTIC_REGRESSION.logistic_regression import compute_roc


@settings(max_examples=500)
@given(n_cols=integers(1, int(1e6)), n_obs=integers(1, int(1e6)))
def test_init_model(n_cols, n_obs):
    coeff, _, ll = init_model(n_cols, n_obs)
    assert (coeff == 0.0).all()
    assert not np.isnan(ll)

@settings(suppress_health_check=[hypothesis.HealthCheck.filter_too_much,
                                 hypothesis.HealthCheck.too_slow,
                                 hypothesis.HealthCheck.data_too_large],
          max_examples=500)
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
                                 hypothesis.HealthCheck.data_too_large],
          max_examples=500)
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


@settings(max_examples=500)
@given(grad=arrays(np.float, (10,), elements=floats(-1e6, 1e6)),
       hess=arrays(np.float64, (10, 10), elements=floats(-1e6, 1e6)))
def test_update_coefficients_not_nan(grad, hess):
    assume(not np.isnan(grad).any() and not np.isnan(hess).any())
    coeff = update_coefficients(grad, hess)
    assert not np.isnan(coeff).any()


@settings(max_examples=500)
@given(grad=arrays(np.float, (1,), elements=floats(-1e6, 1e6)),
       hess=arrays(np.float64, (1, 1), elements=floats(-1e6, 1e6)))
def test_update_coefficients_not_nan_1_column(grad, hess):
    coeff = update_coefficients(grad, hess)
    assert not np.isnan(coeff).any()


@settings(max_examples=500)
@given(y=arrays(np.int32, (100,), elements=integers(0, 1)),
       yhats=arrays(np.int32, (10, 100), elements=integers(0, 1)))
def test_compute_classification_results_not_nan(y, yhats):
    fn, fp, tn, tp = compute_classification_results(y, yhats)
    assert not np.isnan(fn).any()
    assert not np.isnan(fp).any()
    assert not np.isnan(tn).any()
    assert not np.isnan(tp).any()


@settings(max_examples=500)
@given(y=arrays(np.int32, (100,), elements=integers(0, 1)),
       yhats=arrays(np.int32, (10, 100), elements=integers(0, 1)))
def test_compute_classification_results_sum_to_one(y, yhats):
    assume(not np.isnan(y).any() and not np.isnan(yhats).any())
    for res in zip(*compute_classification_results(y, yhats)):
        assert sum(res) == len(y)


@settings(suppress_health_check=[hypothesis.HealthCheck.filter_too_much,
                                 hypothesis.HealthCheck.too_slow,
                                 hypothesis.HealthCheck.data_too_large],
          max_examples=500)
@given(coeff=arrays(np.float64, (3,), elements=floats(-1e6, 1e6)),
       ll=floats(-1e6, 1e6),
       hess=arrays(np.float64, (3, 3), elements=floats(-1e6, 1e6)),
       y=arrays(np.int32, (10,), elements=integers(0, 1)))
def test_compute_summary_not_nan(coeff, ll, hess, y):
    n_obs = len(y)
    n_cols = len(coeff)
    y_sum = sum(y)
    assume(not np.isclose(hess, 0.0).all())
    assume(not np.isclose(np.linalg.det(hess), 0.0))
    try:
        inv_hess = np.linalg.inv(hess)
        if np.isnan(inv_hess).any():
            raise np.linalg.LinAlgError
    except np.linalg.LinAlgError:
        inv_hess = np.linalg.pinv(hess)
    assume((np.diag(inv_hess) > 0).all())
    summary = compute_summary(n_obs, n_cols, coeff, ll, hess, y_sum)
    for name, val in summary._asdict().items():
        note(name)
        assert not np.isnan(val).any()


@given(tp=integers(0, int(1e6)), tn=integers(0, int(1e6)),
       fp=integers(0, int(1e6)), fn=integers(0, int(1e6)))
def test_compute_confusion_matrix_not_nan(tp, tn, fp, fn):
    assume(tp + tn + fp + fn > 0)
    summary = compute_confusion_matrix(tp, tn, fp, fn)
    for name, value in summary._asdict().items():
        note(name)
        if isinstance(value, Mapping):
            for key, val in value.items():
                assert not np.isnan(val)
        else:
            assert not np.isnan(value).any()


@settings(max_examples=500)
@given(true_positives=arrays(np.int32, (100,), elements=integers(0, 100)),
       true_negatives=arrays(np.int32, (100,), elements=integers(0, 100)),
       false_positives=arrays(np.int32, (100,), elements=integers(0, 100)),
       false_negatives=arrays(np.int32, (100,), elements=integers(0, 100)),
       )
def test_compute_roc(true_positives, true_negatives, false_positives, false_negatives):
    roc_curve, auc, gini = compute_roc(true_positives, true_negatives,
                                       false_positives, false_negatives)
    for fp, tp in roc_curve:
        assert not np.isnan(fp)
        assert not np.isnan(tp)
    assert not np.isnan(auc)
    assert not np.isnan(gini)
