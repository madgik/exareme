import requests
import json
import logging
import math
from decimal import *


endpointUrl='http://88.197.53.38:9090/mining/query/LINEAR_REGRESSION'

def test_LinearRegression_1_1_dummycoding():
    logging.info("---------- TEST 1.1: Linear Regression, one categorical regressor,dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory"},
		    { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"}]"},
	        { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
			{"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory, data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.52364 -0.26261  0.01177  0.20978  1.32307
    ##
    ## Coefficients:
    ##                             Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  2.82834    0.02231 126.774  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.30049    0.03091   9.722  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.11394    0.03784   3.011  0.00269 **
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3693 on 715 degrees of freedom
    ## Multiple R-squared:  0.1184, Adjusted R-squared:  0.116
    ## F-statistic: 48.03 on 2 and 715 DF,  p-value: < 2.2e-16
    """

    check_variables(result['resources'][0]['data'],[['intercept', 2.82834, 0.02231, 126.774, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.30049 , 0.03091, 9.722 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.11394, 0.03784, 3.011, 0.00269]],
                                                    -1.52364, 1.32307,
                                                     0.3693, 715,
                                                     0.1184,  0.116,
                                                     48.03 , 2 , 715 )


def test_LinearRegression_1_1_simplecoding():
    logging.info("---------- TEST 1.1: Linear Regression, one categorical regressor,simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory"},
		    { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"}]"},
	        { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
			{"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory, data = .,
    ##     contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR11$alzheimerbroadcategory)))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.52364 -0.26261  0.01177  0.20978  1.32307
    ##
    ## Coefficients:
    ##                             Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  2.96648    0.01449 204.734  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.30049    0.03091   9.722  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.11394    0.03784   3.011  0.00269 **
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3693 on 715 degrees of freedom
    ## Multiple R-squared:  0.1184, Adjusted R-squared:  0.116
    ## F-statistic: 48.03 on 2 and 715 DF,  p-value: < 2.2e-16
    """

    check_variables(result['resources'][0]['data'],[['intercept', 2.96648,  0.01449, 204.734 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.30049 , 0.03091, 9.722 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.11394, 0.03784, 3.011, 0.00269]],
                                                    -1.52364, 1.32307,
                                                     0.3693, 715,
                                                     0.1184,  0.116,
                                                     48.03 , 2 , 715 )


def test_LinearRegression_1_2_dummycoding():

    logging.info("---------- TEST 1.2: Linear Regression, two categorical regressors without interaction,dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + gender,
    ##     data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.40751 -0.22342  0.00726  0.21034  1.17756
    ##
    ## Coefficients:
    ##                             Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  2.99378    0.02558 117.051   <2e-16 ***
    ## alzheimerbroadcategoryCN     0.28055    0.02867   9.785   <2e-16 ***
    ## alzheimerbroadcategoryOther  0.08928    0.03510   2.543   0.0112 *
    ## genderF                     -0.28157    0.02567 -10.967   <2e-16 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3419 on 714 degrees of freedom
    ## Multiple R-squared:  0.2455, Adjusted R-squared:  0.2424
    ## F-statistic: 77.45 on 3 and 714 DF,  p-value: < 2.2e-16
    """

    check_variables(result['resources'][0]['data'],[['intercept', 2.99378,  0.02558, 117.051,'< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.28055,    0.02867,   9.785, '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.08928,    0.03510,   2.543,   0.0112],
                                                    ['gender(F)',  -0.28157,  0.02567, -10.967,   '<2e-16']],
                                                    -1.40751, 1.17756,
                                                     0.3419, 714,
                                                     0.2455,  0.2424,
                                                     77.45, 3, 714 )


def test_LinearRegression_1_2_simplecoding():

    logging.info("---------- TEST 1.2: Linear Regression, two categorical regressors without interaction,simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + gender,
    ##     data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR12$alzheimerbroadcategory),
    ##         gender = simpleCodingContrast(df_LR12$gender)))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.40751 -0.22342  0.00726  0.21034  1.17756
    ##
    ## Coefficients:
    ##                             Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  2.97628    0.01344 221.392   <2e-16 ***
    ## alzheimerbroadcategoryCN     0.28055    0.02867   9.785   <2e-16 ***
    ## alzheimerbroadcategoryOther  0.08928    0.03510   2.543   0.0112 *
    ## genderF                     -0.28157    0.02567 -10.967   <2e-16 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3419 on 714 degrees of freedom
    ## Multiple R-squared:  0.2455, Adjusted R-squared:  0.2424
    ## F-statistic: 77.45 on 3 and 714 DF,  p-value: < 2.2e-16
    """

    check_variables(result['resources'][0]['data'],[['intercept', 2.97628,  0.01344, 221.392 ,  '<2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.28055,    0.02867,   9.785, '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.08928,    0.03510,   2.543,   0.0112],
                                                    ['gender(F)',  -0.28157,  0.02567, -10.967,   '<2e-16']],
                                                    -1.40751, 1.17756,
                                                     0.3419, 714,
                                                     0.2455,  0.2424,
                                                     77.45, 3, 714 )


def test_LinearRegression_1_2b_dummycoding():
    logging.info("---------- TEST 1.2b: Linear Regression, two categorical regressors with interaction,dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * gender,
    ##     data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.36036 -0.21369 -0.00888  0.21346  1.21941
    ##
    ## Coefficients:
    ##                                       Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                          3.0609673  0.0319307  95.863  < 2e-16
    ## alzheimerbroadcategoryCN             0.1715202  0.0426573   4.021 6.42e-05
    ## alzheimerbroadcategoryOther          0.0006588  0.0509686   0.013 0.989691
    ## genderF                             -0.3959083  0.0416553  -9.504  < 2e-16
    ## alzheimerbroadcategoryCN:genderF     0.1953208  0.0573006   3.409 0.000689
    ## alzheimerbroadcategoryOther:genderF  0.1572110  0.0699402   2.248 0.024895
    ##
    ## (Intercept)                         ***
    ## alzheimerbroadcategoryCN            ***
    ## alzheimerbroadcategoryOther
    ## genderF                             ***
    ## alzheimerbroadcategoryCN:genderF    ***
    ## alzheimerbroadcategoryOther:genderF *
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3394 on 712 degrees of freedom
    ## Multiple R-squared:  0.2584, Adjusted R-squared:  0.2532
    ## F-statistic: 49.62 on 5 and 712 DF,  p-value: < 2.2e-16
    """

    check_variables(result['resources'][0]['data'],[['intercept' , 3.0609673, 0.0319307,  95.863 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)' , 0.1715202,  0.0426573 ,  4.021, 6.42e-05],
                                                    ['alzheimerbroadcategory(Other)', 0.0006588 , 0.0509686 ,  0.013, 0.989691],
                                                    ['gender(F)' , -0.3959083,  0.0416553,  -9.504,  '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN):gender(F)', 0.1953208, 0.0573006, 3.409, 0.000689],
                                                    ['alzheimerbroadcategory(Other):gender(F)', 0.1572110, 0.0699402, 2.248, 0.024895]],
                                                    -1.36036, 1.21941,
                                                     0.3394, 712,
                                                     0.2584,  0.2532,
                                                     49.62, 5, 712 )

def test_LinearRegression_1_2b_simplecoding():
    logging.info("---------- TEST 1.2b: Linear Regression, two categorical regressors with interaction,simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * gender,
    ##     data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR12$alzheimerbroadcategory),
    ##         gender = simpleCodingContrast(df_LR12$gender)))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.36036 -0.21369 -0.00888  0.21346  1.21941
    ##
    ## Coefficients:
    ##                                     Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                          2.97916    0.01337 222.745  < 2e-16
    ## alzheimerbroadcategoryCN             0.26918    0.02865   9.395  < 2e-16
    ## alzheimerbroadcategoryOther          0.07926    0.03497   2.267 0.023712
    ## genderF                             -0.27840    0.02675 -10.408  < 2e-16
    ## alzheimerbroadcategoryCN:genderF     0.19532    0.05730   3.409 0.000689
    ## alzheimerbroadcategoryOther:genderF  0.15721    0.06994   2.248 0.024895
    ##
    ## (Intercept)                         ***
    ## alzheimerbroadcategoryCN            ***
    ## alzheimerbroadcategoryOther         *
    ## genderF                             ***
    ## alzheimerbroadcategoryCN:genderF    ***
    ## alzheimerbroadcategoryOther:genderF *
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3394 on 712 degrees of freedom
    ## Multiple R-squared:  0.2584, Adjusted R-squared:  0.2532
    ## F-statistic: 49.62 on 5 and 712 DF,  p-value: < 2.2e-16
    """

    check_variables(result['resources'][0]['data'],[['intercept' ,  2.97916,  0.01337, 222.745 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)' ,  0.26918,    0.02865 ,  9.395,  '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.07926 ,   0.03497 ,  2.267, 0.023712],
                                                    ['gender(F)' , -0.27840 ,   0.02675, -10.408,  '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN):gender(F)',0.19532 ,   0.05730,   3.409, 0.000689],
                                                    ['alzheimerbroadcategory(Other):gender(F)', 0.15721,    0.06994 ,  2.248, 0.024895]],
                                                    -1.36036, 1.21941,
                                                     0.3394, 712,
                                                     0.2584,  0.2532,
                                                     49.62, 5, 712 )

def test_LinearRegression_1_3_dummycoding():
    logging.info("---------- TEST 1.3: Linear Regression, three categorical regressors without interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+agegroup"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + gender +
    ##     agegroup, data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.46845 -0.22630  0.00646  0.20722  1.15176
    ##
    ## Coefficients:
    ##                             Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  2.87843    0.04135  69.620  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.25596    0.02896   8.840  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.08630    0.03472   2.486 0.013157 *
    ## genderF                     -0.27103    0.02559 -10.591  < 2e-16 ***
    ## agegroup-50y                 0.21120    0.08704   2.427 0.015493 *
    ## agegroup50-59y               0.22365    0.06624   3.376 0.000775 ***
    ## agegroup60-69y               0.16575    0.04230   3.919 9.76e-05 ***
    ## agegroup70-79y               0.10580    0.04014   2.636 0.008578 **
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.338 on 710 degrees of freedom
    ## Multiple R-squared:  0.2669, Adjusted R-squared:  0.2596
    ## F-statistic: 36.92 on 7 and 710 DF,  p-value: < 2.2e-16
    """

    check_variables(result['resources'][0]['data'],[['intercept' ,   2.87843   , 0.04135 , 69.620 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)' ,  0.25596  ,  0.02896 ,  8.840,  '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.08630  ,  0.03472 ,  2.486, 0.013157],
                                                    ['gender(F)' ,  -0.27103 ,0.02559, -10.591, '< 2e-16'],
                                                    ['agegroup(-50y)' ,  0.21120 , 0.08704, 2.427, 0.015493],
                                                    ['agegroup(50-59y)' , 0.22365 , 0.06624, 3.376, 0.000775],
                                                    ['agegroup(60-69y)' , 0.16575, 0.04230 ,  3.919, 9.76e-05],
                                                    ['agegroup(70-79y)' , 0.10580 ,  0.04014 ,  2.636, 0.008578]],
                                                    -1.46845, 1.15176,
                                                     0.338, 710,
                                                     0.2669,  0.2596,
                                                     36.92, 7, 710 )



def test_LinearRegression_1_3_simplecoding():
    logging.info("---------- TEST 1.3: Linear Regression, three categorical regressors without interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+agegroup"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + gender +
    ##     agegroup, data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR13$alzheimerbroadcategory),
    ##     gender = simpleCodingContrast(relevel(df_LR13$gender, ref = "M")),
    ##     agegroup = simpleCodingContrast(df_LR13$agegroup)))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.46845 -0.22630  0.00646  0.20722  1.15176
    ##
    ## Coefficients:
    ##                             Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  2.99828    0.02178 137.664  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.25596    0.02896   8.840  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.08630    0.03472   2.486 0.013157 *
    ## genderF                     -0.27103    0.02559 -10.591  < 2e-16 ***
    ## agegroup-50y                 0.21120    0.08704   2.427 0.015493 *
    ## agegroup50-59y               0.22365    0.06624   3.376 0.000775 ***
    ## agegroup60-69y               0.16575    0.04230   3.919 9.76e-05 ***
    ## agegroup70-79y               0.10580    0.04014   2.636 0.008578 **
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.338 on 710 degrees of freedom
    ## Multiple R-squared:  0.2669, Adjusted R-squared:  0.2596
    ## F-statistic: 36.92 on 7 and 710 DF,  p-value: < 2.2e-16

    """

    check_variables(result['resources'][0]['data'],[['intercept' ,    2.99828 ,   0.02178 ,137.664, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)' ,  0.25596 ,   0.02896 ,  8.840 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.08630  ,  0.03472 ,  2.486, 0.013157],
                                                    ['gender(F)' ,  -0.27103 ,0.02559, -10.591, '< 2e-16'],
                                                    ['agegroup(-50y)' ,  0.21120 , 0.08704, 2.427, 0.015493],
                                                    ['agegroup(50-59y)' , 0.22365 , 0.06624, 3.376, 0.000775],
                                                    ['agegroup(60-69y)' , 0.16575, 0.04230 ,  3.919, 9.76e-05],
                                                    ['agegroup(70-79y)' , 0.10580 ,  0.04014 ,  2.636, 0.008578]],
                                                    -1.46845, 1.15176,
                                                     0.338, 710,
                                                     0.2669,  0.2596,
                                                     36.92, 7, 710 )


def test_LinearRegression_1_3b():
    logging.info("---------- TEST 1.3b: Linear Regression, three categorical regressors with all interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*agegroup"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * gender *
    ##     agegroup, data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.45766 -0.21746  0.00874  0.20397  1.13691
    ##
    ## Coefficients:
    ##                                                    Estimate Std. Error
    ## (Intercept)                                         3.06347    0.08687
    ## alzheimerbroadcategoryCN                            0.03273    0.21278
    ## alzheimerbroadcategoryOther                        -0.15429    0.13735
    ## genderF                                            -0.50666    0.10259
    ## agegroup-50y                                       -0.22007    0.21278
    ## agegroup50-59y                                      0.21523    0.16252
    ## agegroup60-69y                                      0.02367    0.10222
    ## agegroup70-79y                                     -0.03675    0.09905
    ## alzheimerbroadcategoryCN:genderF                    0.21558    0.23867
    ## alzheimerbroadcategoryOther:genderF                 0.32703    0.17005
    ## alzheimerbroadcategoryCN:agegroup-50y               0.41667    0.37363
    ## alzheimerbroadcategoryOther:agegroup-50y            0.70809    0.33644
    ## alzheimerbroadcategoryCN:agegroup50-59y            -0.08520    0.28339
    ## alzheimerbroadcategoryOther:agegroup50-59y          0.16199    0.25696
    ## alzheimerbroadcategoryCN:agegroup60-69y             0.19512    0.22390
    ## alzheimerbroadcategoryOther:agegroup60-69y          0.15647    0.16405
    ## alzheimerbroadcategoryCN:agegroup70-79y             0.11286    0.22152
    ## alzheimerbroadcategoryOther:agegroup70-79y          0.17048    0.15609
    ## genderF:agegroup-50y                                0.42703    0.27668
    ## genderF:agegroup50-59y                             -0.02898    0.21967
    ## genderF:agegroup60-69y                              0.18187    0.12945
    ## genderF:agegroup70-79y                              0.14426    0.11925
    ## alzheimerbroadcategoryCN:genderF:agegroup-50y      -0.32536    0.45594
    ## alzheimerbroadcategoryOther:genderF:agegroup-50y   -0.57114    0.43505
    ## alzheimerbroadcategoryCN:genderF:agegroup50-59y     0.16331    0.34684
    ## alzheimerbroadcategoryOther:genderF:agegroup50-59y -0.05580    0.38928
    ## alzheimerbroadcategoryCN:genderF:agegroup60-69y    -0.19515    0.25862
    ## alzheimerbroadcategoryOther:genderF:agegroup60-69y -0.23521    0.21374
    ## alzheimerbroadcategoryCN:genderF:agegroup70-79y     0.06629    0.25313
    ## alzheimerbroadcategoryOther:genderF:agegroup70-79y -0.19616    0.19875
    ##                                                    t value Pr(>|t|)
    ## (Intercept)                                         35.266  < 2e-16 ***
    ## alzheimerbroadcategoryCN                             0.154   0.8778
    ## alzheimerbroadcategoryOther                         -1.123   0.2617
    ## genderF                                             -4.939 9.88e-07 ***
    ## agegroup-50y                                        -1.034   0.3014
    ## agegroup50-59y                                       1.324   0.1858
    ## agegroup60-69y                                       0.232   0.8169
    ## agegroup70-79y                                      -0.371   0.7107
    ## alzheimerbroadcategoryCN:genderF                     0.903   0.3667
    ## alzheimerbroadcategoryOther:genderF                  1.923   0.0549 .
    ## alzheimerbroadcategoryCN:agegroup-50y                1.115   0.2652
    ## alzheimerbroadcategoryOther:agegroup-50y             2.105   0.0357 *
    ## alzheimerbroadcategoryCN:agegroup50-59y             -0.301   0.7638
    ## alzheimerbroadcategoryOther:agegroup50-59y           0.630   0.5286
    ## alzheimerbroadcategoryCN:agegroup60-69y              0.871   0.3838
    ## alzheimerbroadcategoryOther:agegroup60-69y           0.954   0.3405
    ## alzheimerbroadcategoryCN:agegroup70-79y              0.509   0.6106
    ## alzheimerbroadcategoryOther:agegroup70-79y           1.092   0.2751
    ## genderF:agegroup-50y                                 1.543   0.1232
    ## genderF:agegroup50-59y                              -0.132   0.8951
    ## genderF:agegroup60-69y                               1.405   0.1605
    ## genderF:agegroup70-79y                               1.210   0.2268
    ## alzheimerbroadcategoryCN:genderF:agegroup-50y       -0.714   0.4757
    ## alzheimerbroadcategoryOther:genderF:agegroup-50y    -1.313   0.1897
    ## alzheimerbroadcategoryCN:genderF:agegroup50-59y      0.471   0.6379
    ## alzheimerbroadcategoryOther:genderF:agegroup50-59y  -0.143   0.8861
    ## alzheimerbroadcategoryCN:genderF:agegroup60-69y     -0.755   0.4508
    ## alzheimerbroadcategoryOther:genderF:agegroup60-69y  -1.100   0.2715
    ## alzheimerbroadcategoryCN:genderF:agegroup70-79y      0.262   0.7935
    ## alzheimerbroadcategoryOther:genderF:agegroup70-79y  -0.987   0.3240
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3364 on 688 degrees of freedom
    ## Multiple R-squared:  0.296,  Adjusted R-squared:  0.2663
    ## F-statistic: 9.973 on 29 and 688 DF,  p-value: < 2.2e-16

    """

    check_variables(result['resources'][0]['data'],[
                                                ['intercept', 3.06347, 0.08687, 35.266, '< 2e-16'],
                                                ['alzheimerbroadcategory(CN)', 0.03273, 0.21278, 0.154, 0.8778],
                                                ['alzheimerbroadcategory(Other)', -0.15429, 0.13735, -1.123, 0.2617],
                                                ['gender(F)', -0.50666, 0.10259, -4.939, 9.88e-07],
                                                ['agegroup(-50y)', -0.22007, 0.21278, -1.034, 0.3014],
                                                ['agegroup(50-59y)', 0.21523, 0.16252, 1.324, 0.1858],
                                                ['agegroup(60-69y)', 0.02367, 0.10222, 0.232, 0.8169],
                                                ['agegroup(70-79y)', -0.03675, 0.09905, -0.371, 0.7107],
                                                ['alzheimerbroadcategory(CN):gender(F)', 0.21558, 0.23867, 0.903, 0.3667],
                                                ['alzheimerbroadcategory(Other):gender(F)', 0.32703, 0.17005, 1.923, 0.0549],
                                                ['alzheimerbroadcategory(CN):agegroup(-50y)', 0.41667, 0.37363, 1.115, 0.2652],
                                                ['alzheimerbroadcategory(Other):agegroup(-50y)', 0.70809, 0.33644, 2.105, 0.0357],
                                                ['alzheimerbroadcategory(CN):agegroup(50-59y)', -0.0852, 0.28339, -0.301, 0.7638],
                                                ['alzheimerbroadcategory(Other):agegroup(50-59y)', 0.16199, 0.25696, 0.63, 0.5286],
                                                ['alzheimerbroadcategory(CN):agegroup(60-69y)', 0.19512, 0.2239, 0.871, 0.3838],
                                                ['alzheimerbroadcategory(Other):agegroup(60-69y)', 0.15647, 0.16405, 0.954, 0.3405],
                                                ['alzheimerbroadcategory(CN):agegroup(70-79y)', 0.11286, 0.22152, 0.509, 0.6106],
                                                ['alzheimerbroadcategory(Other):agegroup(70-79y)', 0.17048, 0.15609, 1.092, 0.2751],
                                                ['gender(F):agegroup(-50y)', 0.42703, 0.27668, 1.543, 0.1232],
                                                ['gender(F):agegroup(50-59y)', -0.02898, 0.21967, -0.132, 0.8951],
                                                ['gender(F):agegroup(60-69y)', 0.18187, 0.12945, 1.405, 0.1605],
                                                ['gender(F):agegroup(70-79y)', 0.14426, 0.11925, 1.21, 0.2268],
                                                ['alzheimerbroadcategory(CN):gender(F):agegroup(-50y)', -0.32536, 0.45594, -0.714, 0.4757],
                                                ['alzheimerbroadcategory(Other):gender(F):agegroup(-50y)', -0.57114, 0.43505, -1.313, 0.1897],
                                                ['alzheimerbroadcategory(CN):gender(F):agegroup(50-59y)', 0.16331, 0.34684, 0.471, 0.6379],
                                                ['alzheimerbroadcategory(Other):gender(F):agegroup(50-59y)', -0.0558, 0.38928, -0.143, 0.8861],
                                                ['alzheimerbroadcategory(CN):gender(F):agegroup(60-69y)', -0.19515, 0.25862, -0.755, 0.4508],
                                                ['alzheimerbroadcategory(Other):gender(F):agegroup(60-69y)', -0.23521, 0.21374, -1.1, 0.2715],
                                                ['alzheimerbroadcategory(CN):gender(F):agegroup(70-79y)', 0.06629, 0.25313, 0.262, 0.7935],
                                                ['alzheimerbroadcategory(Other):gender(F):agegroup(70-79y)', -0.19616, 0.19875, -0.987, 0.324]],
                                                -1.45766 ,  1.13691 ,
                                                 0.3364 , 688 ,
                                                 0.296,    0.2663 ,
                                                 9.973, 29, 688 )


def test_LinearRegression_1_3b():
    logging.info("---------- TEST 1.3b: Linear Regression, three categorical regressors with all interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*agegroup"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    """
    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * gender *
    ##     agegroup, data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR13$alzheimerbroadcategory),
    ##     gender = simpleCodingContrast(relevel(df_LR13$gender, ref = "M")),
    ##     agegroup = simpleCodingContrast(df_LR13$agegroup)))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.45766 -0.21746  0.00874  0.20397  1.13691
    ##
    ## Coefficients:
    ##                                                     Estimate Std. Error
    ## (Intercept)                                         3.006351   0.023648
    ## alzheimerbroadcategoryCN                            0.239318   0.053689
    ## alzheimerbroadcategoryOther                         0.142799   0.058245
    ## genderF                                            -0.270896   0.047295
    ## agegroup-50y                                        0.218949   0.094370
    ## agegroup50-59y                                      0.244255   0.078808
    ## agegroup60-69y                                      0.160077   0.051590
    ## agegroup70-79y                                      0.108182   0.049820
    ## alzheimerbroadcategoryCN:genderF                    0.157400   0.107379
    ## alzheimerbroadcategoryOther:genderF                 0.115371   0.116490
    ## alzheimerbroadcategoryCN:agegroup-50y               0.253996   0.227972
    ## alzheimerbroadcategoryOther:agegroup-50y            0.422522   0.217527
    ## alzheimerbroadcategoryCN:agegroup50-59y            -0.003544   0.173418
    ## alzheimerbroadcategoryOther:agegroup50-59y          0.134093   0.194642
    ## alzheimerbroadcategoryCN:agegroup60-69y             0.097550   0.129312
    ## alzheimerbroadcategoryOther:agegroup60-69y          0.038861   0.106870
    ## alzheimerbroadcategoryCN:agegroup70-79y             0.146002   0.126565
    ## alzheimerbroadcategoryOther:agegroup70-79y          0.072402   0.099374
    ## genderF:agegroup-50y                                0.128200   0.188740
    ## genderF:agegroup50-59y                              0.006860   0.157615
    ## genderF:agegroup60-69y                              0.038418   0.103180
    ## genderF:agegroup70-79y                              0.100972   0.099640
    ## alzheimerbroadcategoryCN:genderF:agegroup-50y      -0.325356   0.455943
    ## alzheimerbroadcategoryOther:genderF:agegroup-50y   -0.571142   0.435055
    ## alzheimerbroadcategoryCN:genderF:agegroup50-59y     0.163307   0.346836
    ## alzheimerbroadcategoryOther:genderF:agegroup50-59y -0.055800   0.389284
    ## alzheimerbroadcategoryCN:genderF:agegroup60-69y    -0.195149   0.258625
    ## alzheimerbroadcategoryOther:genderF:agegroup60-69y -0.235215   0.213741
    ## alzheimerbroadcategoryCN:genderF:agegroup70-79y     0.066292   0.253130
    ## alzheimerbroadcategoryOther:genderF:agegroup70-79y -0.196157   0.198747
    ##                                                    t value Pr(>|t|)
    ## (Intercept)                                        127.131  < 2e-16 ***
    ## alzheimerbroadcategoryCN                             4.457 9.68e-06 ***
    ## alzheimerbroadcategoryOther                          2.452  0.01447 *
    ## genderF                                             -5.728 1.52e-08 ***
    ## agegroup-50y                                         2.320  0.02063 *
    ## agegroup50-59y                                       3.099  0.00202 **
    ## agegroup60-69y                                       3.103  0.00200 **
    ## agegroup70-79y                                       2.171  0.03024 *
    ## alzheimerbroadcategoryCN:genderF                     1.466  0.14315
    ## alzheimerbroadcategoryOther:genderF                  0.990  0.32233
    ## alzheimerbroadcategoryCN:agegroup-50y                1.114  0.26560
    ## alzheimerbroadcategoryOther:agegroup-50y             1.942  0.05250 .
    ## alzheimerbroadcategoryCN:agegroup50-59y             -0.020  0.98370
    ## alzheimerbroadcategoryOther:agegroup50-59y           0.689  0.49111
    ## alzheimerbroadcategoryCN:agegroup60-69y              0.754  0.45088
    ## alzheimerbroadcategoryOther:agegroup60-69y           0.364  0.71625
    ## alzheimerbroadcategoryCN:agegroup70-79y              1.154  0.24908
    ## alzheimerbroadcategoryOther:agegroup70-79y           0.729  0.46650
    ## genderF:agegroup-50y                                 0.679  0.49721
    ## genderF:agegroup50-59y                               0.044  0.96530
    ## genderF:agegroup60-69y                               0.372  0.70975
    ## genderF:agegroup70-79y                               1.013  0.31125
    ## alzheimerbroadcategoryCN:genderF:agegroup-50y       -0.714  0.47572
    ## alzheimerbroadcategoryOther:genderF:agegroup-50y    -1.313  0.18969
    ## alzheimerbroadcategoryCN:genderF:agegroup50-59y      0.471  0.63790
    ## alzheimerbroadcategoryOther:genderF:agegroup50-59y  -0.143  0.88606
    ## alzheimerbroadcategoryCN:genderF:agegroup60-69y     -0.755  0.45077
    ## alzheimerbroadcategoryOther:genderF:agegroup60-69y  -1.100  0.27151
    ## alzheimerbroadcategoryCN:genderF:agegroup70-79y      0.262  0.79349
    ## alzheimerbroadcategoryOther:genderF:agegroup70-79y  -0.987  0.32400
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3364 on 688 degrees of freedom
    ## Multiple R-squared:  0.296,  Adjusted R-squared:  0.2663
    ## F-statistic: 9.973 on 29 and 688 DF,  p-value: < 2.2e-16
    """

    check_variables(result['resources'][0]['data'],[
                                                ['intercept', 3.006351, 0.023648, 127.131,  '< 2e-16'],
                                                ['alzheimerbroadcategory(CN)', 0.239318, 0.053689, 4.457, 9.68e-06],
                                                ['alzheimerbroadcategory(Other)', 0.142799, 0.058245, 2.452, 0.01447],
                                                ['gender(F)', -0.270896, 0.047295, -5.728, 1.52e-08],
                                                ['agegroup(-50y)', 0.218949, 0.09437, 2.32, 0.02063],
                                                ['agegroup(50-59y)', 0.244255, 0.078808, 3.099, 0.00202],
                                                ['agegroup(60-69y)', 0.160077, 0.05159, 3.103, 0.002],
                                                ['agegroup(70-79y)', 0.108182, 0.04982, 2.171, 0.03024],
                                                ['alzheimerbroadcategory(CN):gender(F)', 0.1574, 0.107379, 1.466, 0.14315],
                                                ['alzheimerbroadcategory(Other):gender(F)', 0.115371, 0.11649, 0.99, 0.32233],
                                                ['alzheimerbroadcategory(CN):agegroup(-50y)', 0.253996, 0.227972, 1.114, 0.2656],
                                                ['alzheimerbroadcategory(Other):agegroup(-50y)', 0.422522, 0.217527, 1.942, 0.0525],
                                                ['alzheimerbroadcategory(CN):agegroup(50-59y)', -0.003544, 0.173418, -0.02, 0.9837],
                                                ['alzheimerbroadcategory(Other):agegroup(50-59y)', 0.134093, 0.194642, 0.689, 0.49111],
                                                ['alzheimerbroadcategory(CN):agegroup(60-69y)', 0.09755, 0.129312, 0.754, 0.45088],
                                                ['alzheimerbroadcategory(Other):agegroup(60-69y)', 0.038861, 0.10687, 0.364, 0.71625],
                                                ['alzheimerbroadcategory(CN):agegroup(70-79y)', 0.146002, 0.126565, 1.154, 0.24908],
                                                ['alzheimerbroadcategory(Other):agegroup(70-79y)', 0.072402, 0.099374, 0.729, 0.4665],
                                                ['gender(F):agegroup(-50y)', 0.1282, 0.18874, 0.679, 0.49721],
                                                ['gender(F):agegroup(50-59y)', 0.00686, 0.157615, 0.044, 0.9653],
                                                ['gender(F):agegroup(60-69y)', 0.038418, 0.10318, 0.372, 0.70975],
                                                ['gender(F):agegroup(70-79y)', 0.100972, 0.09964, 1.013, 0.31125],
                                                ['alzheimerbroadcategory(CN):gender(F):agegroup(-50y)', -0.325356, 0.455943, -0.714, 0.47572],
                                                ['alzheimerbroadcategory(Other):gender(F):agegroup(-50y)', -0.571142, 0.435055, -1.313, 0.18969],
                                                ['alzheimerbroadcategory(CN):gender(F):agegroup(50-59y)', 0.163307, 0.346836, 0.471, 0.6379],
                                                ['alzheimerbroadcategory(Other):gender(F):agegroup(50-59y)', -0.0558, 0.389284, -0.143, 0.88606],
                                                ['alzheimerbroadcategory(CN):gender(F):agegroup(60-69y)', -0.195149, 0.258625, -0.755, 0.45077],
                                                ['alzheimerbroadcategory(Other):gender(F):agegroup(60-69y)', -0.235215, 0.213741, -1.1, 0.27151],
                                                ['alzheimerbroadcategory(CN):gender(F):agegroup(70-79y)', 0.066292, 0.25313, 0.262, 0.79349],
                                                ['alzheimerbroadcategory(Other):gender(F):agegroup(70-79y)', -0.196157, 0.198747, -0.987, 0.324]],
                                                -1.45766, 1.13691,
                                                 0.3364, 688,
                                                 0.296,  0.2663,
                                                 9.973, 29, 688 )


def test_LinearRegression_2_1():

    logging.info("---------- TEST 2_1: Linear Regression, one continuous regressor")

    data = [{ "name": "x",	"value": "csfglobal"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ csfglobal, data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.69464 -0.22718  0.03736  0.24564  1.46764
    ##
    ## Coefficients:
    ##             Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)  2.96477    0.04400  67.374   <2e-16 ***
    ## csfglobal    0.01593    0.02619   0.608    0.543
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3876 on 918 degrees of freedom
    ## Multiple R-squared:  0.0004029,  Adjusted R-squared:  -0.000686
    ## F-statistic:  0.37 on 1 and 918 DF,  p-value: 0.5431


    check_variables(result['resources'][0]['data'],[['intercept',  2.96477 ,   0.04400 , 67.374 ,  '<2e-16'],
                                                    ['csfglobal', 0.01593 ,   0.02619 ,  0.608  ,  0.543]],
                                                    -1.69464,  1.46764,
                                                    0.3876, 918,
                                                    0.0004029,    -0.000686,
                                                    0.37, 1, 918 )

def test_LinearRegression_2_2():

    logging.info("---------- TEST 2_2: Linear Regression, two continuous regressors without interaction")

    data = [{ "name": "x",	"value": "opticchiasm+minimentalstate"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ opticchiasm + minimentalstate,
    ##     data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.62609 -0.22504  0.01432  0.21693  1.38595
    ##
    ## Coefficients:
    ##                 Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)     1.645543   0.129362   12.72  < 2e-16 ***
    ## opticchiasm     7.427069   1.476556    5.03 6.25e-07 ***
    ## minimentalstate 0.030139   0.002659   11.34  < 2e-16 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3564 on 695 degrees of freedom
    ## Multiple R-squared:  0.1903, Adjusted R-squared:  0.188
    ## F-statistic: 81.68 on 2 and 695 DF,  p-value: < 2.2e-16

    check_variables(result['resources'][0]['data'],[['intercept',   1.645543,   0.129362,   12.72,  '< 2e-16'],
                                                    ['opticchiasm',  7.427069,   1.476556,    5.03, 6.25e-07],
                                                    ['minimentalstate', 0.030139 ,  0.002659 ,  11.34,  '< 2e-16']],
                                                    -1.62609, 1.38595,
                                                    0.3564, 695,
                                                    0.1903, 0.188 ,
                                                    81.68, 2, 695 )

def test_LinearRegression_2_2b():

    logging.info("---------- TEST 2_2: Linear Regression, two continuous regressors with interaction")

    data = [{ "name": "x",	"value": "opticchiasm*minimentalstate"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ opticchiasm * minimentalstate,
    ##     data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.58391 -0.22081  0.01081  0.22116  1.38060
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                   3.49805    0.55712   6.279 6.02e-10 ***
    ## opticchiasm                 -16.54326    7.16625  -2.308  0.02126 *
    ## minimentalstate              -0.04449    0.02200  -2.022  0.04352 *
    ## opticchiasm:minimentalstate   0.96392    0.28209   3.417  0.00067 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3537 on 694 degrees of freedom
    ## Multiple R-squared:  0.2037, Adjusted R-squared:  0.2003
    ## F-statistic: 59.18 on 3 and 694 DF,  p-value: < 2.2e-16

    check_variables(result['resources'][0]['data'],[['intercept',   3.49805 ,   0.55712 ,  6.279 ,6.02e-10],
                                                    ['opticchiasm',  -16.54326  ,  7.16625  ,-2.308,  0.02126],
                                                    ['minimentalstate',-0.04449  ,  0.02200 ,-2.022 , 0.04352 ],
                                                    ['opticchiasm:minimentalstate',0.96392 ,   0.28209,   3.417,  0.00067]],
                                                    -1.58391,  1.38060 ,
                                                     0.3537 , 694,
                                                     0.2037,  0.2003 ,
                                                    59.18, 3, 694 )

def test_LinearRegression_2_3():
    logging.info("---------- TEST 2_3: Linear Regression, three continuous regressors without interaction")

    data = [{ "name": "x",	"value": "opticchiasm+minimentalstate+subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

##
## Call:
## lm(formula = lefthippocampus ~ opticchiasm + minimentalstate +
##     subjectage, data = .)
##
## Residuals:
##      Min       1Q   Median       3Q      Max
## -1.66457 -0.20086  0.01269  0.22049  1.31721
##
## Coefficients:
##                  Estimate Std. Error t value Pr(>|t|)
## (Intercept)      2.184586   0.178338  12.250  < 2e-16 ***
## opticchiasm      7.423747   1.458042   5.092 4.58e-07 ***
## minimentalstate  0.028308   0.002659  10.645  < 2e-16 ***
## subjectage      -0.006997   0.001615  -4.332 1.70e-05 ***
## ---
## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
##
## Residual standard error: 0.3519 on 694 degrees of freedom
## Multiple R-squared:  0.2116, Adjusted R-squared:  0.2082
## F-statistic:  62.1 on 3 and 694 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept',     2.184586 ,  0.178338,  12.250 , '< 2e-16'],
                                                    ['opticchiasm',   7.423747 ,  1.458042 ,  5.092 , 4.58e-07],
                                                    ['minimentalstate',0.028308,   0.002659,  10.645,  '< 2e-16' ],
                                                    ['subjectage',      -0.006997,   0.001615 , -4.332, 1.70e-05]],
                                                    -1.66457,   1.31721,
                                                     0.3519 , 694,
                                                     0.2116,  0.2082 ,
                                                     62.1, 3, 694 )




def test_LinearRegression_2_3b():
    logging.info("---------- TEST 2_3: Linear Regression, three continuous regressors with interaction")

    data = [{ "name": "x",	"value": "opticchiasm*minimentalstate*subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

        ##
    ## Call:
    ## lm(formula = lefthippocampus ~ opticchiasm * minimentalstate *
    ##     subjectage, data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.63052 -0.20648  0.01204  0.21954  1.31591
    ##
    ## Coefficients:
    ##                                          Estimate Std. Error t value
    ## (Intercept)                              6.937203   4.962343   1.398
    ## opticchiasm                            -45.511430  61.441696  -0.741
    ## minimentalstate                         -0.190837   0.193514  -0.986
    ## subjectage                              -0.046558   0.067219  -0.693
    ## opticchiasm:minimentalstate              2.474250   2.393638   1.034
    ## opticchiasm:subjectage                   0.393737   0.833829   0.472
    ## minimentalstate:subjectage               0.001995   0.002639   0.756
    ## opticchiasm:minimentalstate:subjectage  -0.020829   0.032724  -0.637
    ##                                        Pr(>|t|)
    ## (Intercept)                               0.163
    ## opticchiasm                               0.459
    ## minimentalstate                           0.324
    ## subjectage                                0.489
    ## opticchiasm:minimentalstate               0.302
    ## opticchiasm:subjectage                    0.637
    ## minimentalstate:subjectage                0.450
    ## opticchiasm:minimentalstate:subjectage    0.525
    ##
    ## Residual standard error: 0.3494 on 690 degrees of freedom
    ## Multiple R-squared:  0.2275, Adjusted R-squared:  0.2196
    ## F-statistic: 29.03 on 7 and 690 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 6.937203, 4.962343, 1.398, 0.163],
                                                    ['opticchiasm', -45.51143, 61.441696, -0.741, 0.459],
                                                    ['minimentalstate', -0.190837, 0.193514, -0.986, 0.324],
                                                    ['subjectage', -0.046558, 0.067219, -0.693, 0.489],
                                                    ['opticchiasm:minimentalstate', 2.47425, 2.393638, 1.034, 0.302],
                                                    ['opticchiasm:subjectage', 0.393737, 0.833829, 0.472, 0.637],
                                                    ['minimentalstate:subjectage', 0.001995, 0.002639, 0.756, 0.45],
                                                    ['opticchiasm:minimentalstate:subjectage', -0.020829, 0.032724, -0.637, 0.525]],
                                                        -1.63052,    1.31591,
                                                         0.3494 , 690,
                                                         0.2275,  0.2196 ,
                                                         29.03, 7, 690 )






def test_LinearRegression_3_1_dummycoding():
    logging.info("---------- TEST 3_1: Linear Regression, one categorical and one continuous regressors without interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + subjectage,
    ##     data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.59471 -0.23124  0.00655  0.21622  1.31007
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  3.352625   0.119861  27.971  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.278972   0.030892   9.031  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.110462   0.037360   2.957  0.00321 **
    ## subjectage                  -0.007310   0.001643  -4.450 9.96e-06 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3645 on 714 degrees of freedom
    ## Multiple R-squared:  0.1422, Adjusted R-squared:  0.1386
    ## F-statistic: 39.46 on 3 and 714 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 3.352625, 0.119861, 27.971, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.278972, 0.030892, 9.031, '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.110462, 0.03736, 2.957, 0.00321],
                                                    ['subjectage', -0.00731, 0.001643, -4.45, 0.00000996]],
                                                        -1.59471,  1.31007,
                                                        0.3645  , 714,
                                                         0.1422,   0.1386 ,
                                                         39.46, 3, 714 )

def test_LinearRegression_3_1():
    logging.info("---------- TEST 3_1: Linear Regression, one categorical and one continuous regressors without interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + subjectage,
    ##     data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR31$alzheimerbroadcategory)))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.59471 -0.23124  0.00655  0.21622  1.31007
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  3.482436   0.116826  29.809  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.278972   0.030892   9.031  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.110462   0.037360   2.957  0.00321 **
    ## subjectage                  -0.007310   0.001643  -4.450 9.96e-06 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3645 on 714 degrees of freedom
    ## Multiple R-squared:  0.1422, Adjusted R-squared:  0.1386
    ## F-statistic: 39.46 on 3 and 714 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 3.482436,   0.116826 , 29.809, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)',  0.278972  , 0.030892 ,  9.031,  '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)',  0.110462 ,  0.037360  , 2.957,  0.00321],
                                                    ['subjectage', -0.007310,   0.001643 , -4.450, 9.96e-06]],
                                                        -1.59471,  1.31007,
                                                         0.3645 , 714,
                                                         0.1422,   0.1386 ,
                                                         39.46, 3, 714 )


def test_LinearRegression_3_1b_dummycoding():
    logging.info("---------- TEST 3_1b: Linear Regression, one categorical and one continuous regressors with interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * subjectage,
    ##     data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.60299 -0.24061  0.01262  0.21443  1.31579
    ##
    ## Coefficients:
    ##                                         Estimate Std. Error t value
    ## (Intercept)                             3.413748   0.178705  19.103
    ## alzheimerbroadcategoryCN               -0.003409   0.268905  -0.013
    ## alzheimerbroadcategoryOther             0.243866   0.300298   0.812
    ## subjectage                             -0.008162   0.002473  -3.301
    ## alzheimerbroadcategoryCN:subjectage     0.004069   0.003815   1.067
    ## alzheimerbroadcategoryOther:subjectage -0.001878   0.004172  -0.450
    ##                                        Pr(>|t|)
    ## (Intercept)                             < 2e-16 ***
    ## alzheimerbroadcategoryCN                0.98989
    ## alzheimerbroadcategoryOther             0.41702
    ## subjectage                              0.00101 **
    ## alzheimerbroadcategoryCN:subjectage     0.28651
    ## alzheimerbroadcategoryOther:subjectage  0.65275
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3645 on 712 degrees of freedom
    ## Multiple R-squared:  0.1446, Adjusted R-squared:  0.1386
    ## F-statistic: 24.08 on 5 and 712 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 3.413748, 0.178705, 19.103, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', -0.003409, 0.268905, -0.013, 0.98989],
                                                    ['alzheimerbroadcategory(Other)', 0.243866, 0.300298, 0.812, 0.41702],
                                                    ['subjectage', -0.008162, 0.002473, -3.301, 0.00101],
                                                    ['alzheimerbroadcategory(CN):subjectage', 0.004069, 0.003815, 1.067, 0.28651],
                                                    ['alzheimerbroadcategory(Other):subjectage', -0.001878, 0.004172, -0.45, 0.65275]],
                                                    -1.60299, 1.31579,
                                                    0.3645  , 712,
                                                    0.1446,    0.1386 ,
                                                    24.08, 5, 712 )


def test_LinearRegression_3_1b_simplecoding():
    logging.info("---------- TEST 3_1b: Linear Regression, one categorical and one continuous regressors with interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * subjectage,
    ##     data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR31$alzheimerbroadcategory)))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.60299 -0.24061  0.01262  0.21443  1.31579
    ##
    ## Coefficients:
    ##                                         Estimate Std. Error t value
    ## (Intercept)                             3.493901   0.120440  29.009
    ## alzheimerbroadcategoryCN               -0.003409   0.268905  -0.013
    ## alzheimerbroadcategoryOther             0.243866   0.300298   0.812
    ## subjectage                             -0.007432   0.001695  -4.385
    ## alzheimerbroadcategoryCN:subjectage     0.004069   0.003815   1.067
    ## alzheimerbroadcategoryOther:subjectage -0.001878   0.004172  -0.450
    ##                                        Pr(>|t|)
    ## (Intercept)                             < 2e-16 ***
    ## alzheimerbroadcategoryCN                  0.990
    ## alzheimerbroadcategoryOther               0.417
    ## subjectage                             1.33e-05 ***
    ## alzheimerbroadcategoryCN:subjectage       0.287
    ## alzheimerbroadcategoryOther:subjectage    0.653
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3645 on 712 degrees of freedom
    ## Multiple R-squared:  0.1446, Adjusted R-squared:  0.1386
    ## F-statistic: 24.08 on 5 and 712 DF,  p-value: < 2.2e-16

    check_variables(result['resources'][0]['data'],[['intercept',  3.493901 ,  0.120440,  29.009, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)',   -0.003409,   0.268905 , -0.013, 0.990],
                                                    ['alzheimerbroadcategory(Other)',  0.243866 ,  0.300298,   0.812 , 0.417 ],
                                                    ['subjectage',  -0.007432,   0.001695 , -4.385, 1.33e-05],
                                                    ['alzheimerbroadcategory(CN):subjectage',0.004069 ,  0.003815,   1.067,  0.287],
                                                    ['alzheimerbroadcategory(Other):subjectage', -0.001878 ,  0.004172 , -0.450 , 0.653]],
                                                    -1.60299, 1.31579,
                                                    0.3645  , 712,
                                                    0.1446,    0.1386 ,
                                                    24.08, 5, 712 )

def test_LinearRegression_3_2_dummycoding():
    logging.info("---------- TEST 3_2: Linear Regression, one categorical and two continuous regressors without interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+subjectage+opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + subjectage +
    ##     opticchiasm, data = .)
    ##
    ## Residuals:
    ##     Min      1Q  Median      3Q     Max
    ## -1.6497 -0.2343  0.0071  0.2232  1.2904
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  2.702473   0.162956  16.584  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.275037   0.030229   9.098  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.100981   0.036587   2.760  0.00593 **
    ## subjectage                  -0.007268   0.001607  -4.522 7.17e-06 ***
    ## opticchiasm                  8.329169   1.449662   5.746 1.36e-08 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3566 on 713 degrees of freedom
    ## Multiple R-squared:  0.1802, Adjusted R-squared:  0.1756
    ## F-statistic: 39.18 on 4 and 713 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 2.702473, 0.162956, 16.584, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.275037, 0.030229, 9.098, '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.100981, 0.036587, 2.76, 0.00593],
                                                    ['subjectage', -0.007268, 0.001607, -4.522, 7.17e-06],
                                                    ['opticchiasm', 8.329169, 1.449662, 5.746,  1.36e-08]],
                                                    -1.6497 , 1.2904,
                                                     0.3566  , 713,
                                                    0.1802,   0.1756 ,
                                                    39.18, 4, 713 )

def test_LinearRegression_3_2_simplecoding():
    logging.info("---------- TEST 3_2: Linear Regression, one categorical and two continuous regressors without interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+subjectage+opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + subjectage +
    ##     opticchiasm, data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR31$alzheimerbroadcategory)))
    ##
    ## Residuals:
    ##     Min      1Q  Median      3Q     Max
    ## -1.6497 -0.2343  0.0071  0.2232  1.2904
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  2.827812   0.161381  17.523  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.275037   0.030229   9.098  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.100981   0.036587   2.760  0.00593 **
    ## subjectage                  -0.007268   0.001607  -4.522 7.17e-06 ***
    ## opticchiasm                  8.329169   1.449662   5.746 1.36e-08 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3566 on 713 degrees of freedom
    ## Multiple R-squared:  0.1802, Adjusted R-squared:  0.1756
    ## F-statistic: 39.18 on 4 and 713 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 2.827812 ,  0.161381,  17.523 , '< 2e-16' ],
                                                    ['alzheimerbroadcategory(CN)',  0.275037 ,  0.030229 ,  9.098,  '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)',  0.100981 ,  0.036587 ,  2.760,  0.00593],
                                                    ['subjectage',  -0.007268 ,  0.001607 , -4.522, 7.17e-06],
                                                    ['opticchiasm', 8.329169,   1.449662 ,  5.746, 1.36e-08]],
                                                    -1.6497 , 1.2904 ,
                                                    0.3566  , 713,
                                                    0.1802,   0.1756 ,
                                                    39.18, 4, 713 )


def test_LinearRegression_3_2b_dummycoding():
    logging.info("---------- TEST 3_2b: Linear Regression, one categorical and two continuous regressors with interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*subjectage*opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * subjectage *
    ##     opticchiasm, data = .)
    ##
    ## Residuals:
    ##     Min      1Q  Median      3Q     Max
    ## -1.6050 -0.2394  0.0026  0.2173  1.2876
    ##
    ## Coefficients:
    ##                                                     Estimate Std. Error
    ## (Intercept)                                          5.30663    1.77059
    ## alzheimerbroadcategoryCN                            -6.77061    2.61187
    ## alzheimerbroadcategoryOther                         -3.38758    2.37645
    ## subjectage                                          -0.03876    0.02435
    ## opticchiasm                                        -24.04930   22.45834
    ## alzheimerbroadcategoryCN:subjectage                  0.09386    0.03688
    ## alzheimerbroadcategoryOther:subjectage               0.03984    0.03319
    ## alzheimerbroadcategoryCN:opticchiasm                87.15080   33.51109
    ## alzheimerbroadcategoryOther:opticchiasm             45.99155   29.94342
    ## subjectage:opticchiasm                               0.38935    0.30873
    ## alzheimerbroadcategoryCN:subjectage:opticchiasm     -1.15766    0.47345
    ## alzheimerbroadcategoryOther:subjectage:opticchiasm  -0.52885    0.41809
    ##                                                    t value Pr(>|t|)
    ## (Intercept)                                          2.997  0.00282 **
    ## alzheimerbroadcategoryCN                            -2.592  0.00973 **
    ## alzheimerbroadcategoryOther                         -1.425  0.15446
    ## subjectage                                          -1.592  0.11193
    ## opticchiasm                                         -1.071  0.28461
    ## alzheimerbroadcategoryCN:subjectage                  2.545  0.01113 *
    ## alzheimerbroadcategoryOther:subjectage               1.200  0.23042
    ## alzheimerbroadcategoryCN:opticchiasm                 2.601  0.00950 **
    ## alzheimerbroadcategoryOther:opticchiasm              1.536  0.12500
    ## subjectage:opticchiasm                               1.261  0.20768
    ## alzheimerbroadcategoryCN:subjectage:opticchiasm     -2.445  0.01472 *
    ## alzheimerbroadcategoryOther:subjectage:opticchiasm  -1.265  0.20632
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3548 on 706 degrees of freedom
    ## Multiple R-squared:  0.1964, Adjusted R-squared:  0.1839
    ## F-statistic: 15.68 on 11 and 706 DF,  p-value: < 2.2e-16

    check_variables(result['resources'][0]['data'],[['intercept', 5.30663, 1.77059, 2.997, 0.00282],
                                                    ['alzheimerbroadcategory(CN)', -6.77061, 2.61187, -2.592, 0.00973],
                                                    ['alzheimerbroadcategory(Other)', -3.38758, 2.37645, -1.425, 0.15446],
                                                    ['subjectage', -0.03876, 0.02435, -1.592, 0.11193],
                                                    ['opticchiasm', -24.0493, 22.45834, -1.071, 0.28461],
                                                    ['alzheimerbroadcategory(CN):subjectage', 0.09386, 0.03688, 2.545, 0.01113],
                                                    ['alzheimerbroadcategory(Other):subjectage', 0.03984, 0.03319, 1.2, 0.23042],
                                                    ['alzheimerbroadcategory(CN):opticchiasm', 87.1508, 33.51109, 2.601, 0.0095],
                                                    ['alzheimerbroadcategory(Other):opticchiasm', 45.99155, 29.94342, 1.536, 0.125],
                                                    ['subjectage:opticchiasm', 0.38935, 0.30873, 1.261, 0.20768],
                                                    ['alzheimerbroadcategory(CN):subjectage:opticchiasm', -1.15766, 0.47345, -2.445, 0.01472],
                                                    ['alzheimerbroadcategory(Other):subjectage:opticchiasm', -0.52885, 0.41809, -1.265, 0.20632]],
                                                    -1.6050,  1.2876,
                                                    0.3548 , 706,
                                                     0.1964,   0.1839,
                                                    15.68, 11, 706 )

def test_LinearRegression_3_2b_simplecoding():
    logging.info("---------- TEST 3_2b: Linear Regression, one categorical and two continuous regressors with interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*subjectage*opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * subjectage *
    ##     opticchiasm, data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR31$alzheimerbroadcategory)))
    ##
    ## Residuals:
    ##     Min      1Q  Median      3Q     Max
    ## -1.6050 -0.2394  0.0026  0.2173  1.2876
    ##
    ## Coefficients:
    ##                                                     Estimate Std. Error
    ## (Intercept)                                         1.920570   1.018408
    ## alzheimerbroadcategoryCN                           -6.770608   2.611869
    ## alzheimerbroadcategoryOther                        -3.387583   2.376449
    ## subjectage                                          0.005808   0.014408
    ## opticchiasm                                        20.331485  12.975298
    ## alzheimerbroadcategoryCN:subjectage                 0.093859   0.036877
    ## alzheimerbroadcategoryOther:subjectage              0.039835   0.033187
    ## alzheimerbroadcategoryCN:opticchiasm               87.150801  33.511095
    ## alzheimerbroadcategoryOther:opticchiasm            45.991549  29.943424
    ## subjectage:opticchiasm                             -0.172816   0.183677
    ## alzheimerbroadcategoryCN:subjectage:opticchiasm    -1.157659   0.473448
    ## alzheimerbroadcategoryOther:subjectage:opticchiasm -0.528848   0.418089
    ##                                                    t value Pr(>|t|)
    ## (Intercept)                                          1.886  0.05972 .
    ## alzheimerbroadcategoryCN                            -2.592  0.00973 **
    ## alzheimerbroadcategoryOther                         -1.425  0.15446
    ## subjectage                                           0.403  0.68697
    ## opticchiasm                                          1.567  0.11758
    ## alzheimerbroadcategoryCN:subjectage                  2.545  0.01113 *
    ## alzheimerbroadcategoryOther:subjectage               1.200  0.23042
    ## alzheimerbroadcategoryCN:opticchiasm                 2.601  0.00950 **
    ## alzheimerbroadcategoryOther:opticchiasm              1.536  0.12500
    ## subjectage:opticchiasm                              -0.941  0.34709
    ## alzheimerbroadcategoryCN:subjectage:opticchiasm     -2.445  0.01472 *
    ## alzheimerbroadcategoryOther:subjectage:opticchiasm  -1.265  0.20632
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3548 on 706 degrees of freedom
    ## Multiple R-squared:  0.1964, Adjusted R-squared:  0.1839
    ## F-statistic: 15.68 on 11 and 706 DF,  p-value: < 2.2e-16

    check_variables(result['resources'][0]['data'],[['intercept', 1.92057, 1.018408, 1.886, 0.05972],
                                                    ['alzheimerbroadcategory(CN)', -6.770608, 2.611869, -2.592, 0.00973],
                                                    ['alzheimerbroadcategory(Other)', -3.387583, 2.376449, -1.425, 0.15446],
                                                    ['subjectage', 0.005808, 0.014408, 0.403, 0.68697],
                                                    ['opticchiasm', 20.331485, 12.975298, 1.567, 0.11758],
                                                    ['alzheimerbroadcategory(CN):subjectage', 0.093859, 0.036877, 2.545, 0.01113],
                                                    ['alzheimerbroadcategory(Other):subjectage', 0.039835, 0.033187, 1.2, 0.23042],
                                                    ['alzheimerbroadcategory(CN):opticchiasm', 87.150801, 33.511095, 2.601, 0.0095],
                                                    ['alzheimerbroadcategory(Other):opticchiasm', 45.991549, 29.943424, 1.536, 0.125],
                                                    ['subjectage:opticchiasm', -0.172816, 0.183677, -0.941, 0.34709],
                                                    ['alzheimerbroadcategory(CN):subjectage:opticchiasm', -1.157659, 0.473448, -2.445, 0.01472],
                                                    ['alzheimerbroadcategory(Other):subjectage:opticchiasm', -0.528848, 0.418089, -1.265, 0.20632]],
                                                    -1.6050 ,  1.2876,
                                                    0.3548, 706,
                                                     0.1964,   0.1839,
                                                    15.68, 11, 706 )

def test_LinearRegression_3_3_dummycoding():
    logging.info("---------- TEST 3_3: Linear Regression, two categorical and one continuous regressors without interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+subjectage"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "dummycoding"},
                {"name": "dataset", "value": "desd-synthdata"},
                {"name": "filter", "value": ""}
                ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + gender +
    ##     subjectage, data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.47206 -0.23003  0.00133  0.21378  1.16927
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  3.448440   0.111483  30.932  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.262168   0.028684   9.140  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.086758   0.034708   2.500   0.0127 *
    ## genderF                     -0.275638   0.025422 -10.843  < 2e-16 ***
    ## subjectage                  -0.006388   0.001525  -4.187 3.18e-05 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.338 on 713 degrees of freedom
    ## Multiple R-squared:  0.2636, Adjusted R-squared:  0.2595
    ## F-statistic: 63.82 on 4 and 713 DF,  p-value: < 2.2e-16

    check_variables(result['resources'][0]['data'],[['intercept', 3.44844, 0.111483, 30.932, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.262168, 0.028684, 9.14, '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.086758, 0.034708, 2.5, 0.0127],
                                                    ['gender(F)', -0.275638, 0.025422, -10.843, '< 2e-16'],
                                                    ['subjectage', -0.006388, 0.001525, -4.187, 3.18e-05]],
                                                    -1.47206 ,  1.16927 ,
                                                    0.338 , 713,
                                                     0.2636, 0.2595 ,
                                                    63.82 , 4, 713 )

def test_LinearRegression_3_3_simplecoding():
    logging.info("---------- TEST 3_3: Linear Regression, two categorical and one continuous regressors without interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + gender +
    ##     subjectage, data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR31$alzheimerbroadcategory),
    ##     gender = simpleCodingContrast(relevel(df_LR33$gender, ref = "M"))))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.47206 -0.23003  0.00133  0.21378  1.16927
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  3.426930   0.108440  31.602  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.262168   0.028684   9.140  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.086758   0.034708   2.500   0.0127 *
    ## genderF                     -0.275638   0.025422 -10.843  < 2e-16 ***
    ## subjectage                  -0.006388   0.001525  -4.187 3.18e-05 ***
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.338 on 713 degrees of freedom
    ## Multiple R-squared:  0.2636, Adjusted R-squared:  0.2595
    ## F-statistic: 63.82 on 4 and 713 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept',  3.426930 ,  0.108440 , 31.602 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)',  0.262168 ,  0.028684 ,  9.140 , '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.086758 ,  0.034708 ,  2.500 ,  0.0127],
                                                    ['gender(F)',  -0.275638   ,0.025422, -10.843, '< 2e-16'],
                                                    ['subjectage',  -0.006388   ,0.001525,  -4.187 ,3.18e-05]],
                                                    -1.47206 ,  1.16927 ,
                                                    0.338 , 713,
                                                     0.2636, 0.2595 ,
                                                    63.82 , 4, 713 )

def test_LinearRegression_3_3b_dummycoding():
    logging.info("---------- TEST 3_3b: Linear Regression,two categorical and one continuous regressors with interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * gender *
    ##     subjectage, data = .)
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.44523 -0.21370 -0.00421  0.21518  1.20267
    ##
    ## Coefficients:
    ##                                                 Estimate Std. Error
    ## (Intercept)                                     3.134865   0.259613
    ## alzheimerbroadcategoryCN                        0.654476   0.399613
    ## alzheimerbroadcategoryOther                     0.719213   0.416986
    ## genderF                                         0.103668   0.338214
    ## subjectage                                     -0.001052   0.003669
    ## alzheimerbroadcategoryCN:genderF               -0.682901   0.511187
    ## alzheimerbroadcategoryOther:genderF            -0.585768   0.560490
    ## alzheimerbroadcategoryCN:subjectage            -0.007009   0.005713
    ## alzheimerbroadcategoryOther:subjectage         -0.010154   0.005869
    ## genderF:subjectage                             -0.006828   0.004712
    ## alzheimerbroadcategoryCN:genderF:subjectage     0.012288   0.007271
    ## alzheimerbroadcategoryOther:genderF:subjectage  0.010386   0.007810
    ##                                                t value Pr(>|t|)
    ## (Intercept)                                     12.075   <2e-16 ***
    ## alzheimerbroadcategoryCN                         1.638   0.1019
    ## alzheimerbroadcategoryOther                      1.725   0.0850 .
    ## genderF                                          0.307   0.7593
    ## subjectage                                      -0.287   0.7744
    ## alzheimerbroadcategoryCN:genderF                -1.336   0.1820
    ## alzheimerbroadcategoryOther:genderF             -1.045   0.2963
    ## alzheimerbroadcategoryCN:subjectage             -1.227   0.2203
    ## alzheimerbroadcategoryOther:subjectage          -1.730   0.0840 .
    ## genderF:subjectage                              -1.449   0.1477
    ## alzheimerbroadcategoryCN:genderF:subjectage      1.690   0.0915 .
    ## alzheimerbroadcategoryOther:genderF:subjectage   1.330   0.1840
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.336 on 706 degrees of freedom
    ## Multiple R-squared:  0.2793, Adjusted R-squared:  0.268
    ## F-statistic: 24.87 on 11 and 706 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 3.134865, 0.259613, 12.075, '<2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.654476, 0.399613, 1.638, 0.1019],
                                                    ['alzheimerbroadcategory(Other)', 0.719213, 0.416986, 1.725, 0.085],
                                                    ['gender(F)', 0.103668, 0.338214, 0.307, 0.7593],
                                                    ['subjectage', -0.001052, 0.003669, -0.287, 0.7744],
                                                    ['alzheimerbroadcategory(CN):gender(F)', -0.682901, 0.511187, -1.336, 0.182],
                                                    ['alzheimerbroadcategory(Other):gender(F)', -0.585768, 0.56049, -1.045, 0.2963],
                                                    ['alzheimerbroadcategory(CN):subjectage', -0.007009, 0.005713, -1.227, 0.2203],
                                                    ['alzheimerbroadcategory(Other):subjectage', -0.010154, 0.005869, -1.73, 0.084],
                                                    ['gender(F):subjectage', -0.006828, 0.004712, -1.449, 0.1477],
                                                    ['alzheimerbroadcategory(CN):gender(F):subjectage', 0.012288, 0.007271, 1.69, 0.0915],
                                                    ['alzheimerbroadcategory(Other):gender(F):subjectage', 0.010386, 0.00781, 1.33, 0.184]],
                                                    -1.44523,  1.20267,
                                                     0.336 , 706 ,
                                                     0.2793,   0.268,
                                                    24.87 , 11 , 706 )



def test_LinearRegression_3_3b_simplecoding():
    logging.info("---------- TEST 3_3b: Linear Regression,two categorical and one continuous regressors with interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*subjectage"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory * gender *
    ##     subjectage, data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR31$alzheimerbroadcategory),
    ##     gender = simpleCodingContrast(relevel(df_LR33$gender, ref = "M"))))
    ##
    ## Residuals:
    ##      Min       1Q   Median       3Q      Max
    ## -1.44523 -0.21370 -0.00421  0.21518  1.20267
    ##
    ## Coefficients:
    ##                                                  Estimate Std. Error
    ## (Intercept)                                     3.4331498  0.1131706
    ## alzheimerbroadcategoryCN                        0.3130250  0.2555937
    ## alzheimerbroadcategoryOther                     0.4263286  0.2802449
    ## genderF                                        -0.3192223  0.2263412
    ## subjectage                                     -0.0064086  0.0015956
    ## alzheimerbroadcategoryCN:genderF               -0.6829014  0.5111874
    ## alzheimerbroadcategoryOther:genderF            -0.5857679  0.5604898
    ## alzheimerbroadcategoryCN:subjectage            -0.0008651  0.0036355
    ## alzheimerbroadcategoryOther:subjectage         -0.0049614  0.0039049
    ## genderF:subjectage                              0.0007297  0.0031913
    ## alzheimerbroadcategoryCN:genderF:subjectage     0.0122882  0.0072710
    ## alzheimerbroadcategoryOther:genderF:subjectage  0.0103861  0.0078098
    ##                                                t value Pr(>|t|)
    ## (Intercept)                                     30.336  < 2e-16 ***
    ## alzheimerbroadcategoryCN                         1.225   0.2211
    ## alzheimerbroadcategoryOther                      1.521   0.1286
    ## genderF                                         -1.410   0.1589
    ## subjectage                                      -4.016 6.54e-05 ***
    ## alzheimerbroadcategoryCN:genderF                -1.336   0.1820
    ## alzheimerbroadcategoryOther:genderF             -1.045   0.2963
    ## alzheimerbroadcategoryCN:subjectage             -0.238   0.8120
    ## alzheimerbroadcategoryOther:subjectage          -1.271   0.2043
    ## genderF:subjectage                               0.229   0.8192
    ## alzheimerbroadcategoryCN:genderF:subjectage      1.690   0.0915 .
    ## alzheimerbroadcategoryOther:genderF:subjectage   1.330   0.1840
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.336 on 706 degrees of freedom
    ## Multiple R-squared:  0.2793, Adjusted R-squared:  0.268
    ## F-statistic: 24.87 on 11 and 706 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 3.4331498, 0.1131706, 30.336, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.313025, 0.2555937, 1.225, 0.2211],
                                                    ['alzheimerbroadcategory(Other)', 0.4263286, 0.2802449, 1.521, 0.1286],
                                                    ['gender(F)', -0.3192223, 0.2263412, -1.41, 0.1589],
                                                    ['subjectage', -0.0064086, 0.0015956, -4.016,  6.54e-05],
                                                    ['alzheimerbroadcategory(CN):gender(F)', -0.6829014, 0.5111874, -1.336, 0.182],
                                                    ['alzheimerbroadcategory(Other):gender(F)', -0.5857679, 0.5604898, -1.045, 0.2963],
                                                    ['alzheimerbroadcategory(CN):subjectage', -0.0008651, 0.0036355, -0.238, 0.812],
                                                    ['alzheimerbroadcategory(Other):subjectage', -0.0049614, 0.0039049, -1.271, 0.2043],
                                                    ['gender(F):subjectage', 0.0007297, 0.0031913, 0.229, 0.8192],
                                                    ['alzheimerbroadcategory(CN):gender(F):subjectage', 0.0122882, 0.007271, 1.69, 0.0915],
                                                    ['alzheimerbroadcategory(Other):gender(F):subjectage', 0.0103861, 0.0078098, 1.33, 0.184]],
                                                    -1.44523 , 1.20267 ,
                                                     0.336 , 706 ,
                                                     0.2793,   0.268,
                                                    24.87 , 11 , 706 )

def test_LinearRegression_3_4_dummycoding():
    logging.info("---------- TEST 3_4: Linear Regression, two categorical and two continuous regressors without interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+brainstem+opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + gender +
    ##     brainstem + opticchiasm, data = .)
    ##
    ## Residuals:
    ##     Min      1Q  Median      3Q     Max
    ## -1.5051 -0.2091 -0.0116  0.1974  1.1121
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  1.704301   0.132787  12.835  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.237044   0.026534   8.934  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.069473   0.032191   2.158   0.0313 *
    ## genderF                     -0.155941   0.025874  -6.027 2.68e-09 ***
    ## brainstem                    0.067098   0.006038  11.112  < 2e-16 ***
    ## opticchiasm                  1.062202   1.358013   0.782   0.4344
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3131 on 712 degrees of freedom
    ## Multiple R-squared:  0.3691, Adjusted R-squared:  0.3647
    ## F-statistic: 83.32 on 5 and 712 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 1.704301, 0.132787, 12.835, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.237044, 0.026534, 8.934, '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.069473, 0.032191, 2.158, 0.0313],
                                                    ['gender(F)', -0.155941, 0.025874, -6.027, 2.68e-09],
                                                    ['brainstem', 0.067098, 0.006038, 11.112, '< 2e-16'],
                                                    ['opticchiasm', 1.062202, 1.358013, 0.782, 0.4344]],
                                                        -1.5051 ,  1.1121 ,
                                                         0.3131 , 712 ,
                                                         0.3691,  0.3647 ,
                                                        83.32 , 5,712  )

def test_LinearRegression_3_4_simplecoding():
    logging.info("---------- TEST 3_4: Linear Regression, two categorical and two continuous regressors without interaction, simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender+brainstem+opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
    ## Call:
    ## lm(formula = lefthippocampus ~ alzheimerbroadcategory + gender +
    ##     brainstem + opticchiasm, data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR31$alzheimerbroadcategory),
    ##     gender = simpleCodingContrast(relevel(df_LR33$gender, ref = "M"))))
    ##
    ## Residuals:
    ##     Min      1Q  Median      3Q     Max
    ## -1.5051 -0.2091 -0.0116  0.1974  1.1121
    ##
    ## Coefficients:
    ##                              Estimate Std. Error t value Pr(>|t|)
    ## (Intercept)                  1.728503   0.127729  13.533  < 2e-16 ***
    ## alzheimerbroadcategoryCN     0.237044   0.026534   8.934  < 2e-16 ***
    ## alzheimerbroadcategoryOther  0.069473   0.032191   2.158   0.0313 *
    ## genderF                     -0.155941   0.025874  -6.027 2.68e-09 ***
    ## brainstem                    0.067098   0.006038  11.112  < 2e-16 ***
    ## opticchiasm                  1.062202   1.358013   0.782   0.4344
    ## ---
    ## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
    ##
    ## Residual standard error: 0.3131 on 712 degrees of freedom
    ## Multiple R-squared:  0.3691, Adjusted R-squared:  0.3647
    ## F-statistic: 83.32 on 5 and 712 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 1.728503, 0.127729, 13.533, '< 2e-16'],
                                                    ['alzheimerbroadcategory(CN)', 0.237044, 0.026534, 8.934, '< 2e-16'],
                                                    ['alzheimerbroadcategory(Other)', 0.069473, 0.032191, 2.158, 0.0313],
                                                    ['gender(F)', -0.155941, 0.025874, -6.027, 2.68e-09],
                                                    ['brainstem', 0.067098, 0.006038, 11.112, '< 2e-16'],
                                                    ['opticchiasm', 1.062202, 1.358013, 0.782, 0.4344]],
                                                    -1.5051 ,  1.1121 ,
                                                         0.3131 , 712 ,
                                                         0.3691,  0.3647 ,
                                                        83.32 , 5,712  )




def test_LinearRegression_3_4b_dummycoding():
    logging.info("---------- TEST 3_4b: Linear Regression, two categorical and two continuous regressors with interaction, dummycoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)

    ##
## Call:
## lm(formula = lefthippocampus ~ alzheimerbroadcategory * gender *
##     brainstem * opticchiasm, data = .)
##
## Residuals:
##      Min       1Q   Median       3Q      Max
## -1.42916 -0.19485 -0.01508  0.20111  1.16310
##
## Coefficients:
##                                                             Estimate
## (Intercept)                                                 5.831790
## alzheimerbroadcategoryCN                                    0.105328
## alzheimerbroadcategoryOther                                -3.470934
## genderF                                                    -4.793523
## brainstem                                                  -0.145270
## opticchiasm                                               -55.500505
## alzheimerbroadcategoryCN:genderF                            3.695183
## alzheimerbroadcategoryOther:genderF                         5.026974
## alzheimerbroadcategoryCN:brainstem                         -0.009632
## alzheimerbroadcategoryOther:brainstem                       0.164770
## genderF:brainstem                                           0.256068
## alzheimerbroadcategoryCN:opticchiasm                        5.779844
## alzheimerbroadcategoryOther:opticchiasm                    49.458758
## genderF:opticchiasm                                        62.971851
## brainstem:opticchiasm                                       2.956526
## alzheimerbroadcategoryCN:genderF:brainstem                 -0.227822
## alzheimerbroadcategoryOther:genderF:brainstem              -0.298541
## alzheimerbroadcategoryCN:genderF:opticchiasm              -47.274934
## alzheimerbroadcategoryOther:genderF:opticchiasm           -69.833775
## alzheimerbroadcategoryCN:brainstem:opticchiasm             -0.175221
## alzheimerbroadcategoryOther:brainstem:opticchiasm          -2.387543
## genderF:brainstem:opticchiasm                              -3.545134
## alzheimerbroadcategoryCN:genderF:brainstem:opticchiasm      3.036620
## alzheimerbroadcategoryOther:genderF:brainstem:opticchiasm   4.227727
##                                                           Std. Error
## (Intercept)                                                 2.068664
## alzheimerbroadcategoryCN                                    3.399301
## alzheimerbroadcategoryOther                                 3.599137
## genderF                                                     2.927490
## brainstem                                                   0.112045
## opticchiasm                                                27.753583
## alzheimerbroadcategoryCN:genderF                            4.770427
## alzheimerbroadcategoryOther:genderF                         5.025664
## alzheimerbroadcategoryCN:brainstem                          0.183349
## alzheimerbroadcategoryOther:brainstem                       0.200276
## genderF:brainstem                                           0.171208
## alzheimerbroadcategoryCN:opticchiasm                       43.337093
## alzheimerbroadcategoryOther:opticchiasm                    45.110284
## genderF:opticchiasm                                        38.224458
## brainstem:opticchiasm                                       1.490667
## alzheimerbroadcategoryCN:genderF:brainstem                  0.273727
## alzheimerbroadcategoryOther:genderF:brainstem               0.290582
## alzheimerbroadcategoryCN:genderF:opticchiasm               61.439351
## alzheimerbroadcategoryOther:genderF:opticchiasm            63.606546
## alzheimerbroadcategoryCN:brainstem:opticchiasm              2.322847
## alzheimerbroadcategoryOther:brainstem:opticchiasm           2.479557
## genderF:brainstem:opticchiasm                               2.214171
## alzheimerbroadcategoryCN:genderF:brainstem:opticchiasm      3.505685
## alzheimerbroadcategoryOther:genderF:brainstem:opticchiasm   3.641870
##                                                           t value Pr(>|t|)
## (Intercept)                                                 2.819  0.00495
## alzheimerbroadcategoryCN                                    0.031  0.97529
## alzheimerbroadcategoryOther                                -0.964  0.33519
## genderF                                                    -1.637  0.10200
## brainstem                                                  -1.297  0.19522
## opticchiasm                                                -2.000  0.04592
## alzheimerbroadcategoryCN:genderF                            0.775  0.43884
## alzheimerbroadcategoryOther:genderF                         1.000  0.31753
## alzheimerbroadcategoryCN:brainstem                         -0.053  0.95812
## alzheimerbroadcategoryOther:brainstem                       0.823  0.41095
## genderF:brainstem                                           1.496  0.13520
## alzheimerbroadcategoryCN:opticchiasm                        0.133  0.89394
## alzheimerbroadcategoryOther:opticchiasm                     1.096  0.27329
## genderF:opticchiasm                                         1.647  0.09992
## brainstem:opticchiasm                                       1.983  0.04772
## alzheimerbroadcategoryCN:genderF:brainstem                 -0.832  0.40553
## alzheimerbroadcategoryOther:genderF:brainstem              -1.027  0.30459
## alzheimerbroadcategoryCN:genderF:opticchiasm               -0.769  0.44188
## alzheimerbroadcategoryOther:genderF:opticchiasm            -1.098  0.27263
## alzheimerbroadcategoryCN:brainstem:opticchiasm             -0.075  0.93989
## alzheimerbroadcategoryOther:brainstem:opticchiasm          -0.963  0.33594
## genderF:brainstem:opticchiasm                              -1.601  0.10981
## alzheimerbroadcategoryCN:genderF:brainstem:opticchiasm      0.866  0.38668
## alzheimerbroadcategoryOther:genderF:brainstem:opticchiasm   1.161  0.24610
##
## (Intercept)                                               **
## alzheimerbroadcategoryCN
## alzheimerbroadcategoryOther
## genderF
## brainstem
## opticchiasm                                               *
## alzheimerbroadcategoryCN:genderF
## alzheimerbroadcategoryOther:genderF
## alzheimerbroadcategoryCN:brainstem
## alzheimerbroadcategoryOther:brainstem
## genderF:brainstem
## alzheimerbroadcategoryCN:opticchiasm
## alzheimerbroadcategoryOther:opticchiasm
## genderF:opticchiasm                                       .
## brainstem:opticchiasm                                     *
## alzheimerbroadcategoryCN:genderF:brainstem
## alzheimerbroadcategoryOther:genderF:brainstem
## alzheimerbroadcategoryCN:genderF:opticchiasm
## alzheimerbroadcategoryOther:genderF:opticchiasm
## alzheimerbroadcategoryCN:brainstem:opticchiasm
## alzheimerbroadcategoryOther:brainstem:opticchiasm
## genderF:brainstem:opticchiasm
## alzheimerbroadcategoryCN:genderF:brainstem:opticchiasm
## alzheimerbroadcategoryOther:genderF:brainstem:opticchiasm
## ---
## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
##
## Residual standard error: 0.3104 on 694 degrees of freedom
## Multiple R-squared:  0.3954, Adjusted R-squared:  0.3753
## F-statistic: 19.73 on 23 and 694 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 5.83179, 2.068664, 2.819, 0.00495],
                                            ['alzheimerbroadcategory(CN)', 0.105328, 3.399301, 0.031, 0.97529],
                                            ['alzheimerbroadcategory(Other)', -3.470934, 3.599137, -0.964, 0.33519],
                                            ['gender(F)', -4.793523, 2.92749, -1.637, 0.102],
                                            ['brainstem', -0.14527, 0.112045, -1.297, 0.19522],
                                            ['opticchiasm', -55.500505, 27.753583, -2, 0.04592],
                                            ['alzheimerbroadcategory(CN):gender(F)', 3.695183, 4.770427, 0.775, 0.43884],
                                            ['alzheimerbroadcategory(Other):gender(F)', 5.026974, 5.025664, 1, 0.31753],
                                            ['alzheimerbroadcategory(CN):brainstem', -0.009632, 0.183349, -0.053, 0.95812],
                                            ['alzheimerbroadcategory(Other):brainstem', 0.16477, 0.200276, 0.823, 0.41095],
                                            ['gender(F):brainstem', 0.256068, 0.171208, 1.496, 0.1352],
                                            ['alzheimerbroadcategory(CN):opticchiasm', 5.779844, 43.337093, 0.133, 0.89394],
                                            ['alzheimerbroadcategory(Other):opticchiasm', 49.458758, 45.110284, 1.096, 0.27329],
                                            ['gender(F):opticchiasm', 62.971851, 38.224458, 1.647, 0.09992],
                                            ['brainstem:opticchiasm', 2.956526, 1.490667, 1.983, 0.04772],
                                            ['alzheimerbroadcategory(CN):gender(F):brainstem', -0.227822, 0.273727, -0.832, 0.40553],
                                            ['alzheimerbroadcategory(Other):gender(F):brainstem', -0.298541, 0.290582, -1.027, 0.30459],
                                            ['alzheimerbroadcategory(CN):gender(F):opticchiasm', -47.274934, 61.439351, -0.769, 0.44188],
                                            ['alzheimerbroadcategory(Other):gender(F):opticchiasm', -69.833775, 63.606546, -1.098, 0.27263],
                                            ['alzheimerbroadcategory(CN):brainstem:opticchiasm', -0.175221, 2.322847, -0.075, 0.93989],
                                            ['alzheimerbroadcategory(Other):brainstem:opticchiasm', -2.387543, 2.479557, -0.963, 0.33594],
                                            ['gender(F):brainstem:opticchiasm', -3.545134, 2.214171, -1.601, 0.10981],
                                            ['alzheimerbroadcategory(CN):gender(F):brainstem:opticchiasm', 3.03662, 3.505685, 0.866, 0.38668],
                                            ['alzheimerbroadcategory(Other):gender(F):brainstem:opticchiasm', 4.227727, 3.64187, 1.161, 0.2461]],
                                            -1.42916,  1.16310 ,
                                            0.3104 , 694,
                                            0.3954,  0.3753,
                                            19.73 , 23 , 694   )

def test_LinearRegression_3_4b_simplecoding():
    logging.info("---------- TEST 3_4b: Linear Regression, two categorical and two continuous regressors with interaction,simplecoding")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            {"name": "dataset", "value": "desd-synthdata"},
            {"name": "filter", "value": ""}
            ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    print (r.text)
    result = json.loads(r.text)


##
## Call:
## lm(formula = lefthippocampus ~ alzheimerbroadcategory * gender *
##     brainstem * opticchiasm, data = ., contrasts = list(alzheimerbroadcategory = simpleCodingContrast(df_LR31$alzheimerbroadcategory),
##     gender = simpleCodingContrast(relevel(df_LR33$gender, ref = "M"))))
##
## Residuals:
##      Min       1Q   Median       3Q      Max
## -1.42916 -0.19485 -0.01508  0.20111  1.16310
##
## Coefficients:
##                                                            Estimate
## (Intercept)                                                 3.76685
## alzheimerbroadcategoryCN                                    1.95292
## alzheimerbroadcategoryOther                                -0.95745
## genderF                                                    -1.88614
## brainstem                                                  -0.05325
## opticchiasm                                               -25.11983
## alzheimerbroadcategoryCN:genderF                            3.69518
## alzheimerbroadcategoryOther:genderF                         5.02697
## alzheimerbroadcategoryCN:brainstem                         -0.12354
## alzheimerbroadcategoryOther:brainstem                       0.01550
## genderF:brainstem                                           0.08061
## alzheimerbroadcategoryCN:opticchiasm                      -17.85762
## alzheimerbroadcategoryOther:opticchiasm                    14.54187
## genderF:opticchiasm                                        23.93561
## brainstem:opticchiasm                                       1.54043
## alzheimerbroadcategoryCN:genderF:brainstem                 -0.22782
## alzheimerbroadcategoryOther:genderF:brainstem              -0.29854
## alzheimerbroadcategoryCN:genderF:opticchiasm              -47.27493
## alzheimerbroadcategoryOther:genderF:opticchiasm           -69.83378
## alzheimerbroadcategoryCN:brainstem:opticchiasm              1.34309
## alzheimerbroadcategoryOther:brainstem:opticchiasm          -0.27368
## genderF:brainstem:opticchiasm                              -1.12369
## alzheimerbroadcategoryCN:genderF:brainstem:opticchiasm      3.03662
## alzheimerbroadcategoryOther:genderF:brainstem:opticchiasm   4.22773
##                                                           Std. Error
## (Intercept)                                                  1.04674
## alzheimerbroadcategoryCN                                     2.38521
## alzheimerbroadcategoryOther                                  2.51283
## genderF                                                      2.09348
## brainstem                                                    0.06010
## opticchiasm                                                 13.29106
## alzheimerbroadcategoryCN:genderF                             4.77043
## alzheimerbroadcategoryOther:genderF                          5.02566
## alzheimerbroadcategoryCN:brainstem                           0.13686
## alzheimerbroadcategoryOther:brainstem                        0.14529
## genderF:brainstem                                            0.12021
## alzheimerbroadcategoryCN:opticchiasm                        30.71968
## alzheimerbroadcategoryOther:opticchiasm                     31.80327
## genderF:opticchiasm                                         26.58211
## brainstem:opticchiasm                                        0.75738
## alzheimerbroadcategoryCN:genderF:brainstem                   0.27373
## alzheimerbroadcategoryOther:genderF:brainstem                0.29058
## alzheimerbroadcategoryCN:genderF:opticchiasm                61.43935
## alzheimerbroadcategoryOther:genderF:opticchiasm             63.60655
## alzheimerbroadcategoryCN:brainstem:opticchiasm               1.75284
## alzheimerbroadcategoryOther:brainstem:opticchiasm            1.82093
## genderF:brainstem:opticchiasm                                1.51476
## alzheimerbroadcategoryCN:genderF:brainstem:opticchiasm       3.50568
## alzheimerbroadcategoryOther:genderF:brainstem:opticchiasm    3.64187
##                                                           t value Pr(>|t|)
## (Intercept)                                                 3.599 0.000343
## alzheimerbroadcategoryCN                                    0.819 0.413204
## alzheimerbroadcategoryOther                                -0.381 0.703303
## genderF                                                    -0.901 0.367924
## brainstem                                                  -0.886 0.375938
## opticchiasm                                                -1.890 0.059177
## alzheimerbroadcategoryCN:genderF                            0.775 0.438839
## alzheimerbroadcategoryOther:genderF                         1.000 0.317533
## alzheimerbroadcategoryCN:brainstem                         -0.903 0.367010
## alzheimerbroadcategoryOther:brainstem                       0.107 0.915076
## genderF:brainstem                                           0.671 0.502691
## alzheimerbroadcategoryCN:opticchiasm                       -0.581 0.561221
## alzheimerbroadcategoryOther:opticchiasm                     0.457 0.647638
## genderF:opticchiasm                                         0.900 0.368198
## brainstem:opticchiasm                                       2.034 0.042342
## alzheimerbroadcategoryCN:genderF:brainstem                 -0.832 0.405528
## alzheimerbroadcategoryOther:genderF:brainstem              -1.027 0.304594
## alzheimerbroadcategoryCN:genderF:opticchiasm               -0.769 0.441884
## alzheimerbroadcategoryOther:genderF:opticchiasm            -1.098 0.272628
## alzheimerbroadcategoryCN:brainstem:opticchiasm              0.766 0.443797
## alzheimerbroadcategoryOther:brainstem:opticchiasm          -0.150 0.880575
## genderF:brainstem:opticchiasm                              -0.742 0.458445
## alzheimerbroadcategoryCN:genderF:brainstem:opticchiasm      0.866 0.386681
## alzheimerbroadcategoryOther:genderF:brainstem:opticchiasm   1.161 0.246095
##
## (Intercept)                                               ***
## alzheimerbroadcategoryCN
## alzheimerbroadcategoryOther
## genderF
## brainstem
## opticchiasm                                               .
## alzheimerbroadcategoryCN:genderF
## alzheimerbroadcategoryOther:genderF
## alzheimerbroadcategoryCN:brainstem
## alzheimerbroadcategoryOther:brainstem
## genderF:brainstem
## alzheimerbroadcategoryCN:opticchiasm
## alzheimerbroadcategoryOther:opticchiasm
## genderF:opticchiasm
## brainstem:opticchiasm                                     *
## alzheimerbroadcategoryCN:genderF:brainstem
## alzheimerbroadcategoryOther:genderF:brainstem
## alzheimerbroadcategoryCN:genderF:opticchiasm
## alzheimerbroadcategoryOther:genderF:opticchiasm
## alzheimerbroadcategoryCN:brainstem:opticchiasm
## alzheimerbroadcategoryOther:brainstem:opticchiasm
## genderF:brainstem:opticchiasm
## alzheimerbroadcategoryCN:genderF:brainstem:opticchiasm
## alzheimerbroadcategoryOther:genderF:brainstem:opticchiasm
## ---
## Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1
##
## Residual standard error: 0.3104 on 694 degrees of freedom
## Multiple R-squared:  0.3954, Adjusted R-squared:  0.3753
## F-statistic: 19.73 on 23 and 694 DF,  p-value: < 2.2e-16


    check_variables(result['resources'][0]['data'],[['intercept', 3.76685, 1.04674, 3.599, 0.000343],
                                                ['alzheimerbroadcategory(CN)', 1.95292, 2.38521, 0.819, 0.413204],
                                                ['alzheimerbroadcategory(Other)', -0.95745, 2.51283, -0.381, 0.703303],
                                                ['gender(F)', -1.88614, 2.09348, -0.901, 0.367924],
                                                ['brainstem', -0.05325, 0.0601, -0.886, 0.375938],
                                                ['opticchiasm', -25.11983, 13.29106, -1.89, 0.059177],
                                                ['alzheimerbroadcategory(CN):gender(F)', 3.69518, 4.77043, 0.775, 0.438839],
                                                ['alzheimerbroadcategory(Other):gender(F)', 5.02697, 5.02566, 1, 0.317533],
                                                ['alzheimerbroadcategory(CN):brainstem', -0.12354, 0.13686, -0.903, 0.36701],
                                                ['alzheimerbroadcategory(Other):brainstem', 0.0155, 0.14529, 0.107, 0.915076],
                                                ['gender(F):brainstem', 0.08061, 0.12021, 0.671, 0.502691],
                                                ['alzheimerbroadcategory(CN):opticchiasm', -17.85762, 30.71968, -0.581, 0.561221],
                                                ['alzheimerbroadcategory(Other):opticchiasm', 14.54187, 31.80327, 0.457, 0.647638],
                                                ['gender(F):opticchiasm', 23.93561, 26.58211, 0.9, 0.368198],
                                                ['brainstem:opticchiasm', 1.54043, 0.75738, 2.034, 0.042342],
                                                ['alzheimerbroadcategory(CN):gender(F):brainstem', -0.22782, 0.27373, -0.832, 0.405528],
                                                ['alzheimerbroadcategory(Other):gender(F):brainstem', -0.29854, 0.29058, -1.027, 0.304594],
                                                ['alzheimerbroadcategory(CN):gender(F):opticchiasm', -47.27493, 61.43935, -0.769, 0.441884],
                                                ['alzheimerbroadcategory(Other):gender(F):opticchiasm', -69.83378, 63.60655, -1.098, 0.272628],
                                                ['alzheimerbroadcategory(CN):brainstem:opticchiasm', 1.34309, 1.75284, 0.766, 0.443797],
                                                ['alzheimerbroadcategory(Other):brainstem:opticchiasm', -0.27368, 1.82093, -0.15, 0.880575],
                                                ['gender(F):brainstem:opticchiasm', -1.12369, 1.51476, -0.742, 0.458445],
                                                ['alzheimerbroadcategory(CN):gender(F):brainstem:opticchiasm', 3.03662, 3.50568, 0.866, 0.386681],
                                                ['alzheimerbroadcategory(Other):gender(F):brainstem:opticchiasm', 4.22773, 3.64187, 1.161, 0.246095]],
                                        -1.42916 ,  1.16310 ,
                                        0.3104 , 694,
                                        0.3954,  0.3753,
                                        19.73 , 23 , 694   )

def test_LinearRegression_Privacy():
    """
    
    """

    logging.info("---------- TEST : Algorithms for Privacy Error")

    data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "dummycoding"},
            {"name": "dataset", "value": "adni_9rows"},
            {"name": "filter", "value": ""}
           ]


    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl + '/mining/query/LINEAR_REGRESSION', data=json.dumps(data), headers=headers)

    result = json.loads(r.text)

    check_privacy_result(r.text)

def check_privacy_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"



def check_variables(variable_data,corr_coeff_data,
                        corr_residualsmin, corr_residualsmax, corr_residualstandarderror,corr_degreesoffreedom,
                        corr_rsquared, corr_adjustedR,
                        corr_fstatistic,  corr_noofvariables,corr_degreesoffreedom2):
    noofcoefficient = 0
    for c in variable_data:
        if c[0] == 'Model Coefficients':
            noofcoefficient = noofcoefficient + 1
            exist_corr_c = False
            for corr_c in corr_coeff_data:
                if c[1] == corr_c[0]:
                    exist_corr_c = True
                    assert math.isclose(float(c[2]),corr_c[1],rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_c[1])).as_tuple().exponent)))
                    assert math.isclose(float(c[3]),corr_c[2],rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_c[2])).as_tuple().exponent)))
                    assert math.isclose(float(c[4]),corr_c[3],rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_c[3])).as_tuple().exponent)))
                    if type(corr_c[4]) is str:
                        corr_c[4].replace(' ','')
                        assert (float(c[5]) < float(corr_c[4].replace('<','')))
                    else:
                        assert (math.isclose(float(c[5]),corr_c[4],rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_c[4])).as_tuple().exponent))))
            assert exist_corr_c==True
        elif c[0] == 'Residuals':
            assert math.isclose(float(c[6]),corr_residualsmin,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_residualsmin)).as_tuple().exponent)))
            assert math.isclose(float(c[7]),corr_residualsmax,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_residualsmax)).as_tuple().exponent)))
            assert math.isclose(float(c[8]),corr_residualstandarderror,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_residualstandarderror)).as_tuple().exponent)))
            assert int(c[9])==corr_degreesoffreedom
        elif c[0] =='R squared':
            assert math.isclose(float(c[10]),corr_rsquared,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_rsquared)).as_tuple().exponent)))
            assert math.isclose(float(c[11]),corr_adjustedR,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_adjustedR)).as_tuple().exponent)))
        elif c[0] == 'F-statistics':
            assert int(c[9])==corr_degreesoffreedom2
            assert math.isclose(float(c[12]),corr_fstatistic,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_fstatistic)).as_tuple().exponent)))
            assert math.isclose(float(c[13]),corr_noofvariables,rel_tol=0,abs_tol=10**(-abs(Decimal(str(corr_noofvariables)).as_tuple().exponent)))
        else:
            if c[0]!='tablename':
                assert False
    assert noofcoefficient== len (corr_coeff_data)
