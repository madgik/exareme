import pytest
import json
from pathlib import Path
import numpy as np

from CALIBRATION_BELT import CalibrationBelt

from mipframework import create_runner
from mipframework.runner.runner import capture_stdout


def get_test_params():
    path = Path(__file__).parent / "expected" / "calibration_belt_expected.json"
    with path.open() as json_expected:
        params = json.load(json_expected)["test_cases"]
    params = [(p["input"], p["output"]) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_pearson_algorithm_local(test_input, expected):
    if expected[0] is None:
        assert True
    else:
        alg_args = sum([["-" + p["name"], p["value"]] for p in test_input], [])
        runner = create_runner(
            CalibrationBelt,
            alg_type="iterative",
            num_workers=1,
            algorithm_args=alg_args,
        )
        result = capture_stdout(runner.run)()
        result = json.loads(result)["result"][0]["data"]
        expected = expected[0]
        assert int(result["n_obs"]) == int(expected["n_obs"])
        assert int(result["Model degree"]) == int(expected["Model degree"])
        assert np.isclose(result["p value"], expected["p value"], atol=1e-3)
