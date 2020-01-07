from __future__ import division
from __future__ import print_function

import codecs
import errno
import logging
import os
import pickle
import sqlite3
from argparse import ArgumentParser
from collections import OrderedDict

import numpy as np
import pandas as pd
from patsy import dmatrix, dmatrices

PRIVACY_MAGIC_NUMBER = 10
P_VALUE_CUTOFF = 0.001
P_VALUE_CUTOFF_STR = '< ' + str(P_VALUE_CUTOFF)


class TransferAndAggregateData(object):
    def __init__(self, **kwargs):
        self.data = OrderedDict()
        self.reduce_type = OrderedDict()
        for k, v in kwargs.items():
            self.data[k] = v[0]
            self.reduce_type[k] = v[1]

    def __repr__(self):
        ret = ''
        for k in self.data.keys():
            ret += '{k} : {val}, reduce by {red_type}\n'.format(k=k, val=self.data[k], red_type=self.reduce_type[k])
        return ret

    def __add__(self, other):
        kwargs = OrderedDict()
        for k in self.data.keys():
            if self.reduce_type[k] == 'add':
                kwargs[k] = (self.data[k] + other.data[k], 'add')
            elif self.reduce_type[k] == 'max':
                kwargs[k] = (max(self.data[k], other.data[k]), 'max')
            elif self.reduce_type[k] == 'do_nothing':
                kwargs[k] = (self.data[k], 'do_nothing')
            else:
                raise ValueError('{rt} is not implemented as a reduce method.'.format(rt=self.reduce_type[k]))
        return TransferAndAggregateData(**kwargs)

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

    def get_data(self):
        return self.data


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


def query_from_formula(fname_db, formula, variables,
                       data_table, metadata_table, metadata_code_column,
                       metadata_isCategorical_column,
                       no_intercept=False, coding=None):
    """
    Queries a database based on a list of variables and a patsy (R language) formula. Additionally performs privacy
    check and returns results only if number of datapoints is sufficient.

    Parameters
    ----------
    fname_db : string
        Path and name of database.
    formula : string or None
        Formula in patsy (R language) syntax. E.g. 'y ~ x1 + x2 * x3'. If None a trivial formula of the form 'lhs ~
        rhs' is generated.
    variables : tuple of list of strings
        A tuple of the form (`lhs`, `rhs`) or (`rhs`,) where `lhs` and `rhs` are lists of the variable names.
    data_table : string
        The name of the data table in the database.
    metadata_table : string
        The name of the metagata table in the database.
    metadata_code_column : string
        The name of the code column in the metadata table in the database.
    metadata_isCategorical_column : string
        The name of the is_categorical column in the metadata table in the database.
    no_intercept : bool
        If no_intercept is True there is no intercept in the returned matrix(-ices). To use in the case where only a
        rhs expression is needed, not a full formula.
    coding : None or string
        Specifies the coding scheme for categorical variables. Must be in {None, 'Treatment', 'Poly', 'Sum', 'Diff',
        Helmert'}.

    Returns
    -------
    (lhs_dm, rhs_dm) or rhs_dm : patsy.DesignMatrix objects
        When a tilda is present in the formula, the function returns two design matrices (lhs_dm, rhs_dm).
        When it is not the function returns just the rhs_dm.
    """

    assert coding in {None, 'Treatment', 'Poly', 'Sum', 'Diff', 'Helmert'}

    # If no formula is given, generate a trivial one
    if formula is '' or formula is None:
        formula = '~'.join(map(lambda x: '+'.join(x), variables))
    variables = reduce(lambda a, b: a + b, variables)

    if no_intercept:
        formula += '-1'
    conn = sqlite3.connect(fname_db)

    # Define query forming functions
    def iscateg_query(var):
        return "SELECT {is_cat} FROM {metadata} WHERE {code}=='{var}';".format(is_cat=metadata_isCategorical_column,
                                                                               metadata=metadata_table,
                                                                               code=metadata_code_column,
                                                                               var=var)

    def count_query(varz):
        return 'SELECT COUNT({var}) FROM {data} WHERE {clause};'.format(var=varz[0],
                                                                        data=data_table,
                                                                        clause=' AND '.join(
                                                                                ["{}!=''".format(v)
                                                                                 for v in varz]))

    def data_query(varz, is_cat):
        variables_casts = ', '.join(
                [v if not c else 'CAST({v} AS text) AS {v}'.format(v=v) for v, c in
                 zip(varz, is_cat)])
        return 'SELECT {variables} FROM {data} WHERE {clause};'.format(variables=variables_casts,
                                                                       data=data_table,
                                                                       clause=' AND '.join(
                                                                               ["{}!=''".format(v)
                                                                                for v in varz]))

    # Perform privacy check
    if pd.read_sql_query(sql=count_query(variables), con=conn).iat[0, 0] < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Query results in illegal number of datapoints.')
    # Pull is_categorical from metadata table
    is_categorical = [pd.read_sql_query(sql=iscateg_query(v), con=conn).iat[0, 0] for v in
                      variables]
    if coding is not None:
        for c, v in zip(is_categorical, variables):
            if c:
                formula = formula.replace(v, 'C({v}, {coding})'.format(v=v, coding=coding))
    # Pull data from db and return design matrix(-ces)
    data = pd.read_sql_query(sql=data_query(variables, is_categorical), con=conn)
    if '~' in formula:
        lhs_dm, rhs_dm = dmatrices(formula, data, return_type='dataframe')
        return lhs_dm, rhs_dm
    else:
        rhs_dm = dmatrix(formula, data, return_type='dataframe')
        return None, rhs_dm


