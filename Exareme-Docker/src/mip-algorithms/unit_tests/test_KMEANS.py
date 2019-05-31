import requests
import json
import logging
from time import time as t
import math


url='http://88.197.53.38:9090/mining/query/KMEANS'


def test_KMEANS_1():
    logging.info("---------- TEST : KMEANS - Iris dataset  & 4 variables, 3 clusters")
    data = [
            {   "name": "iterations_max_number", "value": "50" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "Iris_Sepal_Length,Iris_Sepal_Width,Iris_Petal_Length,Iris_Petal_Width" },
            {   "name": "k", "value":""},
            {   "name":"centers", "value": "[{\"clid\":1, \"Iris_Sepal_Length\":6.0, \"Iris_Sepal_Width\":2.5, \"Iris_Petal_Length\":4.0 ,\"Iris_Petal_Width\":1.5 },\
            {\"clid\":2, \"Iris_Sepal_Length\":5.0, \"Iris_Sepal_Width\":3.5, \"Iris_Petal_Length\":1.5 ,\"Iris_Petal_Width\":0.5},\
            {\"clid\":3, \"Iris_Sepal_Length\":6.5, \"Iris_Sepal_Width\":3.0, \"Iris_Petal_Length\":6.0 ,\"Iris_Petal_Width\":2.0}]" },
            {   "name": "dataset", "value": "Iris" },
            {   "name": "e", "value": "0.01" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)


# KMEANS
#     k$centers
#       Sepal_Length Sepal_Width Petal_Length Petal_Width
#     1  5.883606557 2.740983607  4.388524590 1.434426230
#     2  5.006000000 3.418000000  1.464000000 0.244000000
#     3  6.853846154 3.076923077  5.715384615 2.053846154
#     k$size
#       [1] 61 50 39
    check_variablename(result['resources'][0]['data'][0][1:], ["Iris_Sepal_Length", "Iris_Sepal_Width","Iris_Petal_Length", "Iris_Petal_Width"] )
    check_variable(result['resources'][0]['data'][1], [1, 5.883606557, 2.740983607, 4.388524590, 1.434426230,61])
    check_variable(result['resources'][0]['data'][2], [2, 5.006000000, 3.418000000, 1.464000000, 0.244000000,50])
    check_variable(result['resources'][0]['data'][3], [3, 6.853846154, 3.076923077, 5.715384615 ,2.053846154,39])


def test_KMEANS_2():
    logging.info("---------- TEST : KMEANS - desd-synthdata   & 4 variables,  5 clusters")
    data = [
            {   "name": "iterations_max_number", "value": "50" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "rightpallidum,leftpallidum,lefthippocampus,righthippocampus" },
            {   "name": "k", "value":""},
            {   "name":"centers", "value": "[{\"clid\":1,\"rightpallidum\":0.2,\"leftpallidum\":0.5,\"lefthippocampus\":1.7,\"righthippocampus\":1.5},\
             {\"clid\":2,\"rightpallidum\":0.6,\"leftpallidum\":1.2,\"lefthippocampus\":2.5,\"righthippocampus\":2.0},\
             {\"clid\":3,\"rightpallidum\":1.0,\"leftpallidum\":1.5,\"lefthippocampus\":3.9,\"righthippocampus\":2.5},\
             {\"clid\":4,\"rightpallidum\":1.5,\"leftpallidum\":2.0,\"lefthippocampus\":4.0,\"righthippocampus\":3.0},\
            { \"clid\":5,\"rightpallidum\":2.0,\"leftpallidum\":2.2,\"lefthippocampus\":2.3,\"righthippocampus\":4.0}]" },
            {   "name": "dataset", "value": "desd-synthdata" },
            {   "name": "e", "value": "0.0001" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]


    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)

    # result ={"resources":[{"name":"KMEANS_TABLE","profile":"tabular-data-resource","data":[["cluster id","rightpallidum","leftpallidum","lefthippocampus","righthippocampus","number of points"],[1.0,1.16975764706,1.17013882353,2.04674117647,2.33005,34.0],[2.0,1.45123097345,1.44481637168,2.61178185841,2.8066039823,226.0],[3.0,1.63644254545,1.62866181818,3.26815854545,3.48492,275.0],[4.0,1.78926969697,1.76314848485,3.69845454545,3.93534242424,66.0],[5.0,1.47848721003,1.46940445141,2.97324451411,3.16225297806,319.0]],"schema":{"fields":[{"name":"cluster id","type":"int"},{"name":"rightpallidum","type":"real"},{"name":"leftpallidum","type":"real"},{"name":"lefthippocampus","type":"real"},{"name":"righthippocampus","type":"real"},{"name":"number of points","type":"int"}]}}]}

# KMEANS
    # k$centers
    #         rightpallidum     leftpallidum  lefthippocampus righthippocampus
    # 1          1.169757647         1.170138824            2.046741176             2.330050000
    # 2          1.451230973         1.444816372            2.611781858             2.806603982
    # 3          1.636442545         1.628661818            3.268158545             3.484920000
    # 4          1.789269697         1.763148485            3.698454545             3.935342424
    # 5          1.478487210         1.469404451            2.973244514             3.162252978
    # k$size
    # [1]  34 226 275  66 319

    check_variable(result['resources'][0]['data'][1], [1, 1.169757647, 1.170138824, 2.046741176, 2.330050000,34])
    check_variable(result['resources'][0]['data'][2], [2, 1.451230973, 1.444816372, 2.611781858, 2.806603982,226])
    check_variable(result['resources'][0]['data'][3], [3,  1.636442545 ,1.628661818, 3.268158545,3.48492000039,275])
    check_variable(result['resources'][0]['data'][4], [4,1.789269697, 1.763148485, 3.698454545, 3.935342424,66])
    check_variable(result['resources'][0]['data'][5], [5, 1.478487210, 1.469404451 ,2.973244514, 3.162252978,319])



def test_KMEANS_Privacy():
    """

    """

    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [
            {   "name": "iterations_max_number", "value": "50" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "rightpallidum,leftpallidum,lefthippocampus,righthippocampus" },
            {   "name": "k", "value":""},
            {   "name":"centers", "value": "[{\"clid\":1,\"rightpallidum\":0.2,\"leftpallidum\":0.5,\"lefthippocampus\":1.7,\"righthippocampus\":1.5},\
             {\"clid\":2,\"rightpallidum\":0.6,\"leftpallidum\":1.2,\"lefthippocampus\":2.5,\"righthippocampus\":2.0},\
             {\"clid\":3,\"rightpallidum\":1.0,\"leftpallidum\":1.5,\"lefthippocampus\":3.9,\"righthippocampus\":2.5},\
             {\"clid\":4,\"rightpallidum\":1.5,\"leftpallidum\":2.0,\"lefthippocampus\":4.0,\"righthippocampus\":3.0},\
            { \"clid\":5,\"rightpallidum\":2.0,\"leftpallidum\":2.2,\"lefthippocampus\":2.3,\"righthippocampus\":4.0}]" },
            {   "name": "dataset", "value": "adni_9rows" },
            {   "name": "e", "value": "0.0001" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)

    check_privacy_result(r.text)



def check_privacy_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"


def check_variablename(exareme_result,corr_result):
     for i in range(len(corr_result)):
         assert (str(exareme_result[i])==corr_result[i])

def check_variable(exareme_result,corr_result):
     assert (int(exareme_result[0]) == corr_result[0])
     for i in range(1,len(corr_result)-1):
         assert (math.isclose(float(exareme_result[i]),corr_result[i],rel_tol=0,abs_tol=1e-03))
     assert (int(exareme_result[-1]) == corr_result[-1])
