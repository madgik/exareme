import requests
import os
import json
import logging

endpointUrl='http://88.197.53.100:9090/mining/query/ANOVA'

def test_ANOVA_Privacy():
        logging.info("---------- TEST : Algorithms for User Error")
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
    assert result == "{\"result\" : [{\"data\":\"The value(s) of the parameter 'sstype' should be less than 3.0 .\",\"type\":\"text/plain+error\"}]}"
