import requests
import json
import logging
import math
from decimal import *


url='http://localhost:9090/mining/query/ANOVA'


def test_ANOVA_1():
    logging.info("---------- TEST : ANOVA - Tests With Balanced data set(data_ANOVA_Balanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type I ANOVA.  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
            {   "name": "y", "value": "ANOVA_var_D" },
            {   "name": "type", "value": "1" },
            {   "name": "dataset", "value": "ANOVA_Balanced_with_inter_V1V2" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    # exaremeresult ='{"resources": [{ "name": "ANOVA_TABLE","profile": "tabular-data-resource","data": [ ["model variables","sum of squares","Df","mean square","f","p","eta squared","part eta squared","omega squared" ],["ANOVA_var_I1",313447.744445,2,156723.872223,77571.8524291,3.05165932445e-242,0.955176956351,0.998956896013,0.955158762254],["ANOVA_var_I2",14193.611111,2,7096.8055555,3512.62603114,3.88260499978e-134,0.0432525373077,0.977460092036,0.0432399576434],["ANOVA_var_I3",113.605555,1,113.605555,56.2300638863,4.00217903518e-12,0.000346192978487,0.257664149865,0.000340034161086],["ANOVA_var_I1:ANOVA_var_I2",63.122223,4,15.78055575,7.81072420257,8.81901178094e-06,0.000192353889641,0.161676818791,0.00016772596138],["ANOVA_var_I1:ANOVA_var_I3",4.81111099996,2,2.40555549998,1.19065075159,0.306669502129,1.46610158888e-05,0.014486449988,2.3475536263e-06],["ANOVA_var_I2:ANOVA_var_I3",5.87777700002,2,2.93888850001,1.45462858845,0.236520238058,1.79114932057e-05,0.017641563771,5.59801093111e-06],["ANOVA_var_I1:ANOVA_var_I2:ANOVA_var_I3",0.655556000012,4,0.163889000003,0.0811182951436,0.988064640922,1.99769178725e-06,0.00199891719478,-2.26290645103e-05],["residuals",327.3,162,2.02037037037]], "schema":  { "fields": [{"name": "model variables","type": "text"},{"name": "sum of squares","type": "number"},{"name": "Df","type": "number"},{"name": "mean square","type": "number"},{"name": "f","type": "number"},{"name": "p","type": "number"},{"name": "eta squared","type": "number"},{"name": "part eta squared","type": "number"},{"name": "omega squared","type": "number"} ]}}]}'
    # result = json.loads(exaremeresult)


##  ANOVA
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##                            Sum of Squares    df     Mean Square    F             p         η²       η²p
##  ─────────────────────────────────────────────────────────────────────────────────────────────────────────
##    var_I1                      313447.744      2     156723.872    77571.8524    < .001    0.955    0.999
##    var_I2                       14193.611      2       7096.806     3512.6260    < .001    0.043    0.977
##    var_I3                         113.606      1        113.606       56.2301    < .001    0.000    0.258
##    var_I1:var_I2                   63.122      4         15.781        7.8107    < .001    0.000    0.162
##    var_I1:var_I3                    4.811      2          2.406        1.1907     0.307    0.000    0.014
##    var_I2:var_I3                    5.878      2          2.939        1.4546     0.237    0.000    0.018
##    var_I1:var_I2:var_I3             0.656      4          0.164        0.0811     0.988    0.000    0.002
##    Residuals                      327.300    162          2.020

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1', 313447.744, 2, 156723.872, 77571.8524, '< .001', 0.955, 0.999)
    check_variable(result['resources'][0]['data'][2],'ANOVA_var_I2', 14193.611, 2, 7096.806, 3512.6260, '< .001', 0.043, 0.977)
    check_variable(result['resources'][0]['data'][3],'ANOVA_var_I3',113.606, 1, 113.606, 56.2301, '< .001', 0.000, 0.258 )
    check_variable(result['resources'][0]['data'][4],'ANOVA_var_I1:ANOVA_var_I2',63.122, 4, 15.781, 7.8107, '< .001',0.000,0.162 )
    check_variable(result['resources'][0]['data'][5],'ANOVA_var_I1:ANOVA_var_I3', 4.811, 2, 2.406, 1.1907, 0.307,0.000,0.014 )
    check_variable(result['resources'][0]['data'][6],'ANOVA_var_I2:ANOVA_var_I3',5.878, 2, 2.939, 1.4546, 0.237,0.000,0.018 )
    check_variable(result['resources'][0]['data'][7],'ANOVA_var_I1:ANOVA_var_I2:ANOVA_var_I3',0.656, 4, 0.164, 0.0811, 0.988,0.000,0.002)
    check_variable(result['resources'][0]['data'][8],'residuals',327.300, 162,  2.020)

def test_ANOVA_2():
    logging.info("---------- TEST : ANOVA - Tests With Balanced data set(data_ANOVA_Balanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type II ANOVA.  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
            {   "name": "y", "value": "ANOVA_var_D" },
            {   "name": "type", "value": "2" },
            {   "name": "dataset", "value": "ANOVA_Balanced_with_inter_V1V2" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

    # result =json.loads('{"resources": [{ "name": "ANOVA_TABLE","profile": "tabular-data-resource","data": [ ["model variables","sum of squares","Df","mean square","f","p","eta squared","part eta squared","omega squared" ],["ANOVA_var_I1",313447.744444,2,156723.872222,77571.8524289,3.05165932523338e-242,0.287681018647,0.998956896013,0.287676776637],["ANOVA_var_I2",14193.611111,2,7096.8055555,3512.62603114,3.88260499977796e-134,0.0130268364507,0.977460092036,0.0130231037273],["ANOVA_var_I3",113.605555,1,113.605555,56.2300638863,4.00217903517677e-12,0.000104266699524,0.257664149865,0.000102412222212],["ANOVA_var_I1:ANOVA_var_I2",63.122222,4,15.7805555,7.81072407882,8.81901350190084e-06,5.79333092872e-05,0.161676816644,5.05160659761e-05],["ANOVA_var_I1:ANOVA_var_I3",4.81111100002,2,2.40555550001,1.19065075161,0.306669502124393,4.41561739666e-06,0.0144864499882,7.07041265744e-07],["ANOVA_var_I2:ANOVA_var_I3",5.87777700002,2,2.93888850001,1.45462858845,0.236520238057619,5.39459895539e-06,0.017641563771,1.68602100916e-06],["ANOVA_var_I1:ANOVA_var_I2:ANOVA_var_I3",0.655556000012,4,0.163889000003,0.0811182951436,0.988064640921921,6.01666533599e-07,0.00199891719478,-6.81547046828e-06],["residuals",327.3,162,2.02037037037]], "schema":  { "fields": [{"name": "model variables","type": "text"},{"name": "sum of squares","type": "number"},{"name": "Df","type": "number"},{"name": "mean square","type": "number"},{"name": "f","type": "number"},{"name": "p","type": "number"},{"name": "eta squared","type": "number"},{"name": "part eta squared","type": "number"},{"name": "omega squared","type": "number"} ]}}]}')
    #



##  ANOVA
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##                            Sum of Squares    df     Mean Square    F             p         η²       η²p
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##    var_I1                      313447.744      2     156723.872    77571.8524    < .001    0.955    0.999
##    var_I2                       14193.611      2       7096.806     3512.6260    < .001    0.043    0.977
##    var_I3                         113.606      1        113.606       56.2301    < .001    0.000    0.258
##    var_I1:var_I2                   63.122      4         15.781        7.8107    < .001    0.000    0.162
##    var_I1:var_I3                    4.811      2          2.406        1.1907     0.307    0.000    0.014
##    var_I2:var_I3                    5.878      2          2.939        1.4546     0.237    0.000    0.018
##    var_I1:var_I2:var_I3             0.656      4          0.164        0.0811     0.988    0.000    0.002
##    Residuals                      327.300    162          2.020
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1',  313447.744, 2, 156723.872, 77571.8524, '< .001', 0.955, 0.999)
    check_variable(result['resources'][0]['data'][2],'ANOVA_var_I2', 14193.611, 2, 7096.806, 3512.6260, '< .001', 0.043, 0.977)
    check_variable(result['resources'][0]['data'][3],'ANOVA_var_I3',113.606, 1, 113.606, 56.2301, '< .001',0.000,0.258)
    check_variable(result['resources'][0]['data'][4],'ANOVA_var_I1:ANOVA_var_I2', 63.122, 4, 15.781, 7.8107, '< .001', 0.000, 0.162)
    check_variable(result['resources'][0]['data'][5],'ANOVA_var_I1:ANOVA_var_I3',  4.811, 2, 2.406, 1.1907, 0.307, 0.000, 0.014)
    check_variable(result['resources'][0]['data'][6],'ANOVA_var_I2:ANOVA_var_I3', 5.878, 2, 2.939, 1.4546, 0.237, 0.000, 0.018)
    check_variable(result['resources'][0]['data'][7],'ANOVA_var_I1:ANOVA_var_I2:ANOVA_var_I3',0.656, 4, 0.164, 0.0811, 0.988, 0.000, 0.002)
    check_variable(result['resources'][0]['data'][8],'residuals', 327.300, 162, 2.020)



def test_ANOVA_3():
    logging.info("---------- TEST : ANOVA - Tests With Balanced data set(data_ANOVA_Balanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type III ANOVA.  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
            {   "name": "y", "value": "ANOVA_var_D" },
            {   "name": "type", "value": "3" },
            {   "name": "dataset", "value": "ANOVA_Balanced_with_inter_V1V2" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##                            Sum of Squares    df     Mean Square    F             p         η²       η²p
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##    var_I1                      313447.744      2     156723.872    77571.8524    < .001    0.955    0.999
##    var_I2                       14193.611      2       7096.806     3512.6260    < .001    0.043    0.977
##    var_I3                         113.606      1        113.606       56.2301    < .001    0.000    0.258
##    var_I1:var_I2                   63.122      4         15.781        7.8107    < .001    0.000    0.162
##    var_I1:var_I3                    4.811      2          2.406        1.1907     0.307    0.000    0.014
##    var_I2:var_I3                    5.878      2          2.939        1.4546     0.237    0.000    0.018
##    var_I1:var_I2:var_I3             0.656      4          0.164        0.0811     0.988    0.000    0.002
##    Residuals                      327.300    162          2.020
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1',  313447.744,  2, 156723.872, 77571.8524, '< .001', 0.955, 0.999  )
    check_variable(result['resources'][0]['data'][2],'ANOVA_var_I2', 14193.611,  2,   7096.806, 3512.6260, '< .001', 0.043, 0.977 )
    check_variable(result['resources'][0]['data'][3],'ANOVA_var_I3',  113.606,  1, 113.606, 56.2301, '< .001', 0.000, 0.258 )
    check_variable(result['resources'][0]['data'][4],'ANOVA_var_I1:ANOVA_var_I2', 63.122, 4, 15.781, 7.8107, '< .001', 0.000, 0.162 )
    check_variable(result['resources'][0]['data'][5],'ANOVA_var_I1:ANOVA_var_I3',  4.811,  2, 2.406, 1.1907, 0.307, 0.000, 0.014   )
    check_variable(result['resources'][0]['data'][6],'ANOVA_var_I2:ANOVA_var_I3', 5.878,  2, 2.939, 1.4546, 0.237, 0.000, 0.018)
    check_variable(result['resources'][0]['data'][7],'ANOVA_var_I1:ANOVA_var_I2:ANOVA_var_I3',0.656, 4, 0.164, 0.0811, 0.988, 0.000, 0.002  )
    check_variable(result['resources'][0]['data'][8],'residuals', 327.300, 162, 2.020  )


## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ##
## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ##
## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ##


def test_ANOVA_4():
    logging.info("---------- TEST : ANOVA - Tests With Unbalanced data set(data_ANOVA_Unbalanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type I ANOVA.  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
            {   "name": "y", "value": "ANOVA_var_D" },
            {   "name": "type", "value": "1" },
            {   "name": "dataset", "value": "ANOVA_UnBalanced_with_inter_V1V2" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##                            Sum of Squares    df     Mean Square    F             p         η²       η²p
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##    var_I1                      382587.870      2     191293.935    93539.7533    < .001    0.958    0.999
##    var_I2                       16283.869      2       8141.935     3981.2791    < .001    0.041    0.978
##    var_I3                         134.480      1        134.480       65.7586    < .001    0.000    0.265
##    var_I1:var_I2                   73.281      4         18.320        8.9583    < .001    0.000    0.164
##    var_I1:var_I3                    4.920      2          2.460        1.2029     0.303    0.000    0.013
##    var_I2:var_I3                    6.943      2          3.471        1.6975     0.186    0.000    0.018
##    var_I1:var_I2:var_I3             0.657      4          0.164        0.0803     0.988    0.000    0.002
##    Residuals                      372.200    182          2.045
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1',  382587.870,  2, 191293.935,93539.7533,'< .001',0.958,0.999   )
    check_variable(result['resources'][0]['data'][2],'ANOVA_var_I2', 16283.869,  2,   8141.935, 3981.2791,'< .001',0.041,0.978)
    check_variable(result['resources'][0]['data'][3],'ANOVA_var_I3',  134.480,  1,    134.480, 65.7586,'< .001',0.000,0.265  )
    check_variable(result['resources'][0]['data'][4],'ANOVA_var_I1:ANOVA_var_I2',  73.281,  4, 18.320,    8.9583,'< .001',0.000,0.164  )
    check_variable(result['resources'][0]['data'][5],'ANOVA_var_I1:ANOVA_var_I3',  4.920,  2, 2.460,    1.2029, 0.303,0.000,0.013   )
    check_variable(result['resources'][0]['data'][6],'ANOVA_var_I2:ANOVA_var_I3', 6.943,  2,  3.471,    1.6975, 0.186,0.000,0.018    )
    check_variable(result['resources'][0]['data'][7],'ANOVA_var_I1:ANOVA_var_I2:ANOVA_var_I3',0.657,  4,  0.164, 0.0803, 0.988,0.000,0.002     )
    check_variable(result['resources'][0]['data'][8],'residuals', 372.200,182,  2.045  )



def test_ANOVA_5():
    logging.info("---------- TEST : ANOVA - Tests With Unbalanced data set(data_ANOVA_Unbalanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type II ANOVA.  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
            {   "name": "y", "value": "ANOVA_var_D" },
            {   "name": "type", "value": "2" },
            {   "name": "dataset", "value": "ANOVA_UnBalanced_with_inter_V1V2" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##                            Sum of Squares    df     Mean Square    F             p         η²       η²p
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##    var_I1                      353945.602      2     176972.801    86536.9420    < .001    0.954    0.999
##    var_I2                       16283.869      2       8141.935     3981.2791    < .001    0.044    0.978
##    var_I3                         134.480      1        134.480       65.7586    < .001    0.000    0.265
##    var_I1:var_I2                   73.281      4         18.320        8.9583    < .001    0.000    0.164
##    var_I1:var_I3                    4.810      2          2.405        1.1759     0.311    0.000    0.013
##    var_I2:var_I3                    6.943      2          3.471        1.6975     0.186    0.000    0.018
##    var_I1:var_I2:var_I3             0.657      4          0.164        0.0803     0.988    0.000    0.002
##    Residuals                      372.200    182          2.045
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1',  353945.602,  2, 176972.801,86536.9420,'< .001',0.954,0.999)
    check_variable(result['resources'][0]['data'][2],'ANOVA_var_I2', 16283.869,  2,   8141.935, 3981.2791,'< .001',0.044,0.978)
    check_variable(result['resources'][0]['data'][3],'ANOVA_var_I3',  134.480,  1,134.480,   65.7586,'< .001',0.000,0.265)
    check_variable(result['resources'][0]['data'][4],'ANOVA_var_I1:ANOVA_var_I2',  73.281,  4, 18.320,8.9583,'< .001',0.000,0.164)
    check_variable(result['resources'][0]['data'][5],'ANOVA_var_I1:ANOVA_var_I3',  4.810,  2,  2.405,1.1759, 0.311,0.000,0.013)
    check_variable(result['resources'][0]['data'][6],'ANOVA_var_I2:ANOVA_var_I3',6.943,  2,  3.471,1.6975, 0.186,0.000,0.018)
    check_variable(result['resources'][0]['data'][7],'ANOVA_var_I1:ANOVA_var_I2:ANOVA_var_I3',0.657,  4,  0.164,0.0803, 0.988,0.000,0.002)
    check_variable(result['resources'][0]['data'][8],'residuals', 372.200,182, 2.045)


def test_ANOVA_6():
    logging.info("---------- TEST : ANOVA - Tests With Unbalanced data set(data_ANOVA_Unbalanced_with_inter_V1V2.csv) - Test the 3 main effects and all the interactions with type III ANOVA.  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
            {   "name": "y", "value": "ANOVA_var_D" },
            {   "name": "type", "value": "3" },
            {   "name": "dataset", "value": "ANOVA_UnBalanced_with_inter_V1V2" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

## ANOVA
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##                            Sum of Squares    df     Mean Square    F             p         η²       η²p
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────
##    var_I1                      342798.312      2     171399.156    83811.5165    < .001    0.955    0.999
##    var_I2                       15541.146      2       7770.573     3799.6891    < .001    0.043    0.977
##    var_I3                         120.288      1        120.288       58.8191    < .001    0.000    0.244
##    var_I1:var_I2                   73.281      4         18.320        8.9583    < .001    0.000    0.164
##    var_I1:var_I3                    4.812      2          2.406        1.1766     0.311    0.000    0.013
##    var_I2:var_I3                    6.612      2          3.306        1.6167     0.201    0.000    0.017
##    var_I1:var_I2:var_I3             0.657      4          0.164        0.0803     0.988    0.000    0.002
##    Residuals                      372.200    182          2.045
##  ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1', 342798.312, 2, 171399.156,83811.5165,'< .001', 0.955, 0.999 )
    check_variable(result['resources'][0]['data'][2],'ANOVA_var_I2', 15541.146, 2, 7770.573, 3799.6891,'< .001', 0.043, 0.977 )
    check_variable(result['resources'][0]['data'][3],'ANOVA_var_I3', 120.288,  1, 120.288, 58.8191,'< .001', 0.000, 0.244)
    check_variable(result['resources'][0]['data'][4],'ANOVA_var_I1:ANOVA_var_I2', 73.281, 4, 18.320, 8.9583, '< .001', 0.000, 0.164 )
    check_variable(result['resources'][0]['data'][5],'ANOVA_var_I1:ANOVA_var_I3', 4.812, 2, 2.406, 1.1766, 0.311, 0.000, 0.013 )
    check_variable(result['resources'][0]['data'][6],'ANOVA_var_I2:ANOVA_var_I3', 6.612,  2,  3.306, 1.6167, 0.201, 0.000, 0.017 )
    check_variable(result['resources'][0]['data'][7],'ANOVA_var_I1:ANOVA_var_I2:ANOVA_var_I3', 0.657, 4, 0.164, 0.0803, 0.988, 0.000, 0.002 )
    check_variable(result['resources'][0]['data'][8],'residuals', 372.200,182, 2.045 )



def test_ANOVA_7():
    logging.info("---------- TEST : ANOVA - Tests With Unbalanced data set(data_ANOVA_Unbalanced_with_inter_V1V2.csv) - Test the 3 main effects only with type III ANOVA.  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "x", "value": "ANOVA_var_I1+ANOVA_var_I2+ANOVA_var_I3" },
            {   "name": "y", "value": "ANOVA_var_D" },
            {   "name": "type", "value": "3" },
            {   "name": "dataset", "value": "ANOVA_UnBalanced_with_inter_V1V2" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "pfa" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##  ────────────────────────────────────────────────────────────────────────────────────────────
##                 Sum of Squares    df     Mean Square    F          p         η²       η²p
##  ────────────────────────────────────────────────────────────────────────────────────────────
##    var_I1               353946      2      176972.80    74962.1    < .001    0.954    0.999
##    var_I2                16284      2        8141.93     3448.8    < .001    0.044    0.973
##    var_I3                  134      1         134.48       57.0    < .001    0.000    0.227
##    Residuals               458    194           2.36
##  ────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_var_I1', 353946,  2,  176972.80,74962.1,'< .001',0.954,0.999)
    check_variable(result['resources'][0]['data'][2],'ANOVA_var_I2', 16284,  2,8141.93, 3448.8,'< .001',0.044,0.973 )
    check_variable(result['resources'][0]['data'][3],'ANOVA_var_I3', 134,  1, 134.48,   57.0,'< .001',0.000,0.227)
    check_variable(result['resources'][0]['data'][4],'residuals', 458, 194, 2.36 )


## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ##
## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ##
## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ##


def test_ANOVA_8():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - without interaction and 2 variables - type III ")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory+ANOVA_gender" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "3" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────
##                              Sum of Squares    df     Mean Square    F        p         η²       η²p
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory              11.6      2          5.789     49.5    < .001    0.106    0.122
##    gender                              14.1      1         14.058    120.3    < .001    0.129    0.144
##    Residuals                           83.5    714          0.117
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────


    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 11.6, 2, 5.789, 49.5, '< .001',  0.106,   0.122)
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender', 14.1, 1 , 14.058, 120.3 , '< .001',   0.129,    0.144  )
    check_variable(result['resources'][0]['data'][3],'residuals', 83.5 , 714 , 0.117)



def test_ANOVA_9():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - without interaction and 2 variables - type II")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory+ANOVA_gender" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "2" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##   ───────────────────────────────────────────────────────────────────────────────────────────────────────
##                               Sum of Squares    df     Mean Square    F        p         η²       η²p
##   ───────────────────────────────────────────────────────────────────────────────────────────────────────
##     alzheimerbroadcategory              11.6      2          5.789     49.5    < .001    0.106    0.122
##    gender                               14.1      1         14.058    120.3    < .001    0.129    0.144
##     Residuals                           83.5    714          0.117
##   ───────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 11.6, 2, 5.789, 49.5,  '< .001',  0.106,   0.122)
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender', 14.1, 1 , 14.058, 120.3 , '< .001',   0.129,    0.144  )
    check_variable(result['resources'][0]['data'][3],'residuals', 83.5 , 714 , 0.117)



def test_ANOVA_10():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with interaction and 2 variables - type III")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "3" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)


##  ANOVA
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────
##                                     Sum of Squares    df     Mean Square    F         p         η²       η²p
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory                    10.63      2          5.315     46.13    < .001    0.100    0.115
##    gender                                    12.48      1         12.479    108.32    < .001    0.117    0.132
##    alzheimerbroadcategory:gender              1.42      2          0.712      6.18     0.002    0.013    0.017
##    Residuals                                 82.03    712          0.115
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 10.63 , 2, 5.315 , 46.13,'< .001',0.100 , 0.115)
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender', 12.48,1 ,12.479 , 108.32,'< .001',0.117,0.132 )
    check_variable(result['resources'][0]['data'][3],'ANOVA_alzheimerbroadcategory:ANOVA_gender',1.42 , 2 , 0.712,6.18 ,0.002,0.013 , 0.017)
    check_variable(result['resources'][0]['data'][4],'residuals',82.03 , 712 ,  0.115)




def test_ANOVA_11():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with interaction and 2 variables - type II")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "2" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────
##                                     Sum of Squares    df     Mean Square    F         p         η²       η²p
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory                    11.58      2          5.789     50.24    < .001    0.106    0.124
##    gender                                    14.06      1         14.058    122.02    < .001    0.129    0.146
##    alzheimerbroadcategory:gender              1.42      2          0.712      6.18     0.002    0.013    0.017
##    Residuals                                 82.03    712          0.115
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 11.58,2, 5.789, 50.24, '< .001',    0.106 ,   0.124 )
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender',  14.06,      1 ,        14.058 ,   122.02  ,  '< .001'   , 0.129 ,   0.146 )
    check_variable(result['resources'][0]['data'][3],'ANOVA_alzheimerbroadcategory:ANOVA_gender',1.42   ,   2  ,        0.712 ,     6.18,     0.002  ,  0.013 ,   0.017 )
    check_variable(result['resources'][0]['data'][4],'residuals',82.03  ,  712      ,    0.115 )



def test_ANOVA_12():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - without interaction and 3 variables - type III")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory+ANOVA_gender+ANOVA_agegroup" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "3" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

# {"resources": [{ "name": "ANOVA_TABLE","profile": "tabular-data-resource","data": [
# ["model variables","sum of squares","Df","mean square","f","p","eta squared","part eta squared","omega squared" ],
# ["ANOVA_alzheimerbroadcategory",9.1380378257,2,4.56901891285,40.0033785358,3.44967116564e-17,0.086696720645,0.101273509822,0.0844379872018],
# ["ANOVA_gender",12.8108832986,1,12.8108832986,112.163819793,1.95626293326e-24,0.121542675981,0.136425146781,0.120328668251],
# ["ANOVA_agegroup",2.3601894963,4,0.590047374075,5.16607370409,0.000416707358258,0.0223921911173,0.0282815171933,0.0180381747076],
# ["residuals",81.0932362931,710,0.114215825765]], "schema":  { "fields": [{"name": "model variables","type": "text"},{"name": "sum of squares","type": "number"},{"name": "Df","type": "number"},{"name": "mean square","type": "number"},{"name": "f","type": "number"},{"name": "p","type": "number"},{"name": "eta squared","type": "number"},{"name": "part eta squared","type": "number"},{"name": "omega squared","type": "number"} ]}}]}



##  ANOVA
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────
##                              Sum of Squares    df     Mean Square    F         p         η²       η²p
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory              9.14      2          4.569     40.00    < .001    0.087    0.101
##    gender                             12.81      1         12.811    112.16    < .001    0.122    0.136
##    agegroup                            2.36      4          0.590      5.17    < .001    0.022    0.028
##    Residuals                          81.09    710          0.114
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 9.14 ,     2 ,         4.569 ,    40.00 ,   '< .001',   0.087 ,   0.101  )
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender',  12.81   ,   1     ,    12.811 ,   112.16  ,  '< .001' ,  0.122   , 0.136 )
    check_variable(result['resources'][0]['data'][3],'ANOVA_agegroup',   2.36   ,   4    ,      0.590  ,    5.17 ,   '< .001'   , 0.022  ,  0.028 )
    check_variable(result['resources'][0]['data'][4],'residuals',81.09   , 710  ,        0.114    )



def test_ANOVA_13():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - without interaction and 3 variables - type II")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory+ANOVA_gender+ANOVA_agegroup" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "2" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────
##                              Sum of Squares    df     Mean Square    F         p         η²       η²p
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory              9.14      2          4.569     40.00    < .001    0.087    0.101
##    gender                             12.81      1         12.811    112.16    < .001    0.122    0.136
##    agegroup                            2.36      4          0.590      5.17    < .001    0.022    0.028
##    Residuals                          81.09    710          0.114
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────
    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 9.14 ,     2 ,         4.569 ,    40.00 ,   '< .001',   0.087 ,   0.101  )
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender',  12.81   ,   1     ,    12.811 ,   112.16  ,  '< .001' ,  0.122   , 0.136 )
    check_variable(result['resources'][0]['data'][3],'ANOVA_agegroup',   2.36   ,   4    ,      0.590  ,    5.17 ,   '< .001'   , 0.022  ,  0.028 )
    check_variable(result['resources'][0]['data'][4],'residuals',81.09   , 710  ,        0.114    )




def test_ANOVA_14():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with 1 interaction and 3 variables - type III")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender+ANOVA_agegroup" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "3" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)

##  ANOVA
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────
##                                     Sum of Squares    df     Mean Square    F         p         η²       η²p
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory                     8.48      2          4.242     37.58    < .001    0.082    0.096
##    gender                                    11.36      1         11.357    100.61    < .001    0.110    0.124
##    agegroup                                   2.11      4          0.528      4.68    < .001    0.020    0.026
##    alzheimerbroadcategory:gender              1.17      2          0.587      5.20     0.006    0.011    0.014
##    Residuals                                 79.92    708          0.113
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 8.48   ,   2  ,        4.242  ,   37.58,    '< .001'  ,  0.082 ,   0.096    )
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender',  11.36   ,   1     ,    11.357  ,  100.61 ,   '< .001'  ,  0.110 ,   0.124 )
    check_variable(result['resources'][0]['data'][3],'ANOVA_agegroup',   2.11  ,    4   , 0.528  ,    4.68,    '< .001' ,   0.020  ,  0.026  )
    check_variable(result['resources'][0]['data'][4],'ANOVA_alzheimerbroadcategory:ANOVA_gender',   1.17 ,     2  ,        0.587  ,    5.20 ,    0.006 ,   0.011 ,   0.014  )
    check_variable(result['resources'][0]['data'][5],'residuals', 79.92  ,  708   ,  0.113  )






def test_ANOVA_15():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with 1 interaction and 3 variables - type II")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender+ANOVA_agegroup" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "2" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)




##  ANOVA
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────
##                                     Sum of Squares    df     Mean Square    F         p         η²       η²p
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory                     9.14      2          4.569     40.48    < .001    0.087    0.103
##    gender                                    12.81      1         12.811    113.49    < .001    0.122    0.138
##    agegroup                                   2.11      4          0.528      4.68    < .001    0.020    0.026
##    alzheimerbroadcategory:gender              1.17      2          0.587      5.20     0.006    0.011    0.014
##    Residuals                                 79.92    708          0.113
##  ───────────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 9.14 ,  2, 4.569, 40.48 , '< .001',    0.087,    0.103    )
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender',  12.81 , 1    ,12.811 ,   113.49 ,   '< .001' ,   0.122 ,   0.138   )
    check_variable(result['resources'][0]['data'][3],'ANOVA_agegroup', 2.11 , 4    ,  0.528 ,     4.68,    '< .001' ,   0.020 ,   0.026   )
    check_variable(result['resources'][0]['data'][4],'ANOVA_alzheimerbroadcategory:ANOVA_gender',   1.17  ,    2  ,        0.587  ,    5.20  ,   0.006 ,   0.011  ,  0.014   )
    check_variable(result['resources'][0]['data'][5],'residuals',  79.92  ,  708  ,        0.113  )





def test_ANOVA_16():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with full interaction and 3 variables - type III")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender*ANOVA_agegroup" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "3" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)


##  ANOVA
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
##                                              Sum of Squares    df     Mean Square    F         p         η²       η²p
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory                             2.311      2         1.1557    10.210    < .001    0.026    0.029
##    gender                                             3.714      1         3.7135    32.807    < .001    0.042    0.046
##    agegroup                                           1.710      4         0.4275     3.777     0.005    0.019    0.021
##    alzheimerbroadcategory:gender                      0.266      2         0.1328     1.173     0.310    0.003    0.003
##    alzheimerbroadcategory:agegroup                    0.773      8         0.0967     0.854     0.555    0.009    0.010
##    gender:agegroup                                    0.226      4         0.0564     0.499     0.737    0.003    0.003
##    alzheimerbroadcategory:gender:agegroup             0.857      8         0.1072     0.947     0.477    0.010    0.011
##    Residuals                                         77.876    688         0.1132
##  ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────

    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 2.311  ,    2  ,       1.1557 ,   10.210 ,   '< .001' ,   0.026 ,   0.029      )
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender',   3.714  ,    1  ,       3.7135 ,   32.807,    '< .001' ,   0.042 ,   0.046     )
    check_variable(result['resources'][0]['data'][3],'ANOVA_agegroup',  1.710  ,    4    ,     0.4275 ,    3.777 ,    0.005 ,   0.019 ,   0.021    )
    check_variable(result['resources'][0]['data'][4],'ANOVA_alzheimerbroadcategory:ANOVA_gender',     0.266  ,    2    ,     0.1328 ,   1.173  ,   0.310  ,  0.003 ,   0.003   )
    check_variable(result['resources'][0]['data'][5],'ANOVA_alzheimerbroadcategory:ANOVA_agegroup'   ,                 0.773  ,    8    ,     0.0967   ,  0.854 ,    0.555   , 0.009 ,   0.010   )
    check_variable(result['resources'][0]['data'][6],'ANOVA_gender:ANOVA_agegroup'           ,                         0.226  ,    4    ,     0.0564 ,    0.499 ,    0.737  ,  0.003    ,0.003   )
    check_variable(result['resources'][0]['data'][7],'ANOVA_alzheimerbroadcategory:ANOVA_gender:ANOVA_agegroup'    ,         0.857   ,   8    ,     0.1072 ,    0.947  ,   0.477  ,  0.010,    0.011   )
    check_variable(result['resources'][0]['data'][8],'residuals',  77.876  ,  688    ,     0.1132      )



def test_ANOVA_17():
    logging.info("---------- TEST : ANOVA - Tests With similar to our target data set(dataset_0.csv) - with full interaction and 3 variables - type II")

    data = [
        {   "name": "iterations_max_number", "value": "20" },
        {   "name": "iterations_condition_query_provided", "value": "true" },
        {   "name": "x", "value": "ANOVA_alzheimerbroadcategory*ANOVA_gender*ANOVA_agegroup" },
        {   "name": "y", "value": "ANOVA_lefthippocampus" },
        {   "name": "type", "value": "2" },
        {   "name": "dataset", "value": "ANOVA_dataset1,ANOVA_dataset2,ANOVA_dataset3" },
        {   "name": "filter", "value": "" },
        {   "name": "outputformat", "value": "pfa" }
    ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (r.text)


##  ANOVA
##  ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
##                                              Sum of Squares    df     Mean Square    F          p         η²       η²p
##  ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
##    alzheimerbroadcategory                             9.403      2         4.7016     41.537    < .001    0.090    0.108
##    gender                                            12.293      1        12.2928    108.602    < .001    0.117    0.136
##    agegroup                                           2.112      4         0.5279      4.664     0.001    0.020    0.026
##    alzheimerbroadcategory:gender                      1.210      2         0.6048      5.343     0.005    0.012    0.015
##    alzheimerbroadcategory:agegroup                    0.707      8         0.0883      0.781     0.620    0.007    0.009
##    gender:agegroup                                    0.462      4         0.1155      1.020     0.396    0.004    0.006
##    alzheimerbroadcategory:gender:agegroup             0.857      8         0.1072      0.947     0.477    0.008    0.011
##    Residuals                                         77.876    688         0.1132
##  ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────


    check_variable(result['resources'][0]['data'][1],'ANOVA_alzheimerbroadcategory', 9.403,   2, 4.7016 , 41.537 ,   '< .001' ,   0.090 ,   0.108        )
    check_variable(result['resources'][0]['data'][2],'ANOVA_gender',  12.293,   1  , 12.2928 , 108.602 ,  '< .001'  ,  0.117  ,  0.136       )
    check_variable(result['resources'][0]['data'][3],'ANOVA_agegroup', 2.112,    4  , 0.5279  , 4.664 , 0.001, 0.020 ,   0.026      )
    check_variable(result['resources'][0]['data'][4],'ANOVA_alzheimerbroadcategory:ANOVA_gender', 1.210, 2, 0.6048 , 5.343  ,   0.005 ,   0.012 ,   0.015  )
    check_variable(result['resources'][0]['data'][5],'ANOVA_alzheimerbroadcategory:ANOVA_agegroup', 0.707,  8 , 0.0883 , 0.781  , 0.620 ,   0.007  ,  0.009  )
    check_variable(result['resources'][0]['data'][6],'ANOVA_gender:ANOVA_agegroup', 0.462, 4, 0.1155,   1.020,  0.396  ,  0.004  ,  0.006 )
    check_variable(result['resources'][0]['data'][7],'ANOVA_alzheimerbroadcategory:ANOVA_gender:ANOVA_agegroup' ,  0.857, 8, 0.1072, 0.947, 0.477 , 0.008  ,  0.011    )
    check_variable(result['resources'][0]['data'][8],'residuals', 77.876  ,  688  ,   0.1132     )




def check_variable(variable_data,corr_variable,corr_sumOfSquares,corr_Df,corr_meanSquare,corr_f = None,corr_p = None,corr_etaSquared = None,corr_partEtaSquared = None):
    variable = variable_data[0]
    sumOfSquares = float(variable_data[1])
    Df = int(variable_data[2])
    meanSquare = float(variable_data[3])
    if corr_variable != 'residuals':
        f = float(variable_data[4])
        p = float(variable_data[5])
        etaSquared = float(variable_data[6])
        partEtaSquared = float(variable_data[7])
        # omegaSquared = float(variable_data[8])
    assert (variable == corr_variable)

    assert (math.isclose(sumOfSquares,corr_sumOfSquares,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_sumOfSquares)).as_tuple().exponent))))
    assert (Df == corr_Df)
    assert (math.isclose(meanSquare,corr_meanSquare,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_meanSquare)).as_tuple().exponent))))
    if corr_variable != 'residuals':
        assert (math.isclose(f,corr_f,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_f)).as_tuple().exponent))))
        if type(corr_p) is str:
            assert (p <= float(corr_p.replace('< ','0')))
        else:
            assert (math.isclose(p,corr_p,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_p)).as_tuple().exponent))))
        assert (math.isclose(etaSquared,corr_etaSquared,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_etaSquared)).as_tuple().exponent))))
        assert (math.isclose(partEtaSquared,corr_partEtaSquared,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_partEtaSquared)).as_tuple().exponent))))
        #assert math.isclose(omegaSquared,corr_omegaSquared,rel_tol=0,abs_tol=1e-06)
