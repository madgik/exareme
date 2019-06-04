import requests
import json
import logging
import math
from decimal import *


endpointUrl='http://localhost:9090/mining/query/TTEST_UNPAIRED'


def test_Ttest_Unpaired_1():
    logging.info("---------- TEST 1: We check if the means are different. ")

    data = [{"name": "x", "value": "lefthippocampus"},
            {"name": "y", "value": "gender"    },
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


def test_Ttest_Unpaired_2():
    logging.info("---------- TEST 2: We check if the mean volumes are greater for men than for women. ")

    data = [{"name": "x", "value": "lefthippocampus"},
            {"name": "y", "value": "gender"    },
            {"name": "hypothesis", "value": "greaterthan"},
            {"name": "effectsize", "value": "1" },
            {"name": "ci","value": "1"  },
            {"name": "meandiff", "value": "1"  },
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
    ##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
    ##                                      statistic    df     p         Mean difference    SE difference    Lower    Upper    Cohen's d
    ##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
    ##    lefthippocampus    Student's t         12.2    918    < .001              0.289           0.0237    0.250      Inf        0.802
    ##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
    ##    Note. Hₐ M > F
    '''

    assert False