def value_casting(value, type):
    if type == 'text':
        return str(value)
    elif type == 'real' or type == 'int':
        return float(value)


def variable_type(value):
    if str(value) == 'text':
        return 'S16'
    elif str(value) == 'real' or str(value) == 'int':
        return 'float64'


def query_database(fname_db, queryData, queryMetadata):
    # connect to database
    conn = sqlite3.connect(fname_db)
    cur = conn.cursor()

    cur.execute(queryData)
    data = cur.fetchall()
    if len(data) < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Query results in illegal number of datapoints.')
    dataSchema = [description[0] for description in cur.description]

    cur.execute(queryMetadata)
    metadata = cur.fetchall()
    metadataSchema = [description[0] for description in cur.description]
    conn.close()

    # Save data to pd.Dataframe
    dataFrame = pd.DataFrame.from_records(data=data, columns=dataSchema)

    # Cast Dataframe based on metadata
    metadataVarNames = [str(x) for x in list(zip(*metadata)[0])]
    metadataTypes = [variable_type(x) for x in list(zip(*metadata)[1])]
    for varName in dataSchema:
        index = metadataVarNames.index(varName)
        dataFrame[varName] = dataFrame[varName].astype(metadataTypes[index])

    return dataSchema, metadataSchema, metadata, dataFrame


def variable_categorical_getDistinctValues(metadata):
    distinctValues = dict()
    dataTypes = zip((str(x) for x in list(zip(*metadata)[0])),
                    (str(x) for x in list(zip(*metadata)[1])))
    for md in metadata:
        if md[2] == 1:  # when variable is categorical
            distinctValues[str(md[0])] = [value_casting(x, str(md[1])) for x in md[3].split(',')]
    return distinctValues


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


class Global2Local_TD(TransferData):
    def __init__(self, **kwargs):
        self.data = kwargs

    def get_data(self):
        return self.data


def set_algorithms_output_data(data):
    print(data)


class PrivacyError(Exception):
    def __init__(self, message):
        super(PrivacyError, self).__init__(message)


class ExaremeError(Exception):
    def __init__(self, message):
        super(ExaremeError, self).__init__(message)


def make_json_raw(**kwargs):
    result_list = []
    for k, v in kwargs.items():
        result_list.append({
            k: v if type(v) != np.ndarray else v.tolist()
        })
    return result_list


# TODO Open json file using relative path
def parse_exareme_args():
    import json
    with open('/root/mip-algorithms/PCA/properties.json', 'r') as prop:
        params = json.load(prop)['parameters']
    parser = ArgumentParser()
    # Add Exareme arguments
    parser.add_argument('-input_local_DB', required=False, help='Path to local db.')
    parser.add_argument('-db_query', required=False, help='Query to be executed on local db.')
    parser.add_argument('-cur_state_pkl', required=False, help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=False, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-local_step_dbs', required=False, help='Path to local db.')
    parser.add_argument('-global_step_db', required=False, help='Path to db holding global step results.')
    parser.add_argument('-data_table', required=False)
    parser.add_argument('-metadata_table', required=False)
    parser.add_argument('-metadata_code_column', required=False)
    parser.add_argument('-metadata_isCategorical_column', required=False)
    # Add algorithm arguments
    for p in params:
        name = '-' + p['name']
        required = p['valueNotBlank']
        if name not in ['pathology', 'dataset', 'filter']:
            parser.add_argument(name, required=required)

    args, unknown = parser.parse_known_args()
    return args


def main():
    fname_db = '/Users/zazon/madgik/mip_data/dementia/datasets.db'
    lhs = ['leftaccumbensarea']
    rhs = ['leftaccumbensarea', 'leftacgganteriorcingulategyrus']
    variables = (rhs,)
    formula = 'gender ~ alzheimerbroadcategory * lefthippocampus'
    formula = None
    _, X = query_from_formula(fname_db, formula, variables, data_table='DATA',
                              metadata_table='METADATA',
                              metadata_code_column='code',
                              metadata_isCategorical_column='isCategorical',
                              no_intercept=True, coding='Diff')
    print(X.design_info.column_names)
    print(np.array(X))
    X = np.array(X)
    print(X.sum(axis=0))
    print(len(X))
    # print(Y.design_info.column_names)
    # print(Y)


if __name__ == '__main__':
    main()
