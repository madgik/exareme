from collections import Counter
from collections import Mapping
import numpy as np
import hypothesis
from hypothesis import given, settings, assume, note
from hypothesis.strategies import integers, floats, composite
from hypothesis.extra.numpy import arrays

from LOGISTIC_REGRESSION.logistic_regression import init_model
from LOGISTIC_REGRESSION.logistic_regression import update_local_model_parameters
from LOGISTIC_REGRESSION.logistic_regression import update_coefficients
from LOGISTIC_REGRESSION.logistic_regression import compute_classification_results
from LOGISTIC_REGRESSION.logistic_regression import compute_summary
from LOGISTIC_REGRESSION.logistic_regression import compute_confusion_matrix
from LOGISTIC_REGRESSION.logistic_regression import compute_roc


@settings(max_examples=1000)
@given(n_cols=integers(1, int(1e6)), n_obs=integers(1, int(1e6)))
def test_init_model(n_cols, n_obs):
    coeff, _, ll = init_model(n_cols, n_obs)
    assert (coeff == 0.0).all()
    assert not np.isnan(ll)


@composite
def local_data(draw):
    n_cols = draw(integers(1, 50))
    n_obs = draw(integers(n_cols + 1, 1000))
    X = draw(arrays(np.float64, (n_obs, n_cols), elements=floats(-1e6, 1e6)))
    y = draw(arrays(np.int32, (n_obs,), elements=integers(0, 1)))
    coeff = draw(arrays(np.float64, (n_cols,), elements=floats(-1e6, 1e6)))
    return X, y, coeff


@settings(
    max_examples=1000,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
)
@given(local_data())
def test_update_local_model_params_not_nan(local_datum):
    X, y, coeff = local_datum
    grad, hess, ll = update_local_model_parameters(X, y, coeff)
    count = Counter(y)
    assume(count[0] > len(coeff) and count[1] > len(coeff))
    assert not np.isnan(grad).any()
    assert not np.isnan(hess).any()
    assert not np.isnan(ll)


@composite
def grads_and_hessians(draw):
    n_cols = draw(integers(1, 50))
    grad = draw(arrays(np.float, (n_cols,), elements=floats(-1e6, 1e6)))
    hess = draw(arrays(np.float64, (n_cols, n_cols), elements=floats(-1e6, 1e6)))
    return grad, hess


@settings(max_examples=1000)
@given(grads_and_hessians())
def test_update_coefficients_not_nan(grad_and_hess):
    grad, hess = grad_and_hess
    assume(not np.isnan(grad).any() and not np.isnan(hess).any())
    coeff = update_coefficients(grad, hess)
    assert not np.isnan(coeff).any()


@composite
def ys_and_yhats(draw):
    n_cols = draw(integers(1, 50))
    n_obs = draw(integers(n_cols + 1, 1000))
    y = draw(arrays(np.int32, (n_obs,), elements=integers(0, 1)))
    yhats = draw(arrays(np.int32, (n_cols, n_obs), elements=integers(0, 1)))
    return y, yhats


@settings(
    max_examples=1000,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
    deadline=None,
)
@given(ys_and_yhats())
def test_compute_classification_results_not_nan_and_sum_to_one(y_and_yhats):
    y, yhats = y_and_yhats
    fn, fp, tn, tp = compute_classification_results(y, yhats)
    assert not np.isnan(fn).any()
    assert not np.isnan(fp).any()
    assert not np.isnan(tn).any()
    assert not np.isnan(tp).any()
    for res in zip(fn, fp, tn, tp):
        assert sum(res) == len(y)


@composite
def summary_inputs(draw):
    n_cols = draw(integers(1, 50))
    n_obs = draw(integers(n_cols + 1, 1000))
    coeff = draw(arrays(np.float64, (n_cols,), elements=floats(-1e6, 1e6)))
    ll = draw(floats(-1e6, 1e6))
    hess = draw(
        arrays(np.float64, (n_cols, n_cols), elements=floats(-1e6, 1e6)).filter(
            lambda h: not np.isclose(h, 0.0).all()
            and not np.isclose(np.linalg.det(h), 0.0)
        )
    )
    y = draw(arrays(np.int32, (n_obs,), elements=integers(0, 1)))
    return coeff, ll, hess, y


@settings(
    max_examples=1000,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
    deadline=None,
)
@given(summary_inputs())
def test_compute_summary_not_nan(summary_input):
    coeff, ll, hess, y = summary_input
    n_obs = len(y)
    n_cols = len(coeff)
    y_sum = sum(y)
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


@settings(
    max_examples=1000,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
    deadline=None,
)
@given(
    tp=integers(0, int(1e6)),
    tn=integers(0, int(1e6)),
    fp=integers(0, int(1e6)),
    fn=integers(0, int(1e6)),
)
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


@settings(max_examples=1000)
@given(
    true_positives=arrays(np.int32, (100,), elements=integers(0, 100)),
    true_negatives=arrays(np.int32, (100,), elements=integers(0, 100)),
    false_positives=arrays(np.int32, (100,), elements=integers(0, 100)),
    false_negatives=arrays(np.int32, (100,), elements=integers(0, 100)),
)
def test_compute_roc(true_positives, true_negatives, false_positives, false_negatives):
    roc_curve, auc, gini = compute_roc(
        true_positives, true_negatives, false_positives, false_negatives
    )
    for fp, tp in roc_curve:
        assert not np.isnan(fp)
        assert not np.isnan(tp)
    assert not np.isnan(auc)
    assert not np.isnan(gini)
