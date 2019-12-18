import requests
import unittest
import os
import json
import logging

endpointUrl='http://88.197.53.100:9090/mining/query/TTEST_ONESAMPLE'

def test_TTEST_ONESAMPLE_Privacy():
        logging.info("---------- TEST : Algorithms for User Error")
        data = [{"name": "y", "value": "lefthippocampus,righthippocampus"},
                    {"name": "testvalue", "value": "3.0"    },
                    {"name": "hypothesis", "value": "different"},
                    {"name": "effectsize", "value": "1" },
                    {"name": "ci","value": "5"  },
                    {"name": "meandiff", "value": "1"  },
                    {"name": "pathology","value":"dementia"},
                    {"name": "dataset", "value": "adni"},
                    {"name": "filter","value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
        result = json.loads(r.text)
        check_privacy_result(r.text)



def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The value(s) of the parameter 'ci' should be less than 1.0 .\",\"type\":\"text/plain+error\"}]}"

if __name__ == '__main__':
    unittest.main()
