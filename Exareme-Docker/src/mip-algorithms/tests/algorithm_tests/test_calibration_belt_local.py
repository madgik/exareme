import pytest
from pathlib import Path
import numpy as np

from mipframework.testutils import get_test_params, get_algorithm_result
from CALIBRATION_BELT import CalibrationBelt

expected_file = Path(__file__).parent / "expected" / "calibration_belt_expected.json"


@pytest.mark.parametrize("test_input, expected", get_test_params(expected_file))
def test_pearson_algorithm_local(test_input, expected):
    result = get_algorithm_result(CalibrationBelt, test_input)

    expected = expected[0]
    assert int(result["n_obs"]) == int(expected["n_obs"])
    assert int(result["Model degree"]) == int(expected["Model degree"])
    assert np.isclose(result["p value"], expected["p value"], atol=1e-3)