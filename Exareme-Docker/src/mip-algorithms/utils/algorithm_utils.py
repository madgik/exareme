from __future__ import division
from __future__ import print_function

import sqlite3
import pickle
import codecs
import logging
import os
import errno
import pandas as pd
import numpy as np
from patsy import dmatrix, dmatrices

PRIVACY_MAGIC_NUMBER = 10
P_VALUE_CUTOFF = 0.001
P_VALUE_CUTOFF_STR = '< ' + str(P_VALUE_CUTOFF)


class TransferData():
    def __add__(self, other):
        raise NotImplementedError('The __add__ method should be implemented by the child class.')

    @classmethod
    def load(cls, inputDB):
        conn = sqlite3.connect(inputDB)
        cur = conn.cursor()
        cur.execute('SELECT data FROM transfer')
        first = True
        result = None
        for row in cur:
            if first:
                result = pickle.loads(codecs.decode(row[0], 'ascii'))
                first = False
            else:
                result += pickle.loads(codecs.decode(row[0], 'ascii'))
        return result

    def transfer(self):
        print(codecs.encode(pickle.dumps(self), 'ascii'))


def query_with_privacy(fname_db, query):
    conn = sqlite3.connect(fname_db)
    cur = conn.cursor()
    cur.execute(query)
    schema = [description[0] for description in cur.description]
    data = cur.fetchall()
    if len(data) < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Query results in illegal number of datapoints.')
    return schema, data


def query_from_formula(fname_db, formula, variables):
    from numpy import log as log
    from numpy import exp as exp
    conn = sqlite3.connect(fname_db)
    # Perform privacy check
    if pd.read_sql_query(make_count_query(variables), conn).iat[0, 0] < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Query results in illegal number of datapoints.')
        # TODO privacy check by variable
    # Pull data and return design matrix(-ces)
    data = pd.read_sql_query(make_data_query(variables), conn)
    if '~' in formula:
        Y, X = dmatrices(formula, data, return_type='dataframe')
        return Y, X
    else:
        X = dmatrix(formula, data, return_type='dataframe')
        return X


def make_count_query(variables):
    q = 'SELECT count(' + variables[0] + ') FROM DATA WHERE '
    q += ' AND '.join([v + "!=''" for v in variables])
    q += ';'
    return q


def make_data_query(variables):
    q = 'SELECT '
    q += ', '.join(variables)
    q += ' FROM DATA WHERE '
    q += ' AND '.join([v + "!=''" for v in variables])
    q += ';'
    return q


class StateData(object):
    def __init__(self, **kwargs):
        self.data = kwargs

    def get_data(self):
        return self.data

    def save(self, fname, pickle_protocol=2):
        if not os.path.exists(os.path.dirname(fname)):
            try:
                os.makedirs(os.path.dirname(fname))
            except OSError as exc:  # Guard against race condition
                if exc.errno != errno.EEXIST:
                    raise
        with open(fname, 'wb') as f:
            try:
                pickle.dump(self, f, protocol=pickle_protocol)
            except pickle.PicklingError:
                print('Unpicklable object.')

    @classmethod
    def load(cls, fname):
        with open(fname, 'rb') as f:
            try:
                obj = pickle.load(f)
            except pickle.UnpicklingError:
                print('Cannot unpickle.')
                raise
        return obj


def init_logger():
    logging.basicConfig(filename='/var/log/exaremePythonAlgorithms.log')


def set_algorithms_output_data(data):
    print(data)


class PrivacyError(Exception):
    def __init__(self, message):
        super(PrivacyError, self).__init__(message)


class ExaremeError(Exception):
    def __init__(self, message):
        super(ExaremeError, self).__init__(message)


if __name__ == '__main__':
    fname_db = '/Users/zazon/madgik/mip_data/dementia/datasets.db'
    variables = ['lefthippocampus', 'leftamygdala', 'gender', 'righthippocampus']
    formula = "gender ~ (lefthippocampus + leftamygdala) * righthippocampus + log(leftamygdala) + standardize(" \
              "righthippocampus) + exp(lefthippocampus) + I(lefthippocampus + leftamygdala*2)"
    Y, X = query_from_formula(fname_db, formula, variables)
    print(Y.design_info.term_names)
    print(X.design_info.term_names)
    print(X)
