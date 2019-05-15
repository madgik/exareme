import requests
import json
import logging
import math

# Required datasets: adni_10rows

endpointUrl = 'http://88.197.53.100:9090'


def test_PearsonCorrelation_ADNI_9rows():
    logging.info("---------- TEST : Pearson Correlation ADNI on 9 rows")

    data = [
        {
            "name" : "X",
            "value": "leftaccumbensarea, leftacgganteriorcingulategyrus, leftainsanteriorinsula"
        },
        {
            "name" : "Y",
            "value": "rightaccumbensarea, rightacgganteriorcingulategyrus, rightainsanteriorinsula"
        },
        {
            "name" : "dataset",
            "value": "adni_9rows"
        },
        {
            "name" : "filter",
            "value": ""
        }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    """
    Results from R  
    
    > cor.test(X$leftaccumbensarea, Y$rightaccumbensarea, method="pearson")
    cor = 0.832696403083016, p-value = 0.005332947202092 
    
    > cor.test(X$leftacgganteriorcingulategyrus, Y$rightacgganteriorcingulategyrus, method="pearson")
    cor = 0.764766782355394, p-value = 0.016370022484567 
    
    > cor.test(X$leftainsanteriorinsula, Y$rightainsanteriorinsula, method="pearson")
    cor = 0.928237609063798, p-value 0.000874899301446
    """

    check_result(
            result['result'][0], 'leftaccumbensarea_rightaccumbensarea', 0.832696403083016, 0.005332947202092
    )
    check_result(
            result['result'][1], 'leftacgganteriorcingulategyrus_rightacgganteriorcingulategyrus',
            0.764766782355394, 0.016370022484567
    )
    check_result(
            result['result'][2], 'leftainsanteriorinsula_rightainsanteriorinsula', 0.928237609063798, 0.000874899301446
    )


def test_PearsonCorrelation_ADNI_alldata():
    logging.info("---------- TEST : Pearson Correlation ADNI on all data")

    data = [
        {
            "name" : "X",
            "value": "leftaccumbensarea, leftacgganteriorcingulategyrus, leftainsanteriorinsula"
        },
        {
            "name" : "Y",
            "value": "rightaccumbensarea, rightacgganteriorcingulategyrus, rightainsanteriorinsula"
        },
        {
            "name" : "dataset",
            "value": "adni"
        },
        {
            "name" : "filter",
            "value": ""
        }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    """
    Results from R  

    > cor.test(X$leftaccumbensarea, Y$rightaccumbensarea, method="pearson")
    cor = 0.911518956593483, p-value = 0.000000000000000 

    > cor.test(X$leftacgganteriorcingulategyrus, Y$rightacgganteriorcingulategyrus, method="pearson")
    cor = 0.872706907353685, p-value = 0.000000000000000 

    > cor.test(X$leftainsanteriorinsula, Y$rightainsanteriorinsula, method="pearson")
    cor = 0.907680160667781, p-value 0.000000000000000
    """

    check_result(
            result['result'][0], 'leftaccumbensarea_rightaccumbensarea', 0.911518956593483, 0.000000000000000
    )
    check_result(
            result['result'][1], 'leftacgganteriorcingulategyrus_rightacgganteriorcingulategyrus',
            0.872706907353685, 0.000000000000000
    )
    check_result(
            result['result'][2], 'leftainsanteriorinsula_rightainsanteriorinsula', 0.907680160667781, 0.000000000000000
    )

def check_result(my_result, r_var_pair, r_corr, r_pval):
    var_pair = my_result['Variable pair']
    corr = float(my_result['Pearson correlation coefficient'])
    pval = float(my_result['p-value'])
    assert var_pair == r_var_pair
    assert math.isclose(corr, r_corr, rel_tol=0, abs_tol=1e-06)
    assert math.isclose(pval, r_pval, rel_tol=0, abs_tol=1e-06)
