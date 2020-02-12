import codecs
import errno
import json
import logging
import os
import pickle
from argparse import ArgumentParser
from functools import wraps

import numpy as np
import pandas as pd
from PEARSON_EXPERIMENTAL.mip_framework import _LOGGING_LEVEL
from sqlalchemy import between, not_, and_, or_, Table, select, create_engine, MetaData
from patsy import dmatrix, dmatrices

from PEARSON_EXPERIMENTAL.mip_framework.exceptions import TransferError, PrivacyError, UnknownFunctionError, \
    AlgorithmError

my_filter = '''{
    "condition": "AND",
    "rules"    : [
        {
            "id"      : "alzheimerbroadcategory",
            "field"   : "alzheimerbroadcategory",
            "type"    : "string",
            "input"   : "select",
            "operator": "not_equal",
            "value"   : "MCI"
        },
        {
            "id"      : "alzheimerbroadcategory",
            "field"   : "alzheimerbroadcategory",
            "type"    : "string",
            "input"   : "select",
            "operator": "not_equal",
            "value"   : "Other"
        }
    ],
    "valid"    : true
}'''

fake_args = [
    '-input_local_DB', 'datasets.db',
    '-db_query', '',
    '-cur_state_pkl', 'cur_state.pkl',
    '-prev_state_pkl', 'prev_state.pkl',
    '-local_step_dbs', 'path/to/local/dbs',
    '-global_step_db', 'path/to/global/dbs',
    '-data_table', 'DATA',
    '-metadata_table', 'METADATA',
    '-metadata_code_column', 'code',
    '-metadata_isCategorical_column', 'isCategorical',
    '-y', 'alzheimerbroadcategory, lefthippocampus',
    '-pathology', 'dementia',
    '-dataset', 'adni',
    '-filter', my_filter,
    '-formula', '',
    '-coding', None
]

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

_FILTER_OPERATORS = {
    'equal'           : lambda a, b: a.__eq__(b),
    'not_equal'       : lambda a, b: a.__ne__(b),
    'less'            : lambda a, b: a.__lt__(b),
    'greater'         : lambda a, b: a.__gt__(b),
    'less_or_equal'   : lambda a, b: a.__le__(b),
    'greater_or_equal': lambda a, b: a.__ge__(b),
    'between'         : lambda a, b: between(a, b[0], b[1]),
    'not_between'     : lambda a, b: not_(between(a, b[0], b[1]))
}

_FILTER_CONDS = {
    'AND': lambda *a: and_(*a),
    'OR' : lambda *a: or_(*a),
}

_ALLOWED_METHODS = {'local_', 'global_'}


class AlgorithmMeta(type):
    def __new__(mcs, name, bases, attrs):
        for key, attr in attrs.items():
            if callable(attr):
                if attr.__name__ not in _ALLOWED_METHODS:
                    attrs[key] = logged(attr)
                if attr.__name__ in _ALLOWED_METHODS:
                    attrs[key] = logged(algorithm_methods_decorator(attr))
            # if callable(attr) and attr.__name__ in _ALLOWED_METHODS:
            #     attrs[key] = algorithm_methods_decorator(attr)
        return type.__new__(mcs, name, bases, attrs)


def algorithm_methods_decorator(func):
    func_name = func.__name__
    if func_name == 'local_':
        wrapper = make_local_wrapper(func)
    elif func_name == 'global_':
        wrapper = make_global_wrapper(func)
    else:
        logging.error('Unknown function name.')
        raise UnknownFunctionError(func_name)
    return wrapper


def make_local_wrapper(func):
    @wraps(func)
    def wrapper(self):
        self.data = AlgorithmData(self._args)
        func(self)
        self._transfer_struct.transfer_all()

    return wrapper


def make_global_wrapper(func):
    @wraps(func)
    def wrapper(self):
        self.data = AlgorithmError('There are no data available on the global node. Only local nodes can access '
                                   'data.')  # todo rephrase this
        self._transfer_struct = TransferStruct.fetch_all(
                transfer_db='/Users/zazon/madgik/exareme/Exareme-Docker/src/mip-algorithms/EXPERIMENTAL/transfer_db.txt')
        func(self)
        self.set_algorithms_output_data()

    return wrapper


