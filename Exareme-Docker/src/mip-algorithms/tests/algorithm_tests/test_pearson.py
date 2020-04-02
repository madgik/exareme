import pytest
import json
from functools import reduce
from pathlib import Path
import numpy as np

from mipframework import create_runner
from mipframework.runner.runner import capture_stdout


def get_test_params():
    path = Path(__file__).parent / 'expected' / 'pearson_expected.json'
    with path.open() as json_expected:
        params = json.load(json_expected)['test_cases']
    params = [(p['input'], p['output']) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_eval(test_input, expected):
    alg_args = reduce(
        lambda a, b: a + b,
        [
            ['-' + p['name'], p['value']]
            for p in test_input
        ]
    )
    runner = create_runner(for_class='Pearson',
                           found_in='PEARSON_CORRELATION/pearson',
                           alg_type='local-global', num_workers=1,
                           algorithm_args=alg_args)
    result = capture_stdout(runner.run)()
    result = json.loads(result)['result'][0]['data']
    expected = expected[0]
    assert np.isclose(
        result['Pearson correlation coefficient'],
        expected['Pearson correlation coefficient'], atol=1e-3
    ).all()
    assert np.isclose(result['p-value'], expected['p-value'], atol=1e-3).all()
    assert int(result['n_obs']) == int(expected['n_obs'])