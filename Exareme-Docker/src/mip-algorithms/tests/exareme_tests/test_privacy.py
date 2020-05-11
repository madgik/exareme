import json

import logging
import unittest

import requests
from tests.algorithm_tests_with_privacy.test_ANOVA import endpointUrl as url_anova
from tests.vm_url import vmUrl \
    as url_descr_stat
from tests.algorithm_tests_with_privacy.test_Histograms import endpointUrl as url_hist
from tests.algorithm_tests_with_privacy.test_ID3 import endpointUrl as url_id3
from tests.algorithm_tests_with_privacy.test_KMEANS import endpointUrl as url_kmeans
from tests.algorithm_tests_with_privacy.test_LinearRegression import endpointUrl as url_linreg
from tests.algorithm_tests_without_privacy.test_LogisticRegression import endpointUrl as url_logreg
from tests.algorithm_tests_with_privacy.test_MultipleHistograms import endpointUrl \
    as url_multi_hist
from tests.algorithm_tests_with_privacy.test_NaiveBayes import url1
from tests.algorithm_tests_with_privacy.test_NaiveBayes_Training_Standalone import \
    endpointUrl as url_naive_bayes_standalone
from tests.algorithm_tests_without_privacy.test_PearsonCorrelation import endpointUrl as url_pearson
from tests.algorithm_tests_with_privacy.test_ttest_independent import endpointUrl \
    as url_ttest_indep
from tests.algorithm_tests_with_privacy.test_ttest_onesample import endpointUrl \
    as url_ttest_onesample
from tests.algorithm_tests_with_privacy.test_ttest_paired import endpointUrl \
    as url_ttest_paired

from tests.vm_url import vmUrl
url_calibration= vmUrl+'CALIBRATION_BELT'

url_descr_stat += 'DESCRIPTIVE_STATS'


def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The Experiment could not run with the input provided because there are insufficient data.\",\"type\":\"text/plain+warning\"}]}"


