import pytest
import json
import requests
import math
from tests.vm_url import vmUrl

endpointUrl = vmUrl + "DESCRIPTIVE_STATS"


def get_test_params():
    with open("expected/descr_stats_runs.json") as json_file:
        params = json.load(json_file)["results"]
    params = [(p["input"], p["output"]) for p in params]
    return params


@pytest.mark.parametrize("test_input, expected", get_test_params())
def test_eval(test_input, expected):
    headers = {"Content-type": "application/json", "Accept": "text/plain"}
    res = requests.post(endpointUrl, data=json.dumps(test_input), headers=headers)
    res = json.loads(res.text)
    res = res["result"][0]["data"]["data"]
    for i, e in enumerate(expected):
        for key, val in e.items():
            print(key, val)
            test_val = res[i][key]
            if type(val) == float:
                assert math.isclose(val, test_val, rel_tol=0, abs_tol=1e-03)
            else:
                assert val == test_val
