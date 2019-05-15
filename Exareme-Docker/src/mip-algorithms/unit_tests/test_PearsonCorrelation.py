import requests
import json
import logging
import math

# Required datasets: adni_9rows, adni, data_pr1, desd-synthdata

endpointUrl = 'http://88.197.53.52:9090'


def test_PearsonCorrlation_MIP_AlgoTesting_1():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    lefthippocampus vs righthippocampus
        Pearson's r     0.902
        p-value         < .001
        95% CI Upper    0.913
        95% CI Lower    0.889
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_1")

    data = [
        {
            "name" : "X",
            "value": "lefthippocampus"
        },
        {
            "name" : "Y",
            "value": "righthippocampus"
        },
        {
            "name" : "dataset",
            "value": "desd-synthdata"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'lefthippocampus_righthippocampus', 0.902, '< 0.001', 0.889, 0.913
    )


def test_PearsonCorrlation_MIP_AlgoTesting_2():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    lefthippocampus vs opticchiasm
        Pearson's r     0.211
        p-value         < .001
        95% CI Upper    0.272
        95% CI Lower    0.148
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_2")

    data = [
        {
            "name" : "X",
            "value": "lefthippocampus"
        },
        {
            "name" : "Y",
            "value": "opticchiasm"
        },
        {
            "name" : "dataset",
            "value": "desd-synthdata"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'lefthippocampus_opticchiasm', 0.211, '< 0.001', 0.148, 0.272
    )


def test_PearsonCorrlation_MIP_AlgoTesting_2p1():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    subjectageyears vs minimentalstate
        Pearson's r     -0.149
        p-value         < .001
        95% CI Upper    -0.079
        95% CI Lower    -0.218
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_2p1")

    data = [
        {
            "name" : "X",
            "value": "subjectageyears"
        },
        {
            "name" : "Y",
            "value": "minimentalstate"
        },
        {
            "name" : "dataset",
            "value": "desd-synthdata"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'subjectageyears_minimentalstate', -0.149, '< 0.001', -0.218, -0.079
    )


def test_PearsonCorrlation_MIP_AlgoTesting_3():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    subjectageyears vs opticchiasm
        Pearson's r     -0.006
        p-value          0.867
        95% CI Upper     0.067
        95% CI Lower    -0.079
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_3p1")

    data = [
        {
            "name" : "X",
            "value": "subjectageyears"
        },
        {
            "name" : "Y",
            "value": "opticchiasm"
        },
        {
            "name" : "dataset",
            "value": "desd-synthdata"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'subjectageyears_opticchiasm', -0.006, 0.867, -0.079, 0.067
    )


def test_PearsonCorrlation_MIP_AlgoTesting_3p1():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    var1 vs var2
        Pearson's r     -0.006
        p-value          0.867
        95% CI Upper     0.067
        95% CI Lower    -0.079
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_3p1")

    data = [
        {
            "name" : "X",
            "value": "var1"
        },
        {
            "name" : "Y",
            "value": "var2"
        },
        {
            "name" : "dataset",
            "value": "data_pr1"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'var1_var2', -0.006, 0.867, -0.079, 0.067
    )


def test_PearsonCorrlation_MIP_AlgoTesting_3p2():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    var3 vs var4
        Pearson's r     0.008
        p-value         0.838
        95% CI Upper    0.081
        95% CI Lower    -0.066
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_3p2")

    data = [
        {
            "name" : "X",
            "value": "var3"
        },
        {
            "name" : "Y",
            "value": "var4"
        },
        {
            "name" : "dataset",
            "value": "data_pr1"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'var3_var4', 0.008, 0.838, -0.066, 0.081
    )


def test_PearsonCorrlation_MIP_AlgoTesting_4():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    righthippocampus vs lefthippocampus
        Pearson's r     0.902
        p-value       < 0.001
        95% CI Upper    0.913
        95% CI Lower    0.889
    righthippocampus vs leftententorhinalarea
        Pearson's r     0.808
        p-value       < 0.001
        95% CI Upper    0.829
        95% CI Lower    0.784
    lefthippocampus vs leftententorhinalarea
        Pearson's r     0.806
        p-value       < 0.001
        95% CI Upper    0.828
        95% CI Lower    0.782
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_4")

    data = [
        {
            "name" : "X",
            "value": "righthippocampus, lefthippocampus, leftententorhinalarea"
        },
        {
            "name" : "Y",
            "value": ""
        },
        {
            "name" : "dataset",
            "value": "desd-synthdata"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'righthippocampus_lefthippocampus', 0.902, '< 0.001', 0.889, 0.913
    )
    check_result(
            result['result'][1], 'righthippocampus_leftententorhinalarea', 0.808, '< 0.001', 0.784, 0.829
    )
    check_result(
            result['result'][2], 'lefthippocampus_leftententorhinalarea', 0.806, '< 0.001', 0.782, 0.828
    )


def test_PearsonCorrlation_MIP_AlgoTesting_5():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    righthippocampus vs lefthippocampus
        Pearson's r     0.902
        p-value       < 0.001
        95% CI Upper    0.913
        95% CI Lower    0.889
    righthippocampus vs opticchiasm
        Pearson's r     0.198
        p-value       < 0.001
        95% CI Upper    0.259
        95% CI Lower    0.135
    lefthippocampus vs opticchiasm
        Pearson's r     0.211
        p-value       < 0.001
        95% CI Upper    0.272
        95% CI Lower    0.148
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_5")

    data = [
        {
            "name" : "X",
            "value": "righthippocampus, lefthippocampus, opticchiasm"
        },
        {
            "name" : "Y",
            "value": ""
        },
        {
            "name" : "dataset",
            "value": "desd-synthdata"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'righthippocampus_lefthippocampus', 0.902, '< 0.001', 0.889, 0.913
    )
    check_result(
            result['result'][1], 'righthippocampus_opticchiasm', 0.198, '< 0.001', 0.135, 0.259
    )
    check_result(
            result['result'][2], 'lefthippocampus_opticchiasm', 0.211, '< 0.001', 0.148, 0.272
    )


def test_PearsonCorrlation_MIP_AlgoTesting_6():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    lefthippocampus vs subjectageyears
        Pearson's r     -0.208
        p-value        < 0.001
        95% CI Upper    -0.137
        95% CI Lower    -0.277
    lefthippocampus vs opticchiasm
        Pearson's r     0.202
        p-value       < 0.001
        95% CI Upper    0.271
        95% CI Lower    0.130
    subjectageyears vs opticchiasm
        Pearson's r     -0.006
        p-value         0.867
        95% CI Upper    0.067
        95% CI Lower    -0.079
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_6")

    data = [
        {
            "name" : "X",
            "value": "lefthippocampus, subjectageyears, opticchiasm"
        },
        {
            "name" : "Y",
            "value": ""
        },
        {
            "name" : "dataset",
            "value": "desd-synthdata"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'lefthippocampus_subjectageyears', -0.208, '< 0.001', -0.277, -0.137
    )
    check_result(
            result['result'][1], 'lefthippocampus_opticchiasm', 0.202, '< 0.001', 0.130, 0.271
    )
    check_result(
            result['result'][2], 'subjectageyears_opticchiasm', -0.006, 0.867, -0.079, 0.067
    )


def test_PearsonCorrlation_MIP_AlgoTesting_7():
    """
    Results from 2019_MIP_Algo_Testing/PearsonCorrelation

    subjectageyears vs lefthippocampus
        Pearson's r     -0.208
        p-value        < 0.001
        95% CI Upper    -0.137
        95% CI Lower    -0.277
    lefthippocampus vs opticchiasm
        Pearson's r     0.202
        p-value       < 0.001
        95% CI Upper    0.271
        95% CI Lower    0.130
    subjectageyears vs opticchiasm
        Pearson's r     -0.006
        p-value         0.867
        95% CI Upper    0.067
        95% CI Lower    -0.079
    """

    logging.info("---------- TEST : Pearson Correlation MIP_Algo_Testing_6")

    data = [
        {
            "name" : "X",
            "value": "subjectageyears, lefthippocampus, opticchiasm"
        },
        {
            "name" : "Y",
            "value": ""
        },
        {
            "name" : "dataset",
            "value": "desd-synthdata"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_result(
            result['result'][0], 'subjectageyears_lefthippocampus', -0.208, '< 0.001', -0.277, -0.137
    )
    check_result(
            result['result'][1], 'subjectageyears_opticchiasm', -0.006, 0.867, -0.079, 0.067
    )
    check_result(
            result['result'][2], 'lefthippocampus_opticchiasm', 0.202, '< 0.001', 0.130, 0.271
    )


def check_result(my_result, r_var_pair, r_corr, r_pval, r_ci_lo, r_ci_hi):
    var_pair = my_result['Variable pair']
    corr = float(my_result['Pearson correlation coefficient'])
    if '<' in str(my_result['p-value']):
            pval = str(my_result['p-value'])
    else:
        pval = float(my_result['p-value'])
    ci_lo = float(my_result['C.I. Lower'])
    ci_hi = float(my_result['C.I. Upper'])
    assert var_pair == r_var_pair
    assert math.isclose(corr, r_corr, rel_tol=0, abs_tol=1e-03)
    if type(r_pval) == str:
        assert pval == r_pval
    else:
        assert math.isclose(pval, r_pval, rel_tol=0, abs_tol=1e-03)
    assert math.isclose(ci_lo, r_ci_lo, rel_tol=0, abs_tol=1e-03)
    assert math.isclose(ci_hi, r_ci_hi, rel_tol=0, abs_tol=1e-03)