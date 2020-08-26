from collections import Counter
import numpy as np
import hypothesis
from hypothesis import given, settings, assume
from hypothesis.strategies import (
    integers,
    floats,
    composite,
    sampled_from,
    tuples,
    lists,
)
from hypothesis.extra.numpy import arrays

from CALIBRATION_BELT.calibration_belt import init_model
from CALIBRATION_BELT.calibration_belt import update_local_model_parameters
from CALIBRATION_BELT.calibration_belt import update_coefficients
from CALIBRATION_BELT.calibration_belt import compute_log_like_on_bisector
from CALIBRATION_BELT.calibration_belt import giviti_stat_cdf
from CALIBRATION_BELT.calibration_belt import compute_calibration_curve
from CALIBRATION_BELT.calibration_belt import compute_calibration_belt
from CALIBRATION_BELT.calibration_belt import find_relative_to_bisector

PROP_TESTING_MAX_EXAMPLES = 1000


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(max_deg=integers(1, 4), n_obs=integers(1, int(1e6)))
def test_init_model(max_deg, n_obs):
    coeff, ll = init_model(max_deg, n_obs)
    assert (coeff == 0.0).all()
    assert not (np.isnan(ll)).all()


@composite
def local_data(draw):
    n_cols = 5
    n_obs = draw(integers(3, 1000))
    Xs = draw(arrays(np.float64, (4, n_obs, n_cols), elements=floats(-1e6, 1e6)))
    y = draw(arrays(np.int32, (n_obs,), elements=integers(0, 1)))
    coeffs = draw(arrays(np.float64, (4, n_cols), elements=floats(-1e6, 1e6)))
    return Xs, y, coeffs


@settings(
    max_examples=PROP_TESTING_MAX_EXAMPLES,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
)
@given(local_data())
def test_update_local_model_params_not_nan(local_datum):
    Xs, Y, coeffs = local_datum
    grads, hessians, log_likes = update_local_model_parameters(Xs, Y, coeffs, max_deg=4)
    count = Counter(Y)
    assume(count[0] >= len(coeffs[-1]) and count[1] >= len(coeffs[-1]))
    assert not np.isnan(grads).any()
    assert not np.isnan(hessians).any()
    assert not np.isnan(log_likes).any()


@composite
def grads_and_hessians(draw):
    n_cols = 5
    grad = draw(arrays(np.float, (4, n_cols), elements=floats(-1e6, 1e6)))
    hess = draw(arrays(np.float64, (4, n_cols, n_cols), elements=floats(-1e6, 1e6)))
    grad = np.ma.masked_array(grad)
    hess = np.ma.masked_array(hess)
    return grad, hess


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(grads_and_hessians())
def test_update_coefficients_not_nan(grad_and_hess):
    grad, hess = grad_and_hess
    assume(not np.isnan(grad).any() and not np.isnan(hess).any())
    coeff = update_coefficients(grad, hess, max_deg=4)
    assert not np.isnan(coeff).any()


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(local_data())
def test_compute_log_like_on_bisector_not_nan(local_datum):
    Xs, y, _ = local_datum
    log_like_bisector = compute_log_like_on_bisector(Xs, y)
    assert not np.isnan(log_like_bisector)


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES, deadline=None)
@given(
    t=floats(-1e6, 1e6),
    m=integers(1, 4),
    devel=sampled_from(["external", "internal"]),
    thres=floats(0.01, 0.99),
)
def test_giviti_stat_cdf_not_nan(t, m, devel, thres):
    assume(not (m == 1 and devel == "internal"))
    cdf_value = giviti_stat_cdf(t, m, devel, thres)
    assert not np.isnan(cdf_value)


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(
    coeff=arrays(np.float64, integers(2, 5), elements=floats(-1e6, 1e6)),
    num_points=integers(10, 500),
)
def test_compute_calibration_curve(coeff, num_points):
    G, calibration_curve, e = compute_calibration_curve(coeff, num_points, [0.01, 0.99])
    assert not np.isnan(G).any()
    assert not np.isnan(calibration_curve).any()
    assert not np.isnan(e).any()
    assert (0 <= calibration_curve).all() and (calibration_curve <= 1).all()


@composite
def coeffs_and_covariances(draw):
    n_cols = draw(integers(2, 5))
    coeff = draw(arrays(np.float64, n_cols, elements=floats(-1e6, 1e6)))
    covariance = draw(
        arrays(
            np.float64, (n_cols, n_cols), elements=floats(-1e6, 1e6), unique=True
        ).filter(lambda c: not (c == 0).any())
    )
    num_points = draw(integers(10, 500))
    confidence_levels = draw(tuples(floats(0.01, 0.99), floats(0.01, 0.99)))
    return coeff, covariance, num_points, confidence_levels


@settings(
    max_examples=PROP_TESTING_MAX_EXAMPLES,
    suppress_health_check=[
        hypothesis.HealthCheck.filter_too_much,
        hypothesis.HealthCheck.too_slow,
        hypothesis.HealthCheck.data_too_large,
    ],
    deadline=None,
)
@given(coeffs_and_covariances())
def test_compute_calibration_belt(coeff_and_covariance):
    coeff, covariance, num_points, confidence_levels = coeff_and_covariance
    G, calibration_curve, e = compute_calibration_curve(coeff, num_points, [0.01, 0.99])
    calibration_belts, _, _ = compute_calibration_belt(
        G, coeff, covariance, confidence_levels
    )
    assume((np.einsum("li, ij, lj -> l", G, covariance, G) >= 0).all())
    assert not np.isnan(calibration_belts).any()
    assert (0 <= calibration_belts).all() and (calibration_belts <= 1).all()


@settings(max_examples=PROP_TESTING_MAX_EXAMPLES)
@given(
    x=lists(floats(0, 1), min_size=100, max_size=100),
    y=lists(floats(0, 1), min_size=100, max_size=100),
    region=sampled_from(["over", "under"]),
)
def test_find_relative_to_bisector(x, y, region):
    segments = find_relative_to_bisector(x, y, region)
    assert isinstance(segments, basestring)
