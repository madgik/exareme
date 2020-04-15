import pytest
import json
from pathlib import Path
import numpy as np

from PEARSON_CORRELATION import Pearson
from mipframework import create_runner
from mipframework.runner.runner import capture_stdout


def get_test_params():
    path = Path(__file__).parent / "expected" / "pearson_expected.json"
    with path.open() as json_expected:
        params = json.load(json_expected)["test_cases"]
    params = [(p["input"], p["output"]) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_pearson_algorithm_local(test_input, expected):
    alg_args = sum([["-" + p["name"], p["value"]] for p in test_input], [])
    runner = create_runner(
        Pearson, alg_type="local-global", num_workers=1, algorithm_args=alg_args
    )
    result = capture_stdout(runner.run)()
    result = json.loads(result)["result"][0]["data"]
    expected = expected[0]
    assert np.isclose(
        result["Pearson correlation coefficient"],
        expected["Pearson correlation coefficient"],
        atol=1e-3,
    ).all()
    assert np.isclose(result["p-value"], expected["p-value"], atol=1e-3).all()
    assert int(result["n_obs"]) == int(expected["n_obs"])
