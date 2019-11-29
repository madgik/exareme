import requests
import os
import json
import logging

endpointUrl='http://88.197.53.23:9090/mining/query/LINEAR_REGRESSION'

def test_LINEAR_REGRESSION():
    logging.info("---------- TEST : Algorithms for Privacy Error")
    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "simplecoding"},
            { "name": "pathology","value":"dementia"},
            { "name": "dataset", "value": "demo1"},
            { "name": "filter", "value": ""}]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    check_result(r.text)



def check_result(result):
    assert result == "{\"result\" : [{\"data\":\"Dataset(s) demo1 not found for pathology dementia!\",\"type\":\"text/plain+user_error\"}]}"
if __name__ == '__main__':
    unittest.main()
