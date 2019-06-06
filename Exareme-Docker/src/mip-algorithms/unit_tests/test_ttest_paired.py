import requests
import json
import logging
import math
from decimal import *

endpointUrl='http://localhost:9090/mining/query/TTEST_PAIRED'

def test_pairedttest_1():
    logging.info("---------- TEST 1:  ")

    data = [{"name": "x", "value": "lefthippocampus-righthippocampus"},
            {"name": "hypothesis", "value": "different"},
            {"name": "effectsize", "value": "1" },
            {"name": "ci","value": "0"  },
            {"name": "meandiff", "value": "0"  },
            {"name": "sediff", "value": "0"  },
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter","value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
 #  PAIRED SAMPLES T-TEST
 #
 # Paired Samples T-Test
 # ─────────────────────────────────────────────────────────────────────────────────────────────────
 #                                                         statistic    df     p         Cohen's d
 # ─────────────────────────────────────────────────────────────────────────────────────────────────
 #   lefthippocampus    righthippocampus    Student's t        -36.1    919    < .001        -1.19
 # ─────────────────────────────────────────────────────────────────────────────────────────────────

    '''
    corr_result = ['lefthippocampus-righthippocampus',   -36.1 ,   919  ,  '< .001'      ,  -1.19    ]

    check_result(result['resources'][0]['data'][1],corr_result)



def test_pairedttest_2():
    logging.info("---------- TEST 2: ")

    data = [{"name": "x", "value": "lefthippocampus-righthippocampus"},
            {"name": "hypothesis", "value": "different"},
            {"name": "effectsize", "value": "1" },
            {"name": "ci","value": "1"  },
            {"name": "meandiff", "value": "1"  },
            {"name": "sediff", "value": "1"  },
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter","value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
 # PAIRED SAMPLES T-TEST
 #
 # Paired Samples T-Test
 # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #                                                         statistic    df     p         Mean difference    SE difference    Lower     Upper     Cohen's d
 # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   lefthippocampus    righthippocampus    Student's t        -36.1    919    < .001             -0.206          0.00569    -0.217    -0.194        -1.19
 # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────




    '''
    corr_result = ['lefthippocampus-righthippocampus',   -36.1 ,   919  ,  '< .001'  ,  -0.206  ,  0.00569  ,  -0.217  ,  -0.194  , -1.19   ]
    check_result(result['resources'][0]['data'][1],corr_result)



def test_pairedttest_3():
    logging.info("---------- TEST 3: ")

    data = [{"name": "x", "value": "lefthippocampus-righthippocampus"},
            {"name": "hypothesis", "value": "greaterthan"},
            {"name": "effectsize", "value": "1" },
            {"name": "ci","value": "1"  },
            {"name": "meandiff", "value": "1"  },
            {"name": "sediff", "value": "1"  },
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter","value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
 #  PAIRED SAMPLES T-TEST
 #
 # Paired Samples T-Test
 # ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #                                                         statistic    df     p        Mean difference    SE difference    Lower     Upper    Cohen's d
 # ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   lefthippocampus    righthippocampus    Student's t        -36.1    919    1.000             -0.206          0.00569    -0.215      Inf        -1.19
 # ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   Note. Hₐ Measure 1 > Measure 2

    '''
    corr_result = ['lefthippocampus-righthippocampus',   -36.1 ,   919  ,  1.000   ,  -0.206  ,  0.00569  ,  -0.215  ,  'Inf'  , -1.19   ]
    check_result(result['resources'][0]['data'][1],corr_result)




def test_pairedttest_4():


    logging.info("---------- TEST 4: ")

    data = [{"name": "x", "value": "lefthippocampus-righthippocampus"},
            {"name": "hypothesis", "value": "lessthan"},
            {"name": "effectsize", "value": "1" },
            {"name": "ci","value": "1"  },
            {"name": "meandiff", "value": "1"  },
            {"name": "sediff", "value": "1"  },
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter","value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
 # PAIRED SAMPLES T-TEST
 #
 # Paired Samples T-Test
 # ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #                                                         statistic    df     p         Mean difference    SE difference    Lower    Upper     Cohen's d
 # ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   lefthippocampus    righthippocampus    Student's t        -36.1    919    < .001             -0.206          0.00569     -Inf    -0.196        -1.19
 # ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   Note. Hₐ Measure 1 < Measure 2
 #

    '''
    corr_result = ['lefthippocampus-righthippocampus',   -36.1 ,   919  ,  '< .001'   ,  -0.206  ,  0.00569  ,  '-Inf'  ,  -0.196    , -1.19   ]
    check_result(result['resources'][0]['data'][1],corr_result)



def test_pairedttest_Privacy():

    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [{"name": "x", "value": "lefthippocampus-righthippocampus"},
                {"name": "hypothesis", "value": "different"},
                {"name": "effectsize", "value": "1" },
                {"name": "ci","value": "1"  },
                {"name": "meandiff", "value": "1"  },
                {"name": "sediff", "value": "1"  },
                {"name": "dataset", "value": "adni_9rows"},
                {"name": "filter","value": ""}]

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
        if str(corr_result[i]) == '-Inf' or str(corr_result[i]) == 'Inf':
            assert (str(exareme_result[i]) == str(corr_result[i]))
        elif str(corr_result[i]) == '< .001':
            assert (exareme_result[i] <= float(0.001))
        else:
            assert (math.isclose(exareme_result[i],corr_result[i],rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_result[i])).as_tuple().exponent))))


#
# def check_result(exareme_result,corr_result):
#     print (exareme_result[0], corr_result[0])
#     assert (str(exareme_result[0])==str(corr_result[0]))
#     for i in range(1,len(corr_result)-1):
#         print (exareme_result[i], corr_result[i])
#         if type(corr_result[i]) is str:
#             assert (exareme_result[i] <= float(corr_result[i].replace('< ','0')))
#         else:
#             assert (math.isclose(exareme_result[i],corr_result[i],rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_result[i])).as_tuple().exponent))))
