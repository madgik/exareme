import requests
import os
import json
import logging

endpointUrl='http://88.197.53.23:9090/mining/query/ANOVA'

def test_Histogram_Privacy():
        logging.info("---------- TEST : Algorithms for Privacy Error")
        data = [{   "name": "iterations_max_number", "value": "20" },
                {   "name": "x", "value": "ANOVA_var_I1*ANOVA_var_I2*ANOVA_var_I3" },
                {   "name": "y", "value": "ANOVA_var_D" },
                {   "name": "sstype", "value": "5" },
                {   "name": "pathology","value":"dementia"},
                {   "name": "dataset", "value": "ANOVA_Balanced_with_inter_V1V2" },
                {   "name": "filter", "value": "" },
                {   "name": "outputformat", "value": "pfa" }]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
        result = json.loads(r.text)
        check_privacy_result(r.text)



def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\" Incorrect parameter value. sstype'value should be one of the following: 1,2,3 \",\"type\":\"text/plain+user_error\"}]}"
