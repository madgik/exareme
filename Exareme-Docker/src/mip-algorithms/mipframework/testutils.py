import json

from mipframework import create_runner
from mipframework.runner.runner import capture_stdout


def get_test_params(expected_file, slc=None):
    with expected_file.open() as f:
        params = json.load(f)["test_cases"]
    if not slc:
        slc = slice(len(params))
    params = [(p["input"], p["output"]) for p in params[slc]]
    return params


def get_algorithm_result(algorithm_class, test_input, num_workers=1):
    alg_args = sum([["-" + p["name"], p["value"]] for p in test_input], [])
    runner = create_runner(
        algorithm_class, num_workers=num_workers, algorithm_args=alg_args,
    )
    result = capture_stdout(runner.run)()
    result = json.loads(result)["result"][0]["data"]
    return result
