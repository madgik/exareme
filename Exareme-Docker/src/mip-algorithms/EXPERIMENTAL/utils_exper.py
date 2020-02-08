# Forward compatibility
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import codecs
import errno
import json
import os
import pickle
from functools import wraps
import sqlite3

from argparse import ArgumentParser

import numpy as np
import pandas as pd
from patsy import dmatrix, dmatrices

# ------------------------ Use only during dev ---------------- #
fake_args = [
    '-input_local_DB', 'path/to/data',
    '-db_query', '',
    '-cur_state_pkl', 'cur_state.pkl',
    '-prev_state_pkl', 'prev_state.pkl',
    '-local_step_dbs', 'path/to/local/dbs',
    '-global_step_db', 'path/to/global/dbs',
    '-data_table', 'DATA',
    '-metadata_table', 'METADATA',
    '-metadata_code_column', 'code',
    '-metadata_isCategorical_column', 'isCategorical',
    '-x', 'my_x',
    '-y', 'my_y',
    '-pathology', 'dementia',
    '-dataset', 'adni',
    '-filter', ''
]

# -------------------------- Globals -------------------------- #
_ALLOWED_METHODS = {'run_local', 'run_central'}

_COMMON_ALGORITHM_ARGUMENTS = {
    'input_local_DB',
    'db_query',
    'cur_state_pkl',
    'prev_state_pkl',
    'local_step_dbs',
    'global_step_db',
    'data_table',
    'metadata_table',
    'metadata_code_column',
    'metadata_isCategorical_column',
}

_PRIVACY_THRESHOLD = 10


# ----------------------- Helpers ----------------------- #
def make_dirs(fname):
    if not os.path.exists(os.path.dirname(fname)):
        try:
            os.makedirs(os.path.dirname(fname))
        except OSError as exc:  # Guard against race condition
            if exc.errno != errno.EEXIST:
                raise


def get_data(params):
    return {'x': 42}


# ------------------------- Exceptions ------------------------- #
class UnknownFunctionError(Exception):
    pass


class TransferError(Exception):
    pass


class AlgorithmError(Exception):
    def __getitem__(self, item):
        raise self


class PrivacyError(Exception):
    pass


# ----------------------- Meta-programming ----------------------- #
class Meta(type):
    def __new__(mcs, name, bases, attrs):
        for key, value in attrs.items():
            if callable(value) and value.__name__ in _ALLOWED_METHODS:
                attrs[key] = exareme(value)
        return type.__new__(mcs, name, bases, attrs)


def exareme(func):
    func_name = func.__name__
    if func_name == 'run_local':
        @wraps(func)
        def wrapper(self):
            print('Getting data from DBs')
            # self.data = AlgorithmData(self._args) # Uncomment when ready
            self.data = get_data(None)
            print('Start local computation')
            func(self)
            print('End local computation')
            print('Pushing all to central node')
            self._transfer_struct.transfer_all()
            print('\n')
    elif func_name == 'run_central':
        @wraps(func)
        def wrapper(self):
            self.data = AlgorithmError('There are no data available on the global node!')
            print('Fetching all from local nodes')
            self._transfer_struct = TransferStruct.fetch_all(transfer_db='transfer_db.txt')
            print('Start central computation')
            func(self)
            print('End central computation')
            print('Displaying result')
            self.set_algorithms_output_data()
            print('\n')
    else:
        raise UnknownFunctionError(func_name)
    return wrapper


def one_kwarg(func):
    @wraps(func)
    def wrapper(self, **kwarg):
        if len(kwarg) != 1:
            raise ValueError('Please push one variable at the time.')
        func(self, **kwarg)

    return wrapper


# ----------------------- TransferStruct ----------------------- #
class TransferRule(object):
    def __init__(self, val):
        self.val = val

    def __add__(self, other):
        raise NotImplementedError

    def __repr__(self):
        return str(self.val)


class AddMe(TransferRule):
    def __add__(self, other):
        return AddMe(self.val + other.val)


class MaxMe(TransferRule):
    def __add__(self, other):
        return MaxMe(max(self.val, other.val))


class MinMe(TransferRule):
    def __add__(self, other):
        return MinMe(min(self.val, other.val))


class ConcatMe(TransferRule):
    def __add__(self, other):
        if type(self.val) == list and type(other.val) == list:
            return ConcatMe(self.val + other.val)
        elif type(self.val) == np.ndarray and type(other.val) == np.ndarray:
            return ConcatMe(np.concatenate((self.val, other.val)))


class DoNothing(TransferRule):
    def __add__(self, other):
        raise TransferError('The DoNothing class should be used only to transfer data from the global node to the '
                            'locals!')


