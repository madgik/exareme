import requests
import json
import logging
import math
from decimal import *


endpointUrl='http://localhost:9090/mining/query/HISTOGRAMS'


def test_Histogram_1():
    logging.info("---------- TEST 1: Histogram of right ententorhinal area ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": ""},
            {"name": "bins", "value": "35"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
    > sprintf("%0.7s - %0.7s",result_H0$xmax,result_H0$xmin)
 [1] "0.40351 - 0.34445" "0.46257 - 0.40351" "0.52164 - 0.46257" "0.58070 - 0.52164" "0.63977 - 0.58070" "0.69883 - 0.63977" "0.7579 - 0.69883"  "0.81696 - 0.7579"  "0.87602 - 0.81696" "0.93509 - 0.87602"
[11] "0.99415 - 0.93509" "1.05322 - 0.99415" "1.11228 - 1.05322" "1.17135 - 1.11228" "1.23041 - 1.17135" "1.28947 - 1.23041" "1.34854 - 1.28947" "1.40760 - 1.34854" "1.46667 - 1.40760" "1.52573 - 1.46667"
[21] "1.5848 - 1.52573"  "1.64386 - 1.5848"  "1.70292 - 1.64386" "1.76199 - 1.70292" "1.82105 - 1.76199" "1.88012 - 1.82105" "1.93918 - 1.88012" "1.99825 - 1.93918" "2.05731 - 1.99825" "2.11637 - 2.05731"
[31] "2.17544 - 2.11637" "2.23450 - 2.17544" "2.29357 - 2.23450" "2.35263 - 2.29357" "2.4117 - 2.35263"
    > result_H0$y
     [1]   3   0   0   0   0   0   0   0   3   1  10   8   3   9  24  43  29  61 107  82  91 104  81  61  79  31  42  24   6   8   5   0   0   0   5
    '''

    corr_bins = [ "0.40351 - 0.34445" ,"0.46257 - 0.40351", "0.52164 - 0.46257", "0.58070 - 0.52164", "0.63977 - 0.58070" ,"0.69883 - 0.63977", "0.7579 - 0.69883",  "0.81696 - 0.7579",  "0.87602 - 0.81696" ,"0.93509 - 0.87602",
 "0.99415 - 0.93509", "1.05322 - 0.99415", "1.11228 - 1.05322", "1.17135 - 1.11228", "1.23041 - 1.17135", "1.28947 - 1.23041", "1.34854 - 1.28947", "1.40760 - 1.34854", "1.46667 - 1.40760", "1.52573 - 1.46667",
 "1.5848 - 1.52573" , "1.64386 - 1.5848" , "1.70292 - 1.64386", "1.76199 - 1.70292", "1.82105 - 1.76199", "1.88012 - 1.82105", "1.93918 - 1.88012", "1.99825 - 1.93918", "2.05731 - 1.99825", "2.11637 - 2.05731",
 "2.17544 - 2.11637", "2.23450 - 2.17544" ,"2.29357 - 2.23450", "2.35263 - 2.29357", "2.4117 - 2.35263"]

    corr_counts =[ {'name': 'All', 'data': [ 3 ,  0 ,  0  , 0  , 0,   0 ,  0 ,  0 ,  3 ,  1  ,10  , 8 ,  3,   9  ,24 , 43 , 29,  61, 107 , 82 , 91, 104,  81  ,61 , 79 , 31 , 42 ,24  , 6   ,8 ,  5 ,  0 ,  0  , 0  , 5]}]

    check_rangesofbins(result['data']['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['data']['series'],corr_counts,10)



def test_Histogram_2():
    logging.info("---------- TEST 2: Histogram of right ententorhinal area by gender ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": "gender"},
            {"name": "bins", "value": "24"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
    ##Test 2
    > temp$y_M
      0  0  0  0  0  0  0  0  0  3 19 12 55 51 91 61 56 29 42 11 11  0  0  5
    > temp$y_W
     3  0  0  0  0  3  5 14  4 19 38 34 83 62 65 72 42 12 14  2  2  0  0  0

    > sprintf("%0.7s - %0.7s",temp2$xmax,temp2$xmin)
    [1] "0.43058 - 0.34445" "0.51672 - 0.43058" "0.60285 - 0.51672" "0.68899 - 0.60285" "0.77512 - 0.68899" "0.86126 - 0.77512"
 [7] "0.94739 - 0.86126" "1.03353 - 0.94739" "1.11966 - 1.03353" "1.20580 - 1.11966" "1.29193 - 1.20580" "1.37807 - 1.29193"
[13] "1.46421 - 1.37807" "1.55034 - 1.46421" "1.63648 - 1.55034" "1.72261 - 1.63648" "1.80875 - 1.72261" "1.89488 - 1.80875"
[19] "1.98102 - 1.89488" "2.06715 - 1.98102" "2.15329 - 2.06715" "2.23942 - 2.15329" "2.32556 - 2.23942" "2.4117 - 2.32556"

    '''

    corr_bins = [ "0.43058 - 0.34445", "0.51672 - 0.43058", "0.60285 - 0.51672", "0.68899 - 0.60285", "0.77512 - 0.68899" ,"0.86126 - 0.77512",
  "0.94739 - 0.86126", "1.03353 - 0.94739", "1.11966 - 1.03353" ,"1.20580 - 1.11966", "1.29193 - 1.20580", "1.37807 - 1.29193",
 "1.46421 - 1.37807", "1.55034 - 1.46421" ,"1.63648 - 1.55034", "1.72261 - 1.63648", "1.80875 - 1.72261" ,"1.89488 - 1.80875",
 "1.98102 - 1.89488", "2.06715 - 1.98102", "2.15329 - 2.06715", "2.23942 - 2.15329" ,"2.32556 - 2.23942", "2.4117 - 2.32556" ]

    corr_counts =[ {'name': 'M', 'data': [ 0 , 0  ,0  ,0 , 0,  0 , 0 , 0 , 0 , 3, 19, 12, 55, 51, 91, 61 ,56 ,29, 42 ,11, 11  ,0,  0 , 5]},
     {'name': 'F', 'data': [3 , 0,  0 , 0 , 0,  3,  5 ,14 , 4, 19, 38 ,34 ,83, 62, 65 ,72, 42 ,12, 14 , 2 , 2,  0 , 0  ,0]}]

    check_rangesofbins(result['data']['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['data']['series'],corr_counts,10)


def test_Histogram_3():
    logging.info("---------- TEST 3: Histogram of right ententorhinal area by alzheimer broad category ")

    data = [{ "name": "x", "value": "rightententorhinalarea"},
            {"name": "y", "value": "alzheimerbroadcategory"},
            {"name": "bins", "value": "19"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)
    '''
    > result_H2$y_AD
     [1]  0  0  0  0  1  8  7 24 32 56 55 44 15 16 13  2  0  0  1
    > result_H2$y_CN
     [1]  0  0  0  0  0  0  0  0  0 13 54 73 67 47 31  5  6  0  2
    > result_H2$y_Other
     [1]  1  0  0  0  0  2  2  7 13 26 25 24 13 16  8  5  3  0  1

    > sprintf("%0.7s - %0.7s",result2$xmax,result2$xmin)
     [1] "0.45325 - 0.34445" "0.56205 - 0.45325" "0.67085 - 0.56205" "0.77966 - 0.67085" "0.88846 - 0.77966" "0.99726 - 0.88846"
 [7] "1.10606 - 0.99726" "1.21487 - 1.10606" "1.32367 - 1.21487" "1.43247 - 1.32367" "1.54127 - 1.43247" "1.65008 - 1.54127"
[13] "1.75888 - 1.65008" "1.86768 - 1.75888" "1.97648 - 1.86768" "2.08529 - 1.97648" "2.19409 - 2.08529" "2.30289 - 2.19409"
[19] "2.4117 - 2.30289"

    '''
    corr_bins = [ "0.45325 - 0.34445" ,"0.56205 - 0.45325", "0.67085 - 0.56205" ,"0.77966 - 0.67085", "0.88846 - 0.77966", "0.99726 - 0.88846",
                "1.10606 - 0.99726", "1.21487 - 1.10606", "1.32367 - 1.21487" ,"1.43247 - 1.32367" ,"1.54127 - 1.43247" ,"1.65008 - 1.54127",
                "1.75888 - 1.65008" ,"1.86768 - 1.75888", "1.97648 - 1.86768", "2.08529 - 1.97648", "2.19409 - 2.08529", "2.30289 - 2.19409",
                "2.4117 - 2.30289"  ]

    corr_counts =[ {'name': 'AD', 'data': [ 0,  0 , 0 , 0 , 1 , 8 , 7 ,24, 32, 56 ,55 ,44, 15, 16, 13 , 2 , 0,  0,  1]},
                   {'name': 'CN', 'data': [ 0  ,0 , 0 , 0,  0 , 0 , 0,  0,  0, 13, 54, 73 ,67, 47, 31,  5 , 6 , 0 , 2]},
                   {'name': 'Other', 'data': [  1 , 0,  0,  0 , 0,  2,  2,  7, 13, 26, 25, 24 ,13, 16 , 8 , 5,  3 , 0 , 1]}]


    check_rangesofbins(result['data']['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['data']['series'],corr_counts,10)




def test_Histogram_4():
    logging.info("---------- TEST 4: Bar graph of alzheimer broad category ")

    data = [{ "name": "x", "value": "alzheimerbroadcategory"},
            {"name": "y", "value": ""},
            {"name": "bins", "value": ""},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": "{\"condition\": \"AND\", \"rules\": [{\"id\": \"rightententorhinalarea\",\"field\": \"rightententorhinalarea\",\"type\": \"double\", \"input\": \"number\", \"operator\": \"is_not_null\", \"value\": null}],\"valid\": true}"}]


    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)
    '''
    > result_H3$y_AD
    [1] 274
    > result_H3$y_CN
    [1] 298
    > result_H3$y_Other
    [1] 146
    '''
    corr_bins = ["Other","AD","CN"]
    corr_counts =[{'name': 'All', 'data': [146,274,298]}]

    check_rangesofbinsDiscreteVariable(result['data']['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['data']['series'],corr_counts,10)




def test_Histogram_5():
    logging.info("---------- TEST 5: Bar graph of alzheimer broad category by gender ")

    data = [{ "name": "x", "value": "alzheimerbroadcategory"},
            {"name": "y", "value": "gender"},
            {"name": "bins", "value": ""},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": "{\"condition\": \"AND\", \"rules\": [{\"id\": \"rightententorhinalarea\",\"field\": \"rightententorhinalarea\",\"type\": \"double\", \"input\": \"number\", \"operator\": \"is_not_null\", \"value\": null}],\"valid\": true}"}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    '''
     y_W_AD   y_M_AD     y_W_CN    y_M_CN    y_W_Other   y_M_Other
1    161	   113	      154	     144	   73	      73
    '''

    corr_bins = ["Other","AD","CN"]
    corr_counts =[{'name': 'F', 'data': [73,161,154]},
                  {'name': 'M', 'data': [73,113,144]}]

    check_rangesofbinsDiscreteVariable(result['data']['xAxis']['categories'],corr_bins)
    check_valuesofbins(result['data']['series'],corr_counts,10)



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
