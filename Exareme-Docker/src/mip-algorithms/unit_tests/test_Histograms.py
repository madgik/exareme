import requests
import json
import logging
import math
from decimal import *


endpointUrl='http://88.197.53.100:9090/mining/query/HISTOGRAMS'


def test_Histogram_1():
    logging.info("---------- TEST 1: Histogram of right ententorhinal area ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": ""},
            {"name": "bins", "value": "50"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
    > sprintf("%0.7s - %0.7s",result_H0$xmax,result_H0$xmin)
     [1] "0.38579 - 0.34445" "0.42714 - 0.38579" "0.46848 - 0.42714" "0.50983 - 0.46848" "0.55117 - 0.50983" "0.59252 - 0.55117"
     [7] "0.63386 - 0.59252" "0.67521 - 0.63386" "0.71655 - 0.67521" "0.7579 - 0.71655"  "0.79924 - 0.7579"  "0.84059 - 0.79924"
    [13] "0.88193 - 0.84059" "0.92328 - 0.88193" "0.96462 - 0.92328" "1.00597 - 0.96462" "1.04731 - 1.00597" "1.08866 - 1.04731"
    [19] "1.13000 - 1.08866" "1.17135 - 1.13000" "1.21269 - 1.17135" "1.25404 - 1.21269" "1.29538 - 1.25404" "1.33673 - 1.29538"
    [25] "1.37807 - 1.33673" "1.41942 - 1.37807" "1.46076 - 1.41942" "1.50211 - 1.46076" "1.54345 - 1.50211" "1.5848 - 1.54345"
    [31] "1.62614 - 1.5848"  "1.66749 - 1.62614" "1.70883 - 1.66749" "1.75018 - 1.70883" "1.79152 - 1.75018" "1.83287 - 1.79152"
    [37] "1.87421 - 1.83287" "1.91556 - 1.87421" "1.95690 - 1.91556" "1.99825 - 1.95690" "2.03959 - 1.99825" "2.08094 - 2.03959"
    [43] "2.12228 - 2.08094" "2.16363 - 2.12228" "2.20497 - 2.16363" "2.24632 - 2.20497" "2.28766 - 2.24632" "2.32901 - 2.28766"
    [49] "2.37035 - 2.32901" "2.4117 - 2.37035"
    > result_H0$y
     [1]  3  0  0  0  0  0  0  0  0  0  0  0  3  1  4  6  8  0  5  7 21 10 40 21 25 65 67 70 44 74 80 48 65 51 54 36  9 42 17 20
    [41]  5  3  6  5  0  0  0  0  0  5
    '''

    corr_bins = [
    "0.38579 - 0.34445","0.42714 - 0.38579","0.46848 - 0.42714","0.50983 - 0.46848","0.55117 - 0.50983","0.59252 - 0.55117",
    "0.63386 - 0.59252","0.67521 - 0.63386","0.71655 - 0.67521","0.7579 - 0.71655","0.79924 - 0.7579","0.84059 - 0.79924",
    "0.88193 - 0.84059","0.92328 - 0.88193","0.96462 - 0.92328","1.00597 - 0.96462","1.04731 - 1.00597","1.08866 - 1.04731",
    "1.13000 - 1.08866","1.17135 - 1.13000","1.21269 - 1.17135","1.25404 - 1.21269","1.29538 - 1.25404","1.33673 - 1.29538",
    "1.37807 - 1.33673","1.41942 - 1.37807","1.46076 - 1.41942","1.50211 - 1.46076","1.54345 - 1.50211","1.5848 - 1.54345",
    "1.62614 - 1.5848" , "1.66749 - 1.62614","1.70883 - 1.66749","1.75018 - 1.70883","1.79152 - 1.75018","1.83287 - 1.79152",
    "1.87421 - 1.83287","1.91556 - 1.87421","1.95690 - 1.91556","1.99825 - 1.95690","2.03959 - 1.99825","2.08094 - 2.03959",
    "2.12228 - 2.08094","2.16363 - 2.12228","2.20497 - 2.16363","2.24632 - 2.20497","2.28766 - 2.24632","2.32901 - 2.28766",
    "2.37035 - 2.32901","2.4117 - 2.37035"]

    corr_counts =[ {'name': 'All', 'data': [3,0,0,0,0,0,0,0,0,0,0,0,3,1,4,6,8,0,5,7,21,10,40,21,25,65,67,70,44,74,80,48,65,51,54,36, 9,42,17,20,5,3,6,5,0,0,0,0,0,5]}]

    check_rangesofbins(result['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['series'],corr_counts,10)



def test_Histogram_2():
    logging.info("---------- TEST 2: Histogram of right ententorhinal area by gender ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": "gender"},
            {"name": "bins", "value": "50"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
    ##Test 2
    > temp$y_M
     [1]  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  2  2  5 13  6  6 23 29 32 19 40 49 19 33 26 34 19  7 34 12 15
    [41]  4  3  6  3  0  0  0  0  0  5
    > temp$y_W
     [1]  3  0  0  0  0  0  0  0  0  0  0  0  3  1  4  6  8  0  5  5 19  5 27 15 19 42 38 38 25 34 31 29 32 25 20 17  2  8  5  5
    [41]  1  0  0  2  0  0  0  0  0  0

    > sprintf("%0.7s - %0.7s",temp2$xmax,temp2$xmin)
     [1] "0.38579 - 0.34445" "0.42714 - 0.38579" "0.46848 - 0.42714" "0.50983 - 0.46848" "0.55117 - 0.50983" "0.59252 - 0.55117"
     [7] "0.63386 - 0.59252" "0.67521 - 0.63386" "0.71655 - 0.67521" "0.7579 - 0.71655"  "0.79924 - 0.7579"  "0.84059 - 0.79924"
    [13] "0.88193 - 0.84059" "0.92328 - 0.88193" "0.96462 - 0.92328" "1.00597 - 0.96462" "1.04731 - 1.00597" "1.08866 - 1.04731"
    [19] "1.13000 - 1.08866" "1.17135 - 1.13000" "1.21269 - 1.17135" "1.25404 - 1.21269" "1.29538 - 1.25404" "1.33673 - 1.29538"
    [25] "1.37807 - 1.33673" "1.41942 - 1.37807" "1.46076 - 1.41942" "1.50211 - 1.46076" "1.54345 - 1.50211" "1.5848 - 1.54345"
    [31] "1.62614 - 1.5848"  "1.66749 - 1.62614" "1.70883 - 1.66749" "1.75018 - 1.70883" "1.79152 - 1.75018" "1.83287 - 1.79152"
    [37] "1.87421 - 1.83287" "1.91556 - 1.87421" "1.95690 - 1.91556" "1.99825 - 1.95690" "2.03959 - 1.99825" "2.08094 - 2.03959"
    [43] "2.12228 - 2.08094" "2.16363 - 2.12228" "2.20497 - 2.16363" "2.24632 - 2.20497" "2.28766 - 2.24632" "2.32901 - 2.28766"
    [49] "2.37035 - 2.32901" "2.4117 - 2.37035"

    '''

    corr_bins = [ "0.38579 - 0.34445","0.42714 - 0.38579","0.46848 - 0.42714","0.50983 - 0.46848","0.55117 - 0.50983","0.59252 - 0.55117",
                 "0.63386 - 0.59252","0.67521 - 0.63386","0.71655 - 0.67521","0.7579 - 0.71655",  "0.79924 - 0.7579" , "0.84059 - 0.79924",
                 "0.88193 - 0.84059","0.92328 - 0.88193","0.96462 - 0.92328","1.00597 - 0.96462","1.04731 - 1.00597","1.08866 - 1.04731",
                 "1.13000 - 1.08866","1.17135 - 1.13000","1.21269 - 1.17135","1.25404 - 1.21269","1.29538 - 1.25404","1.33673 - 1.29538",
                 "1.37807 - 1.33673","1.41942 - 1.37807","1.46076 - 1.41942","1.50211 - 1.46076","1.54345 - 1.50211","1.5848 - 1.54345" ,
                 "1.62614 - 1.5848",  "1.66749 - 1.62614","1.70883 - 1.66749","1.75018 - 1.70883","1.79152 - 1.75018","1.83287 - 1.79152",
                 "1.87421 - 1.83287","1.91556 - 1.87421","1.95690 - 1.91556","1.99825 - 1.95690","2.03959 - 1.99825","2.08094 - 2.03959",
                 "2.12228 - 2.08094","2.16363 - 2.12228","2.20497 - 2.16363","2.24632 - 2.20497","2.28766 - 2.24632","2.32901 - 2.28766",
                 "2.37035 - 2.32901","2.4117 - 2.37035"]

    corr_counts =[ {'name': 'M', 'data': [ 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,5,13,6,6,23,29,32,19,40,49,19,33,26,34,19,7,34,12,15,4,3,6,3,0,0,0,0,0,5]},
     {'name': 'F', 'data': [3,0,0,0,0,0,0,0,0,0,0,0,3,1,4,6,8,0,5,5,19,5,27,15,19,42,38,38,25,34,31,29,32,25,20,17,2,8,5,5,1,0,0,2,0,0,0,0,0,0]}]

    check_rangesofbins(result['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['series'],corr_counts,10)


def test_Histogram_3():
    logging.info("---------- TEST 3: Histogram of right ententorhinal area by alzheimer broad category ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": "alzheimerbroadcategory"},
            {"name": "bins", "value": "30"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)
    '''
    #AD
    > result2$y
     [1]  0  0  0  0  0  0  0  1  4  9  1  6 20 30 18 47 39 32 20 14 11  6  8  7  0  0  0  0  0  1
    > sprintf("%0.7s - %0.7s",result2$xmax,result2$xmin)
     [1] "0.41335 - 0.34445" "0.48226 - 0.41335" "0.55117 - 0.48226" "0.62008 - 0.55117" "0.68899 - 0.62008" "0.7579 - 0.68899"
     [7] "0.82680 - 0.7579"  "0.89571 - 0.82680" "0.96462 - 0.89571" "1.03353 - 0.96462" "1.10244 - 1.03353" "1.17135 - 1.10244"
    [13] "1.24025 - 1.17135" "1.30916 - 1.24025" "1.37807 - 1.30916" "1.44698 - 1.37807" "1.51589 - 1.44698" "1.5848 - 1.51589"
    [19] "1.65370 - 1.5848"  "1.72261 - 1.65370" "1.79152 - 1.72261" "1.86043 - 1.79152" "1.92934 - 1.86043" "1.99825 - 1.92934"
    [25] "2.06715 - 1.99825" "2.13606 - 2.06715" "2.20497 - 2.13606" "2.27388 - 2.20497" "2.34279 - 2.27388" "2.4117 - 2.34279"

    #CN
    > result2$y
     [1]  0  0  0  0  0  0  0  0  0  0  0  0  0  0  5 14 42 28 54 47 44 18 23 13  1  5  2  0  0  2
    > sprintf("%0.7s - %0.7s",result2$xmax,result2$xmin)
     [1] "0.41335 - 0.34445" "0.48226 - 0.41335" "0.55117 - 0.48226" "0.62008 - 0.55117" "0.68899 - 0.62008" "0.7579 - 0.68899"
     [7] "0.82680 - 0.7579"  "0.89571 - 0.82680" "0.96462 - 0.89571" "1.03353 - 0.96462" "1.10244 - 1.03353" "1.17135 - 1.10244"
    [13] "1.24025 - 1.17135" "1.30916 - 1.24025" "1.37807 - 1.30916" "1.44698 - 1.37807" "1.51589 - 1.44698" "1.5848 - 1.51589"
    [19] "1.65370 - 1.5848"  "1.72261 - 1.65370" "1.79152 - 1.72261" "1.86043 - 1.79152" "1.92934 - 1.86043" "1.99825 - 1.92934"
    [25] "2.06715 - 1.99825" "2.13606 - 2.06715" "2.20497 - 2.13606" "2.27388 - 2.20497" "2.34279 - 2.27388" "2.4117 - 2.34279"

    #Other
    > result2$y
     [1]  1  0  0  0  0  0  0  0  0  3  0  4  5 10  9 28 13 15 12 12 10  5  8  4  2  4  0  0  0  1
    > sprintf("%0.7s - %0.7s",result2$xmax,result2$xmin)
     [1] "0.41335 - 0.34445" "0.48226 - 0.41335" "0.55117 - 0.48226" "0.62008 - 0.55117" "0.68899 - 0.62008" "0.7579 - 0.68899"
     [7] "0.82680 - 0.7579"  "0.89571 - 0.82680" "0.96462 - 0.89571" "1.03353 - 0.96462" "1.10244 - 1.03353" "1.17135 - 1.10244"
    [13] "1.24025 - 1.17135" "1.30916 - 1.24025" "1.37807 - 1.30916" "1.44698 - 1.37807" "1.51589 - 1.44698" "1.5848 - 1.51589"
    [19] "1.65370 - 1.5848"  "1.72261 - 1.65370" "1.79152 - 1.72261" "1.86043 - 1.79152" "1.92934 - 1.86043" "1.99825 - 1.92934"
    [25] "2.06715 - 1.99825" "2.13606 - 2.06715" "2.20497 - 2.13606" "2.27388 - 2.20497" "2.34279 - 2.27388" "2.4117 - 2.34279"
    '''
    corr_bins = ["0.41335 - 0.34445","0.48226 - 0.41335","0.55117 - 0.48226","0.62008 - 0.55117","0.68899 - 0.62008","0.7579 - 0.68899" ,
                 "0.82680 - 0.7579","0.89571 - 0.82680","0.96462 - 0.89571","1.03353 - 0.96462","1.10244 - 1.03353","1.17135 - 1.10244",
                 "1.24025 - 1.17135","1.30916 - 1.24025","1.37807 - 1.30916","1.44698 - 1.37807","1.51589 - 1.44698","1.5848 - 1.51589" ,
                 "1.65370 - 1.5848","1.72261 - 1.65370","1.79152 - 1.72261","1.86043 - 1.79152","1.92934 - 1.86043","1.99825 - 1.92934",
                 "2.06715 - 1.99825","2.13606 - 2.06715","2.20497 - 2.13606","2.27388 - 2.20497","2.34279 - 2.27388","2.4117 - 2.34279" ]

    corr_counts =[ {'name': 'AD', 'data': [ 0,0,0,0,0,0,0,1,4,9,1,6,20,30,18,47,39,32,20,14,11,6,8,7,0,0,0,0,0,1]},
                   {'name': 'CN', 'data': [ 0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,14,42,28,54,47,44,18,23,13,1,5,2,0,0,2]},
                   {'name': 'Other', 'data': [ 1,0,0,0,0,0,0,0,0,3,0,4,5,10,9,28,13,15,12,12,10,5,8,4,2,4,0,0,0,1]}]


    check_rangesofbins(result['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['series'],corr_counts,10)




def test_Histogram_4():
    logging.info("---------- TEST 4: Bar graph of alzheimer broad category ")

    data = [{ "name": "x", "value": "alzheimerbroadcategory"},
            {"name": "y", "value": ""},
            {"name": "bins", "value": ""},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)
    '''
    > layer_H3$count
    [1] 330 298 149
    > layer_H3$group
    [1] 1 2 3
    >
    '''
    corr_bins = ["Other","AD","CN"]
    corr_counts =[{'name': 'All', 'data': [149,330,298]}]

    check_rangesofbinsDiscreteVariable(result['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['series'],corr_counts,10)




def test_Histogram_5():
    logging.info("---------- TEST 5: Bar graph of alzheimer broad category by gender ")

    data = [{ "name": "x", "value": "alzheimerbroadcategory"},
            {"name": "y", "value": "gender"},
            {"name": "bins", "value": ""},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
     y_W_AD   y_M_AD     y_W_CN    y_M_CN    y_W_Other   y_M_Other
1    193        137       154       144          76         73
    '''

    corr_bins = ["Other","AD","CN"]
    corr_counts =[{'name': 'F', 'data': [76,193,154]},
                  {'name': 'M', 'data': [73,137,144]}]

    check_rangesofbinsDiscreteVariable(result['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['series'],corr_counts,10)



def test_Histogram_Privacy():
    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [{"name": "x", "value": "alzheimerbroadcategory"},
            {"name": "y", "value": "gender"},
            {"name": "bins", "value": ""},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_privacy_result(r.text)

def check_privacy_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"


def check_rangesofbinsDiscreteVariable(bins,corr_bins):
    assert len(bins)==len(corr_bins)
    for i in range(len(bins)):
        assert str(bins[i])==str(corr_bins[i])

def check_rangesofbins(bins,corr_bins):
    assert len(bins)==len(corr_bins)
    for i in range(len(bins)):
        A = bins[i].split("-")
        B = corr_bins[i].split(" - ")
        # print (A[0],B[0])
        # print (A[1],B[1])
        assert math.isclose(float(A[0]),float(B[0]),rel_tol=0,abs_tol=10**(-abs(Decimal(str(B[0])).as_tuple().exponent)))
        assert math.isclose(float(A[1]),float(B[1]),rel_tol=0,abs_tol=10**(-abs(Decimal(str(B[1])).as_tuple().exponent)))


def check_valuesofbins(counts,corr_counts,minNumberOfData):

    for i in range(len(counts)):
        for j in range(len(corr_counts)):
            if counts[i]['name']==corr_counts[j]['name']:
                for k in range(len(counts[i]['data'])):
                    if int(corr_counts[j]['data'][k]) >= minNumberOfData:
                        assert int(counts[i]['data'][k]) == int(corr_counts[j]['data'][k])
                        print (counts[i]['data'][k],corr_counts[j]['data'][k])
                    else:
                        assert int(counts[i]['data'][k]) == int(corr_counts[j]['data'][k]) #0
                        print (corr_counts[j]['data'][k])
