import requests
import os
import json
import logging

endpointUrl1='http://88.197.53.100:9090/mining/query/LINEAR_REGRESSION'

def test_valueEnumerationsParameter():
    logging.info("---------- TEST : valueEnumerations throwing error.")
    data = [{ "name": "x",	"value": "alzheimerbroadcategory*gender*brainstem*opticchiasm"},
            { "name": "y",  "value": "lefthippocampus"},
            { "name": "referencevalues", "value": "[{\"name\":\"gender\",\"val\":\"M\"}]"},
            { "name": "encodingparameter", "value": "abcd"},
            { "name": "pathology","value":"dementia"},
            { "name": "dataset", "value": "desd-synthdata"},
            { "name": "filter", "value": ""}]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl1, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    assert r.text == "{\"result\" : [{\"data\":\"The value 'abcd' of the parameter 'encodingparameter' is not included in the valueEnumerations [dummycoding, sumscoding, simplecoding] .\",\"type\":\"text/plain+error\"}]}"

if __name__ == '__main__':
    unittest.main()
