import json

from mipframework import create_runner
from mipframework.runner.runner import capture_stdout


def get_algorithm_result(algorithm_class, algorithm_args):
    runner = create_runner(
        algorithm_class, num_workers=1, algorithm_args=algorithm_args,
    )
    result = capture_stdout(runner.run)()
    result = json.loads(result)
    return result
