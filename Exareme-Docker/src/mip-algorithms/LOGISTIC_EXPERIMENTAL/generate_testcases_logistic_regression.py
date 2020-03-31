import json

from pathlib import Path
from random import shuffle

from sklearn.linear_model import LogisticRegression

from mipframework.algorithmtest import AlgorithmTest


class LogisticRegressionTest(AlgorithmTest):
    def get_expected(self, alg_input):
        x_names = alg_input[0]['value']
        y_name = alg_input[1]['value']
        variables = x_names + ',' + y_name
        data = self.get_data(variables)
        data = data.dropna()
        n_obs = len(data)
        if n_obs == 0 or data.shape[0] < data.shape[1]:
            return None

        categories = list(set(data[y_name]))
        if len(categories) < 2:
            return None
        shuffle(categories)
        cat_0, cat_1 = categories[:2]
        filter_ = {
            "condition": "OR",
            "rules"    : [
                {
                    "id"      : y_name,
                    "field"   : y_name,
                    "type"    : "string",
                    "input"   : "text",
                    "operator": "equal",
                    "value"   : cat_0
                },
                {
                    "id"      : y_name,
                    "field"   : y_name,
                    "type"    : "string",
                    "input"   : "text",
                    "operator": "equal",
                    "value"   : cat_1
                }
            ],
            "valid"    : True
        }
        alg_input[4]['value'] = json.dumps(filter_)

        data = data[(data[y_name] == cat_0) | (data[y_name] == cat_1)]
        y = data[y_name]
        X = data[x_names.split(',')]

        logreg_res = LogisticRegression(penalty='l2', solver='newton-cg').fit(X, y)
        coeff = logreg_res.intercept_.tolist() + logreg_res.coef_.tolist()[0]
        coeff_names = ['Intercept'] + x_names.split(',')
        return [
            {
                'n_obs'      : n_obs,
                'coeff'      : coeff,
                'coeff_names': coeff_names
            }
        ]


if __name__ == '__main__':
    prop_path = dbs_folder = Path(__file__).parent / 'properties.json'
    logistic_regression_test = LogisticRegressionTest(prop_path.as_posix())
    logistic_regression_test.generate_test_cases(num_tests=5)
    logistic_regression_test.to_json('logistic_regression_expected.json')
