import requests
import json
import logging
import math

# Required datasets: data_logisticRegression.csv

endpointUrl = 'http://88.197.53.38:9090/mining/query/LOGISTIC_REGRESSION'


def test_LogisticRegression():
    """
    R results:
        glm.fit <- glm(alzheimerbroadcategory ~ leftententorhinalarea + rightententorhinalarea
                                                + lefthippocampus + righthippocampus, family = binomial)

        Coefficients:
                               Estimate Std. Error z value Pr(>|z|)
        (Intercept)              -8.850      1.025  -8.634 < 0.001
        leftententorhinalarea     1.355      1.432   0.946 0.343976
        rightententorhinalarea    4.973      1.403   3.545 < 0.001
        lefthippocampus           2.711      1.115   2.432 0.015013
        righthippocampus         -2.729      1.031  -2.646 < 0.001
    """

    logging.info("---------- TEST : Logistic Regression 1")

    data = [
        {
            "name" : "x",
            "value": "leftententorhinalarea_logreg_test, rightententorhinalarea_logreg_test, lefthippocampus_logreg_test, righthippocampus_logreg_test"
        },
        {
            "name" : "y",
            "value": "alzheimerbroadcategory_logreg_test"
        },
        {
            "name": "pathology",
            "value": "dementia"
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
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    print (result)
    exareme_coeffs = result['result'][0]['data']['Covariates']
    r_coeffs = [{
        'name'       : '(Intercept)',
        'coefficient': -8.850,
        'std.err.'   : 1.025,
        'z value'    : -8.634,
        'p value'    : '< 0.001'
    }, {
        'name'       : 'leftententorhinalarea_logreg_test',
        'coefficient': 1.355,
        'std.err.'   : 1.432,
        'z value'    : 0.946,
        'p value'    : 0.343976
    }, {
        'name'       : 'rightententorhinalarea_logreg_test',
        'coefficient': 4.973,
        'std.err.'   : 1.403,
        'z value'    : 3.545,
        'p value'    : '< 0.001'
    }, {
        'name'       : 'lefthippocampus_logreg_test',
        'coefficient': 2.711,
        'std.err.'   : 1.115,
        'z value'    : 2.432,
        'p value'    : 0.015013
    }, {
        'name'       : 'righthippocampus_logreg_test',
        'coefficient': -2.729,
        'std.err.'   : 1.031,
        'z value'    : -2.646,
        'p value'    : 0.008144
    }]

    check_result(exareme_coeffs=exareme_coeffs, r_coeffs=r_coeffs)

def test_LogisticRegression_MultipleDataset():
    """
    R results:
        glm.fit <- glm(alzheimerbroadcategory ~ leftententorhinalarea + rightententorhinalarea
                                                + lefthippocampus + righthippocampus, family = binomial)

        Coefficients:
                               Estimate Std. Error z value Pr(>|z|)
        (Intercept)              -8.850      1.025  -8.634 < 0.001
        leftententorhinalarea     1.355      1.432   0.946 0.343976
        rightententorhinalarea    4.973      1.403   3.545 < 0.001
        lefthippocampus           2.711      1.115   2.432 0.015013
        righthippocampus         -2.729      1.031  -2.646 < 0.001
    """

    logging.info("---------- TEST : Logistic Regression 2 Multiple Datasets")

    data = [
        {
            "name" : "x",
            "value": "leftententorhinalarea_logreg_test, rightententorhinalarea_logreg_test, lefthippocampus_logreg_test, righthippocampus_logreg_test"
        },
        {
            "name" : "y",
            "value": "alzheimerbroadcategory_logreg_test"
        },
        {
            "name": "pathology",
            "value": "dementia"

        },
        {
            "name" : "dataset",
            "value": "data_logisticRegression, adni"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    exareme_coeffs = result['result'][0]['data']['Covariates']
    r_coeffs = [{
        'name'       : '(Intercept)',
        'coefficient': -8.850,
        'std.err.'   : 1.025,
        'z value'    : -8.634,
        'p value'    : '< 0.001'
    }, {
        'name'       : 'leftententorhinalarea_logreg_test',
        'coefficient': 1.355,
        'std.err.'   : 1.432,
        'z value'    : 0.946,
        'p value'    : 0.343976
    }, {
        'name'       : 'rightententorhinalarea_logreg_test',
        'coefficient': 4.973,
        'std.err.'   : 1.403,
        'z value'    : 3.545,
        'p value'    : '< 0.001'
    }, {
        'name'       : 'lefthippocampus_logreg_test',
        'coefficient': 2.711,
        'std.err.'   : 1.115,
        'z value'    : 2.432,
        'p value'    : 0.015013
    }, {
        'name'       : 'righthippocampus_logreg_test',
        'coefficient': -2.729,
        'std.err.'   : 1.031,
        'z value'    : -2.646,
        'p value'    : 0.008144
    }]

    check_result(exareme_coeffs=exareme_coeffs, r_coeffs=r_coeffs)


def test_LogisticRegression_Privacy():
    logging.info("---------- TEST : Logistic Regression Privacy Error")

    data = [
        {
            "name" : "x",
            "value": "leftententorhinalarea_logreg_test, rightententorhinalarea_logreg_test, lefthippocampus_logreg_test, righthippocampus_logreg_test"
        },
        {
            "name" : "y",
            "value": "alzheimerbroadcategory_logreg_test"
        },
        {
            "name": "pathology",
            "value": "dementia"
        },
        {
            "name" : "dataset",
            "value": "adni_9rows"
        },
        {
            "name" : "filter",
            "value": ""
        },
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)

    check_privacy_result(r.text)


def check_result(exareme_coeffs, r_coeffs):
    for exa_coeff, r_coeff in zip(exareme_coeffs, r_coeffs):
        assert exa_coeff['Variable'] == r_coeff['name']
        assert math.isclose(exa_coeff['Coefficient'], r_coeff['coefficient'], rel_tol=1e-03)
        assert math.isclose(exa_coeff['std.err.'], r_coeff['std.err.'], rel_tol=1e-03)
        assert math.isclose(exa_coeff['z score'], r_coeff['z value'], rel_tol=1e-03)
        if type(exa_coeff['p value']) == str:
            assert exa_coeff['p value'] == r_coeff['p value']
        else:
            assert math.isclose(exa_coeff['p value'], r_coeff['p value'], rel_tol=1e-03)


def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The Experiment could not run with the input provided because there are insufficient data.\",\"type\":\"text/plain+warning\"}]}"
