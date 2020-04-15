import pytest
import json
from pathlib import Path
import numpy as np

from LOGISTIC_REGRESSION import LogisticRegression
from mipframework import create_runner
from mipframework.runner.runner import capture_stdout


def get_test_params():
    path = Path(__file__).parent / "expected" / "logistic_regression_expected.json"
    with path.open() as json_expected:
        params = json.load(json_expected)["test_cases"]
    params = [(p["input"], p["output"]) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_logistic_regression_algorithm_local(test_input, expected):
    alg_args = sum([["-" + p["name"], p["value"]] for p in test_input], [])
    runner = create_runner(
        LogisticRegression, alg_type="iterative", num_workers=1, algorithm_args=alg_args
    )
    result = capture_stdout(runner.run)()
    result = json.loads(result)["result"][0]["data"]
    expected = expected[0]
    assert np.isclose(result["Coefficients"], expected["coeff"], atol=1e-3).all()
