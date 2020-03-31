from collections import defaultdict
import json
import random
import numpy as np

from sqlalchemy import select

from .data import DataBase

_PARAM_FIELDS = [
    'name',
    'type',
    'columnValuesSQLType',
    'columnValuesIsCategorical',
    'columnValuesNumOfEnumerations',
    'valueNotBlank',
    'valueMultiple',
    'valueType',
    'enumValues',
    'minValue',
    'maxValue'
]
_IGNORE = [
    'formula',
    'coding'
]


class AlgorithmTest(object):
    def __init__(self, properties_path):
        with open(properties_path, 'r') as prop:
            params = json.load(prop)['parameters']
            params = [defaultdict(lambda: None, p) for p in params]
        self.params = [
            {
                name: p[name]
                for name in _PARAM_FIELDS
            }
            for p in params
            if p['name'] not in _IGNORE
        ]
        self.db = DataBase(
            db_path='/Users/zazon/madgik/exareme/Exareme-Docker/src/mip-algorithms/algorithm_tests/static_data'
                    '/test_data.db',
            data_table_name='data',
            metadata_table_name='metadata'
        )
        self.categorical_variables, self.numerical_variables = self.get_variable_groups()
        self.test_cases = []

    def get_variable_groups(self):
        """
        Groups db variables in categorical and numerical
        and returns corresponding lists
        """
        categorical_vars = []
        numerical_vars = []
        sel_stmt = select(
            [self.db.metadata_table.c.code, self.db.metadata_table.c.isCategorical])
        result = self.db.engine.execute(sel_stmt)
        for c, i in result:
            categorical_vars.append(c) if i else numerical_vars.append(c)
        return categorical_vars, numerical_vars

    def generate_test_cases(self, num_tests=100):
        """Generates list of input/output pairs for algorithm tests"""
        while len(self.test_cases) < num_tests:
            in_ = self.generate_random_input()
            out = self.get_expected(in_)
            if out is None:
                continue
            self.test_cases.append({
                'input' : in_,
                'output': out
            })

    def get_expected(self, alg_input):
        """
        Produces expected output from some widely used library.
        Should be implemented in subclasses
        """
        raise NotImplementedError

    def generate_random_input(self):
        """Generates an algorithm input in the form expected by exareme"""
        alg_input = []
        for param in self.params:
            if param['type'] == 'column':
                param_value = self.get_random_column_param(param)
            elif param['type'] == 'other':
                param_value = self.get_random_other_param(param)
            elif param['name'] == 'filter':
                param_value = ''
            elif param['name'] == 'dataset':
                param_value = 'adni'
            elif param['name'] == 'pathology':
                param_value = 'dementia'
            else:
                raise ValueError('Parameter type must be column or other.')
            alg_input.append({
                'name' : param['name'],
                'value': param_value
            })
        return alg_input

    def get_random_column_param(self, param):
        """
        Generates a string of comma separated variable names
        according to the algorithm specifications
        """
        num_columns = 1
        if param['valueMultiple']:
            num_columns = random.randint(1, 20)
        if param['columnValuesIsCategorical'] == 'true':
            num_categorical, num_numerical = num_columns, 0
        elif param['columnValuesIsCategorical'] == 'false':
            num_categorical, num_numerical = 0, num_columns
        elif param['columnValuesIsCategorical'] == '':
            num_categorical = random.randint(0, num_columns)
            num_numerical = num_columns - num_categorical
        else:
            raise ValueError(
                'columnValuesIsCategorical can be true, false or empty.')
        categorical_choice = np.random.permutation(self.categorical_variables) \
                                 .tolist()[:num_categorical]
        numerical_choice = np.random.permutation(self.numerical_variables) \
                               .tolist()[:num_numerical]
        return ','.join(numerical_choice + categorical_choice)

    @staticmethod
    def get_random_other_param(param):
        """Generates a random instance of a type=other parameter"""
        if param['enumValues']:
            return random.choice(param['enumValues'])
        else:
            mi = param['minValue'] if param['minValue'] else -1000
            ma = param['maxValue'] if param['maxValue'] else 1000
            return random.randint(mi, ma)

    def get_data(self, variables):
        variables = variables.split(',')
        data = self.db.select_vars_from_data(variables, datasets=['adni'],
                                             filter_rules=None)
        return data

    def to_json(self, fname):
        with open(fname, 'w') as file:
            json.dump({
                'test_cases': self.test_cases
            }, file, indent=4)
