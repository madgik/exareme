import requests
import json
import logging
import math
from decimal import *


endpointUrl='http://localhost:9090/mining/query/TTEST_ONESAMPLE'


def test_ttest_onesample():
    logging.info("---------- TEST 1: we compare the mean of the left and right hippocampus volumes separetely, with a reference value 3. ")

    data = [{"name": "x", "value": "lefthippocampus,righthippocampus"},
            {"name": "testvalue", "value": "3.0"    },
            {"name": "hypothesis", "value": "different"},
            {"name": "effectsize", "value": "1" },
            {"name": "ci","value": "0"  },
            {"name": "meandiff", "value": "0"  },
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter","value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
    ##
    ##  INDEPENDENT SAMPLES T-TEST
    ##
    ##  Independent Samples T-Test
    ##  ─────────────────────────────────────────────────────────────────────────────
    ##                                      statistic    df     p         Cohen's d
    ##  ─────────────────────────────────────────────────────────────────────────────
    ##    lefthippocampus    Student's t         12.2    918    < .001        0.802
    ##  ─────────────────────────────────────────────────────────────────────────────
    '''

    assert False