class TransferStruct(object):
    def __init__(self, **kwargs):
        for name, val in kwargs.items():
            setattr(self, name, val)

    def __add__(self, other):
        return TransferStruct(**{name: val + other.__dict__[name] for name, val in self.__dict__.items()})

    def __getitem__(self, name):
        return getattr(self, name).val

    def __repr__(self):
        return 'TransferStruct({contents})'.format(contents={name: val for name, val in self.__dict__.items()})

    def register_for_transfer(self, rule_cls, **kwarg):
        name, var = kwarg.popitem()
        setattr(self, name, rule_cls(var))

    def transfer_all(self):
        with open('transfer_db.txt', 'w') as f:
            f.write(codecs.encode(pickle.dumps(self), 'ascii'))

    @classmethod
    def fetch_all(cls, transfer_db):  # TODO replace with sqlalchemy
        with open(transfer_db, 'r') as f:
            content = f.read()
            return pickle.loads(codecs.decode(content, 'ascii'))

        # conn = sqlite3.connect(transfer_db)
        # cur = conn.cursor()
        # cur.execute('SELECT data FROM _transfer_struct')
        # first = True
        # result = None
        # for row in cur:
        #     if first:
        #         result = pickle.loads(codecs.decode(row[0], 'ascii'))
        #         first = False
        #     else:
        #         result += pickle.loads(codecs.decode(row[0], 'ascii'))
        # return result


# ------------------------- State ------------------------- #
# class State(object):
#
#     @one_kwarg
#     def save(self, **kwarg):
#         name, var = kwarg.popitem()
#         setattr(self, name, var)
#
#     def save_all(self, fname):
#         make_dirs(fname)
#         with open(fname, 'wb') as f:
#             try:
#                 pickle.dump(self, f, protocol=2)
#             except pickle.PicklingError:
#                 print('Cannot pickle object.')
#
#     @classmethod
#     def load_all(cls, fname):
#         with open(fname, 'rb') as f:
#             try:
#                 obj = pickle.load(f)
#             except pickle.UnpicklingError:
#                 print('Cannot unpickle.')
#                 raise
#         return obj
#

# ----------------------- Parameters ----------------------- #
class Parameters(object):
    def __init__(self, args):
        for name, val in args.__dict__.items():
            if name not in _COMMON_ALGORITHM_ARGUMENTS:
                setattr(self, name, val)

    def __getitem__(self, name):
        return getattr(self, name)


def parse_exareme_args(fp):
    parser = ArgumentParser()
    # Add common arguments
    for arg in _COMMON_ALGORITHM_ARGUMENTS:
        parser.add_argument('-' + arg)
    # Add algorithm specific arguments
    prop_path = os.path.join(fp, 'properties.json')
    with open(prop_path, 'r') as prop:
        params = json.load(prop)['parameters']
    for p in params:
        name = '-' + p['name']
        required = p['valueNotBlank']
        parser.add_argument(name, required=required)

    args, unknown = parser.parse_known_args(args=fake_args)  # TODO remove when ready
    return args


# ----------------------- Query DB ------------------------- #
class AlgorithmData(object):
    def __init__(self, args):
        if 'y' in args and 'x' not in args:
            args_y = list(
                    args.y
                        .replace(' ', '')
                        .split(',')
            )
            vars_tuples = (args_y,)
            self.variables = query_from_formula(args, vars_tuples)
        elif 'y' in args and 'x' in args:
            args_y = list(
                    args.y
                        .replace(' ', '')
                        .split(',')
            )
            args_x = list(
                    args.x
                        .replace(' ', '')
                        .split(',')
            )
            vars_tuples = (args_y, args_x)
            self.variables, self.covariables = query_from_formula(args, vars_tuples)
        else:
            raise AlgorithmError('Algorithm should either contain `y` and no `x` (variables only), or `y` and `x` ('
                                 'variables and covariables).')


