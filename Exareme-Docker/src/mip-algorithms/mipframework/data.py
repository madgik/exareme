import re

import numpy as np
import pandas as pd
import patsy
from sqlalchemy import between, not_, and_, or_, Table, select, create_engine, MetaData

from mipframework.constants import PRIVACY_THRESHOLD
from mipframework.loggingutils import log_this, repr_with_logging, logged
from mipframework.exceptions import PrivacyError

FILTER_OPERATORS = {
    "equal": lambda a, b: a == b,
    "not_equal": lambda a, b: a != b,
    "less": lambda a, b: a < b,
    "greater": lambda a, b: a > b,
    "less_or_equal": lambda a, b: a <= b,
    "greater_or_equal": lambda a, b: a >= b,
    "between": lambda a, b: between(a, b[0], b[1]),
    "not_between": lambda a, b: not_(between(a, b[0], b[1])),
}
FILTER_CONDITIONS = {
    "AND": lambda *a: and_(*a),
    "OR": lambda *a: or_(*a),
}


class AlgorithmData(object):
    def __init__(self, args):
        log_this("AlgorithmData.__init__", args=args)
        db = DataBase(
            db_path=args.input_local_DB,
            data_table_name=args.data_table,
            metadata_table_name=args.metadata_table,
            privacy=args.privacy,
            dropna=args.dropna,
        )
        self.db = db
        self.full = db.read_data_from_db(args)
        self.metadata = db.read_metadata_from_db(args)
        variables, covariables = self.build_variables(args)
        if 1 in self.metadata.is_categorical.values():
            variables = self.add_missing_levels(args.y, args.coding, variables)
            if covariables is not None:  # truth value of dataframe is ambiguous
                covariables = self.add_missing_levels(args.x, args.coding, covariables)
        self.variables, self.covariables = variables, covariables

    def __repr__(self):
        repr_with_logging(self, variables=self.variables, covariables=self.covariables)

    def build_variables(self, args):
        log_this("AlgorithmData.build_variables", args=args)

        from numpy import log as log
        from numpy import exp as exp

        # This line is needed to prevent import optimizer from removing above lines
        _ = log(exp(1))
        formula = self.get_formula(args)
        if args.formula_is_equation:
            if self.full.dropna().shape[0] == 0:
                return pd.DataFrame(), pd.DataFrame()
            variables, covariables = patsy.dmatrices(
                formula, self.full, return_type="dataframe"
            )
        else:
            if self.full.dropna().shape[0] == 0:
                return pd.DataFrame(), None
            variables = patsy.dmatrix(formula, self.full, return_type="dataframe")
            covariables = None
        return variables, covariables

    def add_missing_levels(self, varnames, coding, dmatrix):
        log_this(
            "AlgorithmData.add_missing_levels",
            varnames=varnames,
            coding=coding,
            dmatrix=dmatrix.columns,
        )
        categorical_variables = (
            var for var in varnames if self.metadata.is_categorical[var]
        )
        all_var_levels = (
            "C({var}, {coding})[{level}]".format(var=var, coding=coding, level=level)
            for var in categorical_variables
            for level in self.metadata.enumerations[var]
        )
        for var_level in all_var_levels:
            if var_level in dmatrix.columns:
                continue
            missing_column = pd.Series(np.zeros((len(dmatrix),)), index=dmatrix.index)
            dmatrix[var_level] = missing_column
        return dmatrix

    def get_formula(self, args):
        log_this("AlgorithmData.add_missing_levels", args=args)
        is_categorical = self.metadata.is_categorical
        # Get formula from args or build if doesn't exist
        if hasattr(args, "formula") and args.formula:
            formula = args.formula
        else:
            if hasattr(args, "x") and args.x:
                formula = "+".join(args.y) + "~" + "+".join(args.x)
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
                    formula = re.sub(
                        r"\b({})\b".format(var),
                        r"C(\g<0>, {})".format(args.coding),
                        formula,
                    )
        return formula


class AlgorithmMetadata(object):
    def __init__(self, label, is_categorical, enumerations, minmax):
        self.label = label
        self.is_categorical = is_categorical
        self.enumerations = enumerations
        self.minmax = minmax

    def __repr__(self):
        name = type(self).__name__
        return "{name}()".format(name=name)


class DataBase(object):
    def __init__(
        self, db_path, data_table_name, metadata_table_name, privacy=True, dropna=True
    ):
        self.db_path = db_path
        self.engine = create_engine("sqlite:///{}".format(self.db_path), echo=False)
        self.sqla_md = MetaData(self.engine)
        self.data_table = self.create_table(data_table_name)
        self.metadata_table = self.create_table(metadata_table_name)
        self.privacy = privacy
        self.dropna = dropna

    def __repr__(self):
        name = type(self).__name__
        return "{name}()".format(name=name)

    @logged
    def create_table(self, table_name):
        return Table(table_name, self.sqla_md, autoload=True)

    @logged
    def read_data_from_db(self, args):
        var_names = list(args.y) + ["dataset"]
        if hasattr(args, "x") and args.x:
            var_names.extend(args.x)
        data = self.select_vars_from_data(
            var_names=var_names, datasets=args.dataset, filter_rules=args.filter
        )
        return data

    @logged
    def read_longitudinal_data_from_db(self, args):
        var_names = list(args.y) + [
            "subjectcode",
            "subjectvisitdate",
            "subjectage",
            "subjectvisitid",
            "dataset",
        ]
        if hasattr(args, "x") and args.x:
            var_names.extend(args.x)
        data = self.select_vars_from_data(
            var_names=var_names, datasets=args.dataset, filter_rules=args.filter
        )
        return data

    @logged
    def read_metadata_from_db(self, args):
        var_names = list(args.y)
        if hasattr(args, "x") and args.x:
            var_names.extend(args.x)
        md = self.select_md_from_metadata(var_names=var_names, args=args)
        return AlgorithmMetadata(*md)

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
        if self.dropna:
            data = data.dropna()
        # Privacy check
        if self.privacy and len(data) < PRIVACY_THRESHOLD:
            raise PrivacyError
        return data

    @logged
    def build_filter_clause(self, rules):
        if "condition" in rules:
            cond = FILTER_CONDITIONS[rules["condition"]]
            rules = rules["rules"]
            return cond(*[self.build_filter_clause(rule) for rule in rules])
        elif "id" in rules:
            var_name = rules["id"]
            op = FILTER_OPERATORS[rules["operator"]]
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
