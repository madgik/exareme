import requests
import json
import logging
import math

# Required datasets: adni

endpointUrl='http://88.197.53.100:9090'


def test_LinearRegression_ADNI_1():
    logging.info("---------- TEST : Linear Regression ADNI 1")

    data = [
          {
            "name": "x",
            "value": "adnicategory*apoe4+subjectage+minimentalstate+gender"
          },
          {
            "name": "y",
            "value": "av45"
          },
          {
            "name": "dataset",
            "value": "adni"
          },
          {
            "name": "filter",
            "value": ""
          }
        ]
    
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl+'/mining/query/LINEAR_REGRESSION',data=json.dumps(data),headers=headers)
    
    result = json.loads(r.text)
    
    """
    Results from R  
    (Intercept) 1.343887 0.179199 7.499 3.48e-13 ***    
    adnicategoryCN -0.107431 0.053384 -2.012 0.044775 *  
    adnicategoryMCI -0.065249 0.049041 -1.330 0.184032 
    apoe4 0.105269 0.029021 3.627 0.000319 ***    
    subjectage 0.004397 0.001302 3.376 0.000801 ***    
    minimentalstate -0.015687 0.005561 -2.821 0.005002 **   
    genderM -0.035283 0.018599 -1.897 0.058472 .   
    adnicategoryCN:apoe4 -0.011513 0.043731 -0.263 0.792468   
    adnicategoryMCI:apoe4 0.030638 0.034206 0.896 0.370894 
"""

    check_variable(result['resources'][0]['data'][1],'(Intercept)', 1.343887, 0.179199, 7.499, 3.48e-13)
    check_variable(result['resources'][0]['data'][2],'adnicategory(CN)', -0.107431, 0.053384, -2.012, 0.044775)
    check_variable(result['resources'][0]['data'][3],'adnicategory(CN):apoe4', -0.011513, 0.043731, -0.263, 0.792468)
    check_variable(result['resources'][0]['data'][4],'adnicategory(MCI)', -0.065249, 0.049041, -1.330, 0.184032)
    check_variable(result['resources'][0]['data'][5],'adnicategory(MCI):apoe4', 0.030638, 0.034206, 0.896, 0.370894)
    check_variable(result['resources'][0]['data'][6],'apoe4', 0.105269, 0.029021, 3.627, 0.000319)
    check_variable(result['resources'][0]['data'][7],'gender(M)', -0.035283, 0.018599, -1.897, 0.058472)
    check_variable(result['resources'][0]['data'][8],'minimentalstate', -0.015687, 0.005561, -2.821, 0.005002)
    check_variable(result['resources'][0]['data'][9],'subjectage', 0.004397, 0.001302, 3.376, 0.000801)


def check_variable(variable_data,corr_variable,corr_estimate,corr_st_error,corr_t_value,corr_p_value):
    variable = variable_data[0]
    estimate = float(variable_data[1])
    st_error = float(variable_data[2])
    t_value = float(variable_data[3])
    p_value = float(variable_data[4])
    assert variable == corr_variable
    assert math.isclose(estimate,corr_estimate,rel_tol=0,abs_tol=1e-06)
    assert math.isclose(st_error,corr_st_error,rel_tol=0,abs_tol=1e-06)
    assert math.isclose(t_value,corr_t_value,rel_tol=0,abs_tol=1e-03)
    assert math.isclose(p_value,corr_p_value,rel_tol=1e-02,abs_tol=0)
    
    
    
