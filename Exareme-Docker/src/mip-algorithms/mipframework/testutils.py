import json

from mipframework import create_runner
from mipframework.runner.runner import capture_stdout


def get_test_params(expected_file):
    with expected_file.open() as f:
        params = json.load(f)["test_cases"]
    params = [(p["input"], p["output"]) for p in params]
    return params


def get_algorithm_result(algorithm_class, test_input):
    alg_args = sum([["-" + p["name"], p["value"]] for p in test_input], [])
    runner = create_runner(algorithm_class, num_workers=1, algorithm_args=alg_args,)
    result = capture_stdout(runner.run)()
    result = json.loads(result)["result"][0]["data"]
    return result
