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


def query_from_formula(fname_db, formula, variables, data_tab_name, metadata_tab_name, no_intercept=False):
    """
    Queries a database based on a list of variables and a patsy (R language) formula. Additionally performs privacy
    check and returns results only if number of datapoints is sufficient.

    Parameters
    ----------
    fname_db : string
        Path and name of database.
    formula : string
        Formula in patsy (R language) syntax. E.g. 'y ~ x1 + x2 * x3'.
    variables : list of strings
        A list of the variables names.
    data_tab_name : string
        The name of the data table in the database.
    metadata_tab_name : string
        The name of the metagata table in the database.
    no_intercept : bool
        If no_intercept is True there is no intercept in the returned matrix(-ices). To use in the case where only a
        rhs expression is needed, not a full formula.

    Returns
    -------
    (lhs_dm, rhs_dm) or rhs_dm : patsy.DesignMatrix objects
        When a tilda is present in the formula, the function returns two design matrices (lhs_dm, rhs_dm).
        When it is not the function returns just the rhs_dm.
    """
    from numpy import log as log
    from numpy import exp as exp
    if no_intercept:
        formula += '-1'
    conn = sqlite3.connect(fname_db)
    # Perform privacy check
    if pd.read_sql_query(make_count_query(variables, data_tab_name), conn).iat[0, 0] < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Query results in illegal number of datapoints.')
        # TODO privacy check by variable
    # Pull is_categorical from metadata table
    is_categorical = [pd.read_sql_query(make_iscateg_query(v, metadata_tab_name,
                                                           is_categorical_argname='isCategorical', code_argname='code'),
                                        conn).iat[0, 0] for v in variables]
    # Pull data from db and return design matrix(-ces)
    data = pd.read_sql_query(make_data_query(variables, data_tab_name, is_categorical), conn)
    if '~' in formula:
        lhs_dm, rhs_dm = dmatrices(formula, data, return_type='dataframe')
        return lhs_dm, rhs_dm
    else:
        rhs_dm = dmatrix(formula, data, return_type='dataframe')
        return rhs_dm


def make_iscateg_query(variable, metadata_tab_name, is_categorical_argname, code_argname):
    q = 'SELECT ' + is_categorical_argname + ' FROM ' + metadata_tab_name
    q += ' WHERE ' + code_argname + "=='" + variable + "';"
    return q


def make_count_query(variables, data_tab_name):
    q = 'SELECT count(' + variables[0] + ') FROM ' + data_tab_name + ' WHERE '
    q += ' AND '.join([v + "!=''" for v in variables])
    q += ';'
    return q


def make_data_query(variables, data_tab_name, is_categorical):
    variables_casts = [v if not c else 'CAST(' + v + ' AS text) AS ' + v for v, c in zip(variables, is_categorical)]
    q = 'SELECT '
    q += ', '.join(variables_casts)
    q += ' FROM ' + data_tab_name + ' WHERE '
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


def main():
    fname_db = '/Users/zazon/madgik/mip_data/dementia/datasets.db'
    variables = ['gender', 'lefthippocampus', 'righthippocampus']
    formula = 'gender ~ lefthippocampus * righthippocampus'
    Y, X = query_from_formula(fname_db, formula, variables, data_tab_name='DATA', metadata_tab_name='METADATA',
                           no_intercept=True)
    print(Y.design_info.term_names)
    print(X.design_info.term_names)
    print(X)
    print(Y)


if __name__ == '__main__':
    main()
