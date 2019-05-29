import requests
import json
import logging
import math
from decimal import *


url='http://88.197.53.100:9090/mining/query/ANOVA'


def test_Histogram_1():
    logging.info("---------- TEST 1: Histogram of right ententorhinal area ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": ""},
            {"name": "bins", "value": "50"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1', 313447.744, 2, 156723.872, 77571.8524, '< .001', 0.955, 0.999)


def test_Histogram_2():
    logging.info("---------- TEST 2: Histogram of right ententorhinal area by gender ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": "gender"},
            {"name": "bins", "value": "50"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1', 313447.744, 2, 156723.872, 77571.8524, '< .001', 0.955, 0.999)


def test_Histogram_3():
    logging.info("---------- TEST 3: Histogram of right ententorhinal area by alzheimer broad category ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": "alzheimerbroadcategory"},
            {"name": "bins", "value": "30"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1', 313447.744, 2, 156723.872, 77571.8524, '< .001', 0.955, 0.999)


def test_Histogram_4():
    logging.info("---------- TEST 4: Bar graph of alzheimer broad category ")

    data = [{ "name": "x", "value": "alzheimerbroadcategory"},
            {"name": "y", "value": ""},
            {"name": "bins", "value": ""},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1', 313447.744, 2, 156723.872, 77571.8524, '< .001', 0.955, 0.999)


def test_Histogram_5():
    logging.info("---------- TEST 5: Bar graph of alzheimer broad category by gender ")

    data = [{ "name": "x", "value": "alzheimerbroadcategory"},
            {"name": "y", "value": "gender"},
            {"name": "bins", "value": ""},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

# sprintf("%0.7s - %0.7s",result_H0$xmax,result_H0$xmin)

corr_bins = []
corr_count =[]

def check_variables(bins,counts,corr_bins,corr_counts):

    assert len(bins)==len(corr_bins)
    for i in xrange(len(bins)):
        assert str(bins[i])==str(corr_bins[i])

    assert len(counts)==len(corr_counts)
    for i in xrange(len(counts)):
        assert int(counts[i])==int(corr_counts[i])
