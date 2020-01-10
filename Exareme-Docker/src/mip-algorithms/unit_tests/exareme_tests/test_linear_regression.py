import requests
import os
import json
import logging

endpointUrl='http://88.197.53.38:9090/mining/query/LINEAR_REGRESSION'

def test_LINEAR_REGRESSION_Privacy():
        logging.info("---------- TEST : Algorithms for User Error")
        data = [{ "name": "x",	"value": "alzheimerbroadcategory+gender"},
                { "name": "y",  "value": "lefthippocampus"},
                { "name": "referencevalues", "value": "[{\"name\":\"alzheimerbroadcategory\",\"val\":\"AD\"},{\"name\":\"gender\",\"val\":\"M\"}]"},
                { "name": "encodingparameter", "value": "lala"},
		{   "name": "pathology","value":"dementia"},
                {"name": "dataset", "value": "adni_9rows"},
                {"name": "filter", "value": ""}]
        headers = {'Content-type': 'application/json', "Accept": "text/plain"}
        r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
        result = json.loads(r.text)
        check_privacy_result(r.text)



def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The value 'lala' of the parameter 'encodingparameter' is not included in the valueEnumerations [dummycoding, sumscoding, simplecoding] .\",\"type\":\"text/plain+error\"}]}"
if __name__ == '__main__':
    unittest.main()
