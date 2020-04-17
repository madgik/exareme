import logging
import re

import numpy as np
import pandas as pd
from patsy import PatsyError, dmatrix, dmatrices
from sqlalchemy import between, not_, and_, or_, Table, select, create_engine, MetaData
from sqlalchemy.exc import SQLAlchemyError

from . import logged
from .constants import LOGGING_LEVEL_ALG
from .exceptions import PrivacyError

_PRIVACY_THRESHOLD = 0
_FILTER_OPERATORS = {
    "equal": lambda a, b: a == b,
    "not_equal": lambda a, b: a != b,
    "less": lambda a, b: a < b,
    "greater": lambda a, b: a > b,
    "less_or_equal": lambda a, b: a <= b,
    "greater_or_equal": lambda a, b: a >= b,
    "between": lambda a, b: between(a, b[0], b[1]),
    "not_between": lambda a, b: not_(between(a, b[0], b[1])),
}
_FILTER_CONDS = {
    "AND": lambda *a: and_(*a),
    "OR": lambda *a: or_(*a),
}


class AlgorithmData(object):
    def __init__(self, args):
        db = DataBase(
            db_path=args.input_local_DB,
            data_table_name=args.data_table,
            metadata_table_name=args.metadata_table,
        )
        self.full = read_data_from_db(db, args)
        self.metadata = read_metadata_from_db(db, args)
        self.variables, self.covariables = self.build_vars_and_covars(
            args, self.metadata.is_categorical
        )

    def __repr__(self):
        if LOGGING_LEVEL_ALG == logging.INFO:
            return "AlgorithmData()"
        elif LOGGING_LEVEL_ALG == logging.DEBUG:
            return "AlgorithmData(\nvariables:={var},\ncovariables:={covar},\n)".format(
                var=self.variables, covar=self.covariables
            )

    def build_vars_and_covars(self, args, is_categorical):
        # This one CANNOT be `logged` since it runs on __init__
        if LOGGING_LEVEL_ALG == logging.INFO:
            logging.info("Starting 'AlgorithmData.build_vars_and_covars'.")
        elif LOGGING_LEVEL_ALG == logging.DEBUG:
            logging.debug(
                "Starting 'AlgorithmData.build_vars_and_covars',"
                "\nargs: \n{0},\nkwargs: \n{1}".format(args, is_categorical)
            )
        from numpy import log as log
        from numpy import exp as exp

        # This line is needed to prevent import opimizer from removing above lines
        _ = log(exp(1))
        formula = get_formula(args, is_categorical)
        # Create variables (and possibly covariables)
        if hasattr(args, "x") and args.x:
            try:
                variables, covariables = dmatrices(
                    formula, self.full, return_type="dataframe"
                )
            except (NameError, PatsyError) as e:
                logging.error(
                    "Patsy failed to get variables and covariables from formula."
                )
                raise e
            return variables, covariables
        else:
            try:
                variables = dmatrix(formula, self.full, return_type="dataframe")
            except (NameError, PatsyError) as e:
                logging.error("Patsy failed to get variables from formula.")
                raise e
            return variables, None


@logged
def get_formula(args, is_categorical):
    # Get formula from args or build if doesn't exist
    if hasattr(args, "formula"):
        formula = args.formula
    else:
        formula = None
    if not formula:
        if hasattr(args, "x") and args.x:
            var_tup = (args.y, args.x)
            formula = "~".join(map(lambda x: "+".join(x), var_tup))
            if not args.intercept:
                formula += "-1"
        else:
            formula = "+".join(args.y) + "-1"
    # Process categorical vars
    var_names = list(args.y)
    if hasattr(args, "x") and args.x:
        var_names.extend(args.x)
    if 1 in is_categorical.values():
        if not hasattr(args, "coding") or not args.coding:
            args.coding = "Treatment"
        for var in var_names:
            if is_categorical[var]:
                formula = formula.replace(
                    var, "C({v}, {coding})".format(v=var, coding=args.coding)
                )
    return formula