def logged(func):
    def wrapper(*args, **kwargs):
        try:
            if _LOGGING_LEVEL == logging.INFO:
                logging.info("Starting: '{0}'".format(func.__name__))
            elif _LOGGING_LEVEL == logging.DEBUG:
                logging.info(
                        "Starting: '{0}',\nargs:\n{1},\nkwargs:\n{2}".format(
                                func.__name__, args,
                                kwargs))
            return func(*args, **kwargs)
        except Exception as e:
            logging.exception(e)
            raise e

    return wrapper


@logged
def make_dirs(fname):
    if not os.path.exists(os.path.dirname(fname)):
        try:
            os.makedirs(os.path.dirname(fname))
        except OSError as exc:  # Guard against race condition
            if exc.errno != errno.EEXIST:
                raise


def one_kwarg(func):
    @wraps(func)
    def wrapper(self, **kwarg):
        if len(kwarg) != 1:
            raise ValueError('Please push one variable at the time.')
        func(self, **kwarg)

    return wrapper


class TransferRule(object):
    @logged
    def __init__(self, val):
        self.val = val

    def __add__(self, other):
        raise NotImplementedError

    def __repr__(self):
        return str(self.val)


class AddMe(TransferRule):
    @logged
    def __add__(self, other):
        return AddMe(self.val + other.val)


class MaxMe(TransferRule):
    @logged
    def __add__(self, other):
        return MaxMe(max(self.val, other.val))


class MinMe(TransferRule):
    @logged
    def __add__(self, other):
        return MinMe(min(self.val, other.val))


class ConcatMe(TransferRule):
    @logged
    def __add__(self, other):
        if type(self.val) == list and type(other.val) == list:
            return ConcatMe(self.val + other.val)
        elif type(self.val) == np.ndarray and type(other.val) == np.ndarray:
            return ConcatMe(np.concatenate((self.val, other.val)))


class DoNothing(TransferRule):
    @logged
    def __add__(self, other):
        if self.val != other.val:
            raise TransferError('Local nodes do not agree on common variable: {0}, {1}'.format(self.val, other.val))


class TransferStruct(object):
    @logged
    def __init__(self, **kwargs):
        for name, val in kwargs.items():
            setattr(self, name, val)

    @logged
    def __add__(self, other):
        return TransferStruct(**{name: val + other.__dict__[name] for name, val in self.__dict__.items()})

    @logged
    def __getitem__(self, name):
        return getattr(self, name).val

    def __repr__(self):
        return 'TransferStruct({contents})'.format(contents={name: val for name, val in self.__dict__.items()})

    @logged
    def register_for_transfer(self, rule_cls, **kwarg):
        name, var = kwarg.popitem()
        setattr(self, name, rule_cls(var))

    @logged
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


class Parameters(object):
    @logged
    def __init__(self, args):
        for name, val in args.__dict__.items():
            if name not in _COMMON_ALGORITHM_ARGUMENTS:
                setattr(self, name, val)

    def __getitem__(self, name):
        return getattr(self, name)


@logged
def parse_exareme_args(fp, cli_args):
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

    args, _ = parser.parse_known_args(cli_args)
    args.y = list(args.y
                  .replace(' ', '')
                  .split(','))
    if args.x:
        args.x = list(args.x
                      .replace(' ', '')
                      .split(','))
    args.dataset = list(args.dataset
                        .replace(' ', '')
                        .split(','))
    if args.filter:
        args.filter = json.loads(args.filter)
    return args


class AlgorithmData(object):
    @logged
    def __init__(self, args):
        data, is_categorical = read_data_from_db(args)
        model_vars = get_model_variables(args, data, is_categorical)
        if len(model_vars) == 1:
            self.variables = model_vars[0]
        elif len(model_vars) == 2:
            self.variables, self.covariables = model_vars


