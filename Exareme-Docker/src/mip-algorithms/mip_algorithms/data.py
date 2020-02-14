import logging

import numpy as np
import pandas as pd
from mip_algorithms import _LOGGING_LEVEL_ALG, logged
from mip_algorithms.exceptions import PrivacyError
from patsy import PatsyError, dmatrix, dmatrices
from sqlalchemy import between, not_, and_, or_, Table, select, create_engine, MetaData
from sqlalchemy.exc import SQLAlchemyError

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


class AlgorithmData(object):
    def __init__(self, args):
        data, self.is_categorical = read_data_from_db(args)
        self.variables, self.covariables = get_model_variables(args, data, self.is_categorical)

    def __repr__(self):
        if _LOGGING_LEVEL_ALG == logging.INFO:
            return 'AlgorithmData()'
        elif _LOGGING_LEVEL_ALG == logging.DEBUG:
            return 'AlgorithmData(\nvariables:={var},\ncovariables:={covar},\nis_categorical:={iscat}\n)'.format(
                    var=self.variables, covar=self.covariables, iscat=self.is_categorical)


@logged
def get_model_variables(args, data, is_categorical):
    from numpy import log as log
    from numpy import exp as exp
    _ = log(exp(1))  # This line is needed to prevent import opimizer from removing above lines
    # Get formula from args or build if doesn't exist
    if hasattr(args, 'formula'):
        formula = args.formula
    else:
        formula = None

    if formula:
        formula = formula.replace('_', '~')  # fixme
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
    if hasattr(args, 'coding'):
        if args.coding:
            for var in var_names:
                if is_categorical[var]:
                    formula = formula.replace(var, 'C({v}, {coding})'.format(v=var, coding=args.coding))
    # Create variables (and possibly covariables)
    if args.x:
        try:
            model_vars, model_covars = dmatrices(formula, data, return_type='dataframe')
        except (NameError, PatsyError) as e:
            logging.error('Patsy failed to get variables from formula.')
            raise e
        return model_vars, model_covars
    else:
        try:
            model_vars = dmatrix(formula, data, return_type='dataframe')
        except (NameError, PatsyError) as e:
            logging.error('Patsy failed to get variables from formula.')
            raise e
        return model_vars, None


@logged
def read_data_from_db(args):
    # Connect to db
    try:
        engine, sqla_md = connect_to_db(args.input_local_DB)
    except SQLAlchemyError as e:
        logging.error('SQLAlchemy failed to connect to database.')
        raise e
    # Create tables
    try:
        data_table = Table(args.data_table, sqla_md, autoload=True)
        metadata_table = Table(args.metadata_table, sqla_md, autoload=True)
    except SQLAlchemyError as e:
        logging.error('SQLAlchemy failed to autoload tables.')
        raise e
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
    sel_stmt = select([table.c[var] for var in var_names]).where(dataset_clause)
    if query_filter:
        filter_clause = build_filter_clause(table, query_filter)
        sel_stmt = sel_stmt.where(filter_clause)
    data = pd.read_sql(sel_stmt, engine)
    data.replace('', np.nan, inplace=True)  # todo remove when no empty str in dbs
    data = data.dropna()
    return data


@logged
def connect_to_db(db_path):
    engine = create_engine('sqlite:///{}'.format(db_path), echo=False)
    sqla_md = MetaData(engine)
    return engine, sqla_md