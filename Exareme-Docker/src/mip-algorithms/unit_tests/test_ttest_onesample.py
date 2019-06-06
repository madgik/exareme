import requests
import json
import logging
import math
from decimal import *

endpointUrl='http://localhost:9090/mining/query/TTEST_ONESAMPLE'

def test_onesamplettest_1():
    logging.info("---------- TEST 1: we compare the mean of the left and right hippocampus volumes separetely, with a reference value 3. ")

    data = [{"name": "x", "value": "lefthippocampus,righthippocampus"},
            {"name": "testvalue", "value": "3.0"    },
            {"name": "hypothesis", "value": "lessthan"},
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
     # ONE SAMPLE T-TEST
     #
     # One Sample T-Test
     # ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────
     #                                      statistic    df     p        Mean difference    Lower    Upper     Cohen's d
     # ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────
     #   lefthippocampus     Student's t       -0.752    919    0.226           -0.00961     -Inf    0.0114      -0.0248
     #   righthippocampus    Student's t       15.160    919    1.000            0.19604     -Inf    0.2173       0.4998
     # ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────
     #   Note. Hₐ population mean < 3
    '''
    corr_result1 = ['lefthippocampus',  -0.752  ,919,  0.226    ,  -0.00961 ,    '-Inf'   , 0.0114  ,    -0.0248 ]
    corr_result2 = ['righthippocampus',  15.160 , 919,   1.000   ,   0.19604  ,   '-Inf' ,   0.2173  ,     0.4998   ]
    check_result(result['resources'][0]['data'][1],corr_result1)
    check_result(result['resources'][0]['data'][2],corr_result2)


def test_onesamplettest_2():
    logging.info("---------- TEST 1: we compare the mean of the left and right hippocampus volumes separetely, with a reference value 3. ")

    data = [{"name": "x", "value": "lefthippocampus,righthippocampus"},
            {"name": "testvalue", "value": "3.0"    },
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
 # ONE SAMPLE T-TEST
 #
 # One Sample T-Test
 # ──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #                                      statistic    df     p         Mean difference    Lower      Upper     Cohen's d
 # ──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   lefthippocampus     Student's t       -0.752    919     0.452           -0.00961    -0.0347    0.0155      -0.0248
 #   righthippocampus    Student's t       15.160    919    < .001            0.19604     0.1707    0.2214       0.4998
 # ──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   Note. Hₐ population mean ≠ 3

    '''
    corr_result1 = ['lefthippocampus',  -0.752  , 919,   0.452  ,  -0.00961 , -0.0347 , 0.0155  ,    -0.0248 ]
    corr_result2 = ['righthippocampus', 15.160 , 919,  '< .001',   0.19604  ,   0.1707 ,   0.2214,       0.4998]
    check_result(result['resources'][0]['data'][1],corr_result1)
    check_result(result['resources'][0]['data'][2],corr_result2)


def test_onesamplettest_3():
    logging.info("---------- TEST 1: we compare the mean of the left and right hippocampus volumes separetely, with a reference value 3. ")

    data = [{"name": "x", "value": "lefthippocampus,righthippocampus"},
            {"name": "testvalue", "value": "3.0"    },
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
 #     ONE SAMPLE T-TEST
 #
 # One Sample T-Test
 # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #                                      statistic    df     p         Mean difference    Lower      Upper    Cohen's d
 # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   lefthippocampus     Student's t       -0.752    919     0.774           -0.00961    -0.0306      Inf      -0.0248
 #   righthippocampus    Student's t       15.160    919    < .001            0.19604     0.1748      Inf       0.4998
 # ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 #   Note. Hₐ population mean > 3
    '''
    corr_result1 = ['lefthippocampus', -0.752  ,  919,   0.774 , -0.00961 ,   -0.0306   ,   'Inf'   ,   -0.0248   ]
    corr_result2 = ['righthippocampus', 15.160 ,919 , '< .001',   0.19604   ,  0.1748   ,   'Inf'    ,   0.4998   ]
    check_result(result['resources'][0]['data'][1],corr_result1)
    check_result(result['resources'][0]['data'][2],corr_result2)




def test_onesamplettest_Privacy():

    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [{"name": "x", "value": "lefthippocampus,righthippocampus"},
                {"name": "testvalue", "value": "3.0"    },
                {"name": "hypothesis", "value": "different"},
                {"name": "effectsize", "value": "1" },
                {"name": "ci","value": "1"  },
                {"name": "meandiff", "value": "1"  },
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