@logged
def get_model_variables(args, data, is_categorical):
    from numpy import log as log
    from numpy import exp as exp
    _ = log(exp(1))  # This line is needed to prevent import opimizer from removing above lines
    # Get formula from args or build if doesn't exist
    if args.formula:
        formula = args.formula.replace('_', '~')  # fixme
    else:
        if args.x:
            var_tup = (args.y, args.x)
            formula = '~'.join(map(lambda x: '+'.join(x), var_tup))
        else:
            formula = '+'.join(args.y) + '-1'
    # Process categorical vars
    var_names = args.y
    if args.x:
        var_names.extend(args.x)
    if args.coding:
        for var in var_names:
            if is_categorical[var]:
                formula = formula.replace(var, 'C({v}, {coding})'.format(v=var, coding=args.coding))
    # Create variables (and possibly covariables)
    if args.x:
        model_vars, model_covars = dmatrices(formula, data, return_type='dataframe')
        return model_vars, model_covars
    else:
        model_vars = dmatrix(formula, data, return_type='dataframe')
        return model_vars,


@logged
def read_data_from_db(args):
    engine, sqla_md = connect_to_db(args.input_local_DB)
    # Create tables
    data_table = Table(args.data_table, sqla_md, autoload=True)
    metadata_table = Table(args.metadata_table, sqla_md, autoload=True)
    # Get data
    var_names = args.y
    if args.x:
        var_names.extend(args.x)
    data = select_data_from_datasets(table=data_table, engine=engine, var_names=var_names, datasets=args.dataset,
                                     query_filter=args.filter)
    # Privacy check
    if len(data) < _PRIVACY_THRESHOLD:
        raise PrivacyError
    # Get isCategorical
    is_categorical = select_iscategorical_from_metadata(engine, metadata_table, var_names)
    return data, is_categorical


@logged
def select_iscategorical_from_metadata(engine, metadata_table, var_names):
    is_categorical = dict()
    sel_iscat = select([metadata_table.c.code, metadata_table.c.isCategorical]) \
        .where(or_(*[metadata_table.c.code == vn for vn in var_names]))
    result = engine.execute(sel_iscat)
    for vn, i in result:
        is_categorical[vn] = i
    return is_categorical


@logged
def build_filter_clause(table, rules):
    if 'condition' in rules:
        cond = _FILTER_CONDS[rules['condition']]
        rules = rules['rules']
        return cond(*[build_filter_clause(table, rule) for rule in rules])
    elif 'id' in rules:
        var_name = rules['id']
        op = _FILTER_OPERATORS[rules['operator']]
        val = rules['value']
        return op(table.c[var_name], val)


@logged
def select_data_from_datasets(table, engine, var_names, datasets, query_filter):
    dataset_clause = or_(*[table.c.dataset == ds for ds in datasets])
    filter_clause = build_filter_clause(table, query_filter)
    sel_stmt = select([table.c[var] for var in var_names]).where(dataset_clause).where(filter_clause)
    data = pd.read_sql(sel_stmt, engine)
    data.replace('', np.nan, inplace=True)  # todo remove when no empty str in dbs
    data = data.dropna()
    return data


@logged
def connect_to_db(db_path):
    engine = create_engine('sqlite:///{}'.format(db_path), echo=False)
    sqla_md = MetaData(engine)
    return engine, sqla_md


class Algorithm(object):
    __metaclass__ = AlgorithmMeta

    def __init__(self, alg_file, cli_args):
        self._folder_path, name = os.path.split(alg_file)
        self._name = os.path.splitext(name)[0]
        self._args = parse_exareme_args(self._folder_path, cli_args)
        self.parameters = Parameters(self._args)
        self.data = None
        self._transfer_struct = TransferStruct()
        self.result = None

    def set_algorithms_output_data(self):
        try:
            print(json.dumps(self.result, allow_nan=False))
        except ValueError:
            print('Result contains NaNs.')

    @one_kwarg
    def push_and_aggree(self, **kwarg):
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
    def local_(self):
        raise NotImplementedError

    def global_(self):
        raise NotImplementedError

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
