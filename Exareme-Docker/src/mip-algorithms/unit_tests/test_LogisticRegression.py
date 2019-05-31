import requests
import json
import logging
import math

# Required datasets: data_logisticRegression.csv

endpointUrl = 'http://88.197.53.100:9090'


def test_LogisticRegression():
    """
    R results:
        glm.fit <- glm(alzheimerbroadcategory ~ leftententorhinalarea + rightententorhinalarea
                                                + lefthippocampus + righthippocampus, family = binomial)

        Coefficients:
                               Estimate Std. Error z value Pr(>|z|)
        (Intercept)              -8.850      1.025  -8.634 < 0.01
        leftententorhinalarea     1.355      1.432   0.946 0.343976
        rightententorhinalarea    4.973      1.403   3.545 < 0.01
        lefthippocampus           2.711      1.115   2.432 0.015013
        righthippocampus         -2.729      1.031  -2.646 < 0.01
    """

    logging.info("---------- TEST : Logistic Regression")

    data = [
        {
            "name" : "X",
            "value": "leftententorhinalarea, rightententorhinalarea, lefthippocampus, righthippocampus"
        },
        {
            "name" : "Y",
            "value": "alzheimerbroadcategory"
        },
        {
            "name" : "dataset",
            "value": "data_logisticRegression"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/PEARSON_CORRELATION', data=json.dumps(data), headers=headers)
    result = json.loads(r.text)

    r_coeffs = [{
        'name'       : '(Intercept)',
        'coefficient': -8.850,
        'std.err.'   : 1.025,
        'z value'    : -8.634,
        'p value'    : '< 0.01'
    }, {
        'name'       : 'leftententorhinalarea',
        'coefficient': 1.355,
        'std.err.'   : 1.432,
        'z value'    : 0.946,
        'p value'    : 0.343976
    }, {
        'name'       : 'rightententorhinalarea',
        'coefficient': 4.973,
        'std.err.'   : 1.403,
        'z value'    : 3.545,
        'p value'    : '< 0.01'
    }, {
        'name'       : 'lefthippocampus',
        'coefficient': 2.711,
        'std.err.'   : 1.115,
        'z value'    : 2.432,
        'p value'    : '0.015013'
    }, {
        'name'       : 'righthippocampus',
        'coefficient': -2.729,
        'std.err.'   : 1.031,
        'z value'    : -2.646,
        'p value'    : '< 0.01'
    }, ]

    check_result(exareme_coeffs=result, r_coeffs=r_coeffs)


def check_result(exareme_coeffs, r_coeffs):
    for exa_coeff, r_coeff in zip(exareme_coeffs, r_coeffs):
        assert exa_coeff['Name'] == r_coeff['name']
        assert math.isclose(exa_coeff['Coefficient'], r_coeff['coefficient'], rel_tol=1e-03)
        assert math.isclose(exa_coeff['std.err.'], r_coeff['std.err.'], rel_tol=1e-03)
        assert math.isclose(exa_coeff['z score'], r_coeff['z value'], rel_tol=1e-03)
        if type(exa_coeff['p value']) == str:
            assert exa_coeff['p value'] == r_coeff['p value']
        else:
            assert math.isclose(exa_coeff['p value'], r_coeff['p value'], rel_tol=1e-03)