class AlgorithmMetadata(object):
    def __init__(self, label, is_categorical, enumerations, minmax):
        self.label = label
        self.is_categorical = is_categorical
        self.enumerations = enumerations
        self.minmax = minmax


@logged
def read_data_from_db(db, args):
    var_names = list(args.y)
    if hasattr(args, "x") and args.x:
        var_names.extend(args.x)
    data = db.select_vars_from_data(
        var_names=var_names, datasets=args.dataset, filter_rules=args.filter
    )
    return data


@logged
def read_metadata_from_db(db, args):
    var_names = list(args.y)
    if hasattr(args, "x") and args.x:
        var_names.extend(args.x)
    md = db.select_md_from_metadata(var_names=var_names, args=args)
    return AlgorithmMetadata(*md)


class DataBase(object):
    def __init__(self, db_path, data_table_name, metadata_table_name):
        self.db_path = db_path
        try:
            self.engine = create_engine("sqlite:///{}".format(self.db_path), echo=False)
        except SQLAlchemyError as e:
            logging.error("SQLAlchemy failed to connect to database.")
            raise e
        try:
            self.sqla_md = MetaData(self.engine)
        except SQLAlchemyError as e:
            logging.error("SQLAlchemy failed to build MetaData.")
            raise e
        try:
            self.data_table = self.create_table(data_table_name)
        except SQLAlchemyError as e:
            logging.error("SQLAlchemy failed to build data_table.")
            raise e
        try:
            self.metadata_table = self.create_table(metadata_table_name)
        except SQLAlchemyError as e:
            logging.error("SQLAlchemy failed to build metadata_table.")
            raise e

    @logged
    def create_table(self, table_name):
        return Table(table_name, self.sqla_md, autoload=True)

    @logged
    def select_vars_from_data(self, var_names, datasets, filter_rules):
        dataset_clause = or_(*[self.data_table.c.dataset == ds for ds in datasets])
        sel_stmt = select([self.data_table.c[var] for var in var_names]).where(
            dataset_clause
        )
        if filter_rules:
            filter_clause = self.build_filter_clause(filter_rules)
            sel_stmt = sel_stmt.where(filter_clause)
        data = pd.read_sql(sel_stmt, self.engine)
        data.replace("", np.nan, inplace=True)  # fixme remove
        data = data.dropna()
        # Privacy check
        if len(data) < _PRIVACY_THRESHOLD:
            raise PrivacyError
        return data

    @logged
    def build_filter_clause(self, rules):
        if "condition" in rules:
            cond = _FILTER_CONDS[rules["condition"]]
            rules = rules["rules"]
            return cond(*[self.build_filter_clause(rule) for rule in rules])
        elif "id" in rules:
            var_name = rules["id"]
            op = _FILTER_OPERATORS[rules["operator"]]
            val = rules["value"]
            return op(self.data_table.c[var_name], val)

    @logged
    def select_md_from_metadata(self, var_names, args):
        code_name = args.metadata_code_column
        label_name = args.metadata_label_column
        iscat_name = args.metadata_isCategorical_column
        enums_name = args.metadata_enumerations_column
        min_name = args.metadata_minValue_column
        max_name = args.metadata_maxValue_column
        label = dict()
        is_categorical = dict()
        enumerations = dict()
        minmax = dict()
        sel_stmt = select(
            [
                self.metadata_table.c[code_name],
                self.metadata_table.c[label_name],
                self.metadata_table.c[iscat_name],
                self.metadata_table.c[enums_name],
                self.metadata_table.c[min_name],
                self.metadata_table.c[max_name],
            ]
        ).where(or_(*[self.metadata_table.c[code_name] == vn for vn in var_names]))
        result = self.engine.execute(sel_stmt)
        for vn, la, ic, en, mi, ma in result:
            label[vn] = la
            is_categorical[vn] = ic
            if en:
                enumerations[vn] = re.split(r"\s*,\s*", en)
            minmax[vn] = (mi, ma)
        return label, is_categorical, enumerations, minmax