def test_ANOVA_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [
            {"name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3"},
            {"name": "y", "value": "ANOVA_var_D"},
            {"name": "sstype", "value": "1"},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_anova, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_Descriptive_Statistics_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{
            "name" : "x",
            "value": "leftmpogpostcentralgyrusmedialsegment"
    },
            {
                    "name" : "dataset",
                    "value": "adni_9rows"
            },
            {
                    "name" : "filter",
                    "value": ""
            },
            {
                    "name" : "pathology",
                    "value": "dementia"
            }
    ]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_descr_stat, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_Histogram_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{"name": "x", "value": "alzheimerbroadcategory"},
            {"name": "y", "value": "gender"},
            {"name": "bins", "value": ""},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_hist, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_ID3_Privacy():
    logging.info("---------- TEST : ID3 - Test using contact-lenses dataset  ")

    data = [{"name" : "x",
             "value": "CL_age,CL_spectacle_prescrip,CL_astigmatism,CL_tear_prod_rate"},
            {"name": "y", "value": "CL_contact_lenses"},
            {"name": "dataset", "value": "contact-lenses"},
            {"name" : "filter",
             "value": "{\"condition\": \"AND\", \"rules\": [{\"id\": \"CL_age\", \"field\": \"CL_age\", \"type\": \"string\", \"input\": \"text\", \"operator\": \"equal\", \"value\": \"Young\"}], \"valid\": true}"},
            {"name": "pathology", "value": "dementia"}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_id3, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    print (result)

    check_privacy_result(r.text)


def test_KMEANS_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [
            {"name": "iterations_max_number", "value": "50"},
            {"name" : "y",
             "value": "rightpallidum,leftpallidum,lefthippocampus,righthippocampus"},
            {"name": "k", "value": ""},
            {"name": "centers", "value": "[{\"clid\":1,\"rightpallidum\":0.2,\"leftpallidum\":0.5,\"lefthippocampus\":1.7,\"righthippocampus\":1.5},\
             {\"clid\":2,\"rightpallidum\":0.6,\"leftpallidum\":1.2,\"lefthippocampus\":2.5,\"righthippocampus\":2.0},\
             {\"clid\":3,\"rightpallidum\":1.0,\"leftpallidum\":1.5,\"lefthippocampus\":3.9,\"righthippocampus\":2.5},\
             {\"clid\":4,\"rightpallidum\":1.5,\"leftpallidum\":2.0,\"lefthippocampus\":4.0,\"righthippocampus\":3.0},\
            { \"clid\":5,\"rightpallidum\":2.0,\"leftpallidum\":2.2,\"lefthippocampus\":2.3,\"righthippocampus\":4.0}]"},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "e", "value": "0.0001"},
            {"name": "filter", "value": ""}
    ]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_kmeans, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_LinearRegression_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{"name": "x", "value": "alzheimerbroadcategory+gender"},
            {"name": "y", "value": "lefthippocampus"},
            {"name" : "referencevalues",
             "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
            {"name": "encodingparameter", "value": "dummycoding"},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_linreg, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


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
                    "name" : "pathology",
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
    r = requests.post(url_logreg, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)

    check_privacy_result(r.text)


def test_Multiple_Histogram_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{"name": "y", "value": "alzheimerbroadcategory"},
            {"name": "x", "value": "gender"},
            {"name": "bins", "value": "{}"},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_multi_hist, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_NAIVEBAYES_privacy():
    logging.info("---------- TEST : NAIVE BAYES :CATEGORICAL DATASET  ")
    # CROSS VALIDATION

    data1 = [{"name": "pathology", "value": "dementia"},
             {"name": "dataset", "value": "adni_9rows"},
             {"name": "x", "value": "righthippocampus,lefthippocampus"},
             {"name": "y", "value": "alzheimerbroadcategory"},
             {"name": "kfold", "value": "100"},
             {"name": "dbIdentifier", "value": ""},
             {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url1, data=json.dumps(data1), headers=headers)
    result1 = json.loads(r.text)
    check_privacy_result(r.text)


def test_NaiveBayesStandalone_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "x", "value": "lefthippocampus,righthippocampus"},
            {"name": "y", "value": "alzheimerbroadcategory"},
            {"name": "alpha", "value": "0.1"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_naive_bayes_standalone, data=json.dumps(data),
                      headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_PearsonCorrlation_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [{"name": "x", "value": "lefthippocampus"},
            {"name": "y", "value": "righthippocampus"},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""},
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_pearson, data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_privacy_result(r.text)


def test_UnpairedTtest__Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{"name": "y", "value": "lefthippocampus"},
            {"name": "x", "value": "gender"},
            {"name": "xlevels", "value": "M,F"},
            {"name": "hypothesis", "value": "lessthan"},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}
            ]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_ttest_indep, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_onesamplettest_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{"name": "y", "value": "lefthippocampus,righthippocampus"},
            {"name": "testvalue", "value": "3.0"},
            {"name": "hypothesis", "value": "different"},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_ttest_onesample, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_pairedttest_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{"name": "y", "value": "lefthippocampus-righthippocampus"},
            {"name": "hypothesis", "value": "different"},
            {"name": "pathology", "value": "dementia"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_ttest_paired, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


def test_calibration_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{"name": "x", "value": "probGiViTI_2018_Complessiva"},
            {"name": "y", "value": "hospOutcomeLatest_RIC10"},
            {"name": "devel", "value": "external"},
            {"name": "max_deg", "value": "4"},
            {"name": "confLevels", "value": "0.80, 0.95"},
            {"name": "thres", "value": "0.95"},
            {"name": "num_points", "value": "60"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name" : "filter",
             "value": "\n{\n  \"condition\": \"AND\",\n  \"rules\": [\n    {\n      \"id\": \"cb_var1\",\n      \"field\": \"cb_var1\",\n      \"type\": \"integer\",\n      \"input\": \"select\",\n      \"operator\": \"equal\",\n      \"value\": 7\n    }\n  ],\n  \"valid\": true\n}\n"},
            {"name": "pathology", "value": "dementia"}]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url_calibration, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_privacy_result(r.text)


if __name__ == '__main__':
    unittest.main()