# TODO refactor this shit with SQLAlchemy
def query_from_formula(args, vars_tuples):
    fname_db = args.input_local_DB
    formula = args.formula
    formula = formula.replace('_', '~')  # TODO Fix tilda problem and remove
    no_intercept = args.no_interecpt
    coding = args.coding
    dataset = args.dataset
    query_filter = args.filter
    data_table = args.data_table
    metadata_table = args.metadata_table
    metadata_code_column = args.metadata_code_column
    metadata_isCategorical_column = args.metadata_isCategorical_column

    from numpy import log as log
    from numpy import exp as exp
    _ = log(exp(1))  # This line is needed to prevent import opimizer from removing above lines

    assert coding in {None, 'Treatment', 'Poly', 'Sum', 'Diff', 'Helmert'}
    dataset = dataset.replace(' ', '').split(',')

    # If no formula is given, generate a trivial one
    if formula == '':
        formula = '~'.join(map(lambda x: '+'.join(x), vars_tuples))
    vars_tuples = reduce(lambda a, b: a + b, vars_tuples)

    # Parse filter if given
    if query_filter == '':
        query_filter_clause = ''
    else:
        query_filter_clause = parse_filter(json.loads(query_filter))

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
        return "SELECT COUNT({var}) FROM {data} WHERE ({var_clause}) AND ({ds_clause}) {flt_clause};".format(
                var=varz[0],
                data=data_table,
                var_clause=' AND '.join(["{v}!='' and {v} is not null".format(v=v) for v in varz]),
                ds_clause=' OR '.join(["dataset=='{d}'".format(d=d) for d in dataset]),
                flt_clause='' if query_filter_clause == '' else 'AND ({flt_clause})'.format(
                        flt_clause=query_filter_clause)
        )

    def data_query(varz, is_cat):
        variables_casts = ', '.join(
                [v if not c else 'CAST({v} AS text) AS {v}'.format(v=v) for v, c in
                 zip(varz, is_cat)]
        )
        return "SELECT {variables} FROM {data} WHERE ({var_clause}) AND ({ds_clause})  {flt_clause};".format(
                variables=variables_casts,
                data=data_table,
                var_clause=' AND '.join(["{v}!='' and {v} is not null".format(v=v) for v in varz]),
                ds_clause=' OR '.join(["dataset=='{d}'".format(d=d) for d in dataset]),
                flt_clause='' if query_filter_clause == '' else 'AND ({flt_clause})'.format(
                        flt_clause=query_filter_clause)
        )

    # Perform privacy check
    if pd.read_sql_query(sql=count_query(vars_tuples), con=conn).iat[0, 0] < _PRIVACY_THRESHOLD:
        raise PrivacyError('Query results in illegal number of datapoints.')
    # Pull is_categorical from metadata table
    is_categorical = [pd.read_sql_query(sql=iscateg_query(v), con=conn).iat[0, 0] for v in
                      vars_tuples]
    if coding is not None:
        for c, v in zip(is_categorical, vars_tuples):
            if c:
                formula = formula.replace(v, 'C({v}, {coding})'.format(v=v, coding=coding))
    # Pull data from db and return design matrix(-ces)
    data = pd.read_sql_query(sql=data_query(vars_tuples, is_categorical), con=conn)
    if '~' in formula:
        variables, covariables = dmatrices(formula, data, return_type='dataframe')
        return variables, covariables
    else:
        variables = dmatrix(formula, data, return_type='dataframe')
        return variables


def parse_filter(query_filter):
    _operators = {
        "equal"      : "=",
        "not_equal"  : "!=",
        "less"       : "<",
        "greater"    : ">",
        "between"    : "between",
        "not_between": "not_between"
    }

    def add_spaces(s):
        return ' ' + s + ' '

    def format_rule(rule):
        id_ = rule['id']
        op = _operators[rule['operator']]
        val = rule['value']
        type_ = rule['type']
        if type_ == 'string':
            if type(val) == list:
                val = ["'{v}'".format(v=v) for v in val]
            else:
                val = "'{v}'".format(v=val)
        if op == 'between':
            return "{id} BETWEEN {val1} AND {val2}".format(id=id_, op=op, val1=val[0], val2=val[1])
        elif op == 'not_between':
            return "{id} NOT BETWEEN {val1} AND {val2}".format(id=id_, op=op, val1=val[0], val2=val[1])
        else:
            return "{id}{op}{val}".format(id=id_, op=op, val=val)

    def format_group(group):
        return '({group})'.format(group=group)

    cond = query_filter['condition']
    rules = query_filter['rules']
    return add_spaces(cond).join(
            [format_rule(rule=rule)
             if 'id' in rule else format_group(group=parse_filter(rule)) for rule in rules])


# ----------------------- Algorithms ----------------------- #
class Algorithm(object):
    __metaclass__ = Meta

    def __init__(self):
        self._folder_path = os.path.split(__file__)[0]
        self._args = parse_exareme_args(self._folder_path)
        self.parameters = Parameters(self._args)
        self.data = None
        self._transfer_struct = TransferStruct()
        self.result = None

    def set_algorithms_output_data(self):
        print(self.result)

    @one_kwarg
    def push_to_local(self, **kwarg):
        self._transfer_struct.register_for_transfer(DoNothing, **kwarg)

    @one_kwarg
    def push_and_add(self, **kwarg):
        self._transfer_struct.register_for_transfer(AddMe, **kwarg)

    @one_kwarg
    def push_and_min(self, **kwarg):
        self._transfer_struct.register_for_transfer(MinMe, **kwarg)

    @one_kwarg
    def push_and_max(self, **kwarg):
        self._transfer_struct.register_for_transfer(MaxMe, **kwarg)

    @one_kwarg
    def push_and_concat(self, **kwarg):
        self._transfer_struct.register_for_transfer(ConcatMe, **kwarg)

    def fetch(self, name):
        try:
            return self._transfer_struct[name]
        except KeyError:
            print('Cannot fetch unknown variable.')
            raise
        except TypeError:
            print('{} is not a variable name.'.format(name))
            raise


class LocalGlobal(Algorithm):
    def run_local(self):
        raise NotImplementedError

    def run_central(self):
        raise NotImplementedError


# =============================== Tests =============================== #


class MyAlgorithm(LocalGlobal):
    def run_local(self):
        print('This is a local computation')
        x = self.data['x']
        y = 2 * x
        self.push_and_add(x=x)
        self.push_and_add(y=y)

    def run_central(self):
        print('This is a global computation')
        x = self.fetch('x')
        y = self.fetch('y')
        self.result = {'x': x, 'y': y}


def run():
    a = MyAlgorithm()
    a.run_local()
    a.run_central()

    print('The End')


if __name__ == '__main__':
    run()
