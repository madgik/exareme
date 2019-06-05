import requests
import json
import logging
import math
from decimal import *


endpointUrl='http://localhost:9090/mining/query/TTEST_UNPAIRED'


def test_UnpairedTtest_1a():
    logging.info("---------- TEST 1: We check if the means are different (M,FM). ")

    data = [{"name": "x", "value": "lefthippocampus"},
            {"name": "y", "value": "gender"    },
            {"name": "ylevels",  "value": "M,F"},
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
##  Independent Samples T-Test
##  ─────────────────────────────────────────────────────────────────────────────
##                                      statistic    df     p         Cohen's d
##  ─────────────────────────────────────────────────────────────────────────────
##    lefthippocampus    Student's t         12.2    918    < .001        0.802
##  ─────────────────────────────────────────────────────────────────────────────
    '''

    corr_result = ['lefthippocampus', 918 , 12.2 , '< .001',  0.802]
    check_result(result['resources'][0]['data'][1],corr_result)


def test_UnpairedTtest_1b():
    logging.info("---------- TEST 1: We check if the means are different (F,M). ")

    data = [{"name": "x", "value": "lefthippocampus"},
            {"name": "y", "value": "gender"    },
            {"name": "ylevels",  "value": "F,M"},
            {"name": "hypothesis", "value": "different"},
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
    Independent Samples T-Test
     ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
                                         statistic    df     p         Mean difference    SE difference    Lower     Upper     Cohen's d
     ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
       lefthippocampus    Student's t        -12.2    918    < .001             -0.289           0.0237    -0.335    -0.242       -0.802
     ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
    '''

    corr_result = ['lefthippocampus', 918 , -12.2 ,  '< .001', -0.289 ,  0.0237 , -0.335 , -0.242 , -0.802]
    check_result(result['resources'][0]['data'][1],corr_result)



def test_UnpairedTtest_2():
    logging.info("---------- TEST 2: We check if the mean volumes are greater for men than for women. ")

    data = [{"name": "x", "value": "lefthippocampus"},
            {"name": "y", "value": "gender"    },
            {"name": "ylevels",  "value": "M,F"},
            {"name": "hypothesis", "value": "oneGreater"},
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

    corr_result = ['lefthippocampus', 918 , 12.2 ,  '< .001', 0.289 ,  0.0237 , 0.250 , 'Inf' , -0.802]
    check_result(result['resources'][0]['data'][1],corr_result)



#
# def test_UnpairedTtest_3():
#     logging.info("---------- TEST 3: twoGreater  ")
#
#     data = [{"name": "x", "value": "lefthippocampus"},
#             {"name": "y", "value": "gender"    },
#             {"name": "ylevels",  "value": "F,M"},
#             {"name": "hypothesis", "value": "twoGreater"},
#             {"name": "effectsize", "value": "1" },
#             {"name": "ci","value": "1"  },
#             {"name": "meandiff", "value": "1"  },
#             {"name": "dataset", "value": "desd-synthdata"},
#             {"name": "filter","value": ""}]
#
#     headers = {'Content-type': 'application/json', "Accept": "text/plain"}
#     r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
#     result = json.loads(r.text)
#     print (r.text)
#
#     '''
#      #     INDEPENDENT SAMPLES T-TEST
#      #
#      # Independent Samples T-Test
#      # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
#      #                                      statistic    df     p         Mean difference    SE difference    Lower    Upper     Cohen's d
#      # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
#      #   lefthippocampus     Student's t     -12.16      918    < .001             -0.289           0.0237     -Inf    -0.250       -0.802
#      # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
#      #   Note. Hₐ F < M
#
#     '''
#     corr_result = ['lefthippocampus', 918 , -12.16 ,'< .001',-0.289 , 0.0237, '-Inf',-0.250 , -0.802]
#     check_result(result['resources'][0]['data'][1],corr_result)


def test_UnpairedTtest__Privacy():
    """

    """

    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [{"name": "x", "value": "lefthippocampus"},
            {"name": "y", "value": "gender"    },
            {"name": "ylevels",  "value": "F,M"},
            {"name": "hypothesis", "value": "twoGreater"},
            {"name": "effectsize", "value": "1" },
            {"name": "ci","value": "1"  },
            {"name": "meandiff", "value": "1"  },
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter","value": ""}
           ]


    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_privacy_result(r.text)

def check_privacy_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"





def check_result(exareme_result,corr_result):
    print (exareme_result[0], corr_result[0])
    assert (str(exareme_result[0])==str(corr_result[0]))
    for i in range(1,len(corr_result)-1):
        print (exareme_result[i], corr_result[i])
        if type(corr_result[i]) is str:
            assert (exareme_result[i] <= float(corr_result[i].replace('< ','0')))
        else:
            assert (math.isclose(exareme_result[i],corr_result[i],rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_result[i])).as_tuple().exponent))))
