import requests
import unittest
import os
import json
import logging

endpointUrl='http://88.197.53.38:9090/mining/query/TTEST_ONESAMPLE'

 def test_Histogram_Privacy(self):
        logging.info("---------- TEST : Algorithms for Privacy Error")
        data = [{"name": "x", "value": "lefthippocampus,righthippocampus"},
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
    assert result == assert result == "{\"result\" : [{\"data\":\" Incorrect parameter value. ci'value should be one of the following: 0,1 \",\"type\":\"text/plain+user_error\"}]}"

