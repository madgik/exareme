from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from collections import Counter
import re

import numpy as np  # type: ignore    XXX needed for patsy to know how to take logs and exps
import pandas as pd
import patsy

from mipframework import Algorithm, AlgorithmResult
from mipframework.constants import PRIVACY_THRESHOLD
from mipframework.formula import generate_formula


class DescriptiveStats(Algorithm):
    def __init__(self, cli_args):
        super(DescriptiveStats, self).__init__(
            __file__, cli_args, intercept=False, privacy=False, dropna=False
        )

    def local_(self):
        is_categorical = self.metadata.is_categorical  # type: ignore
        var_names = self.parameters.var_names  # type: ignore
        datasets = self.parameters.dataset  # type: ignore
        data = self.data.full  # type: ignore
        labels = self.metadata.label  # type: ignore

        all_single_stats = MonoidMapping()
        for varname in var_names:
            for dataset in datasets:
                var_df = get_df_for_single_var(varname, data, dataset)
                stats = get_single_stats_monoid(var_df, is_categorical[varname])
                all_single_stats[varname, dataset] = stats
        self.push_and_add(all_single_stats=all_single_stats)

        if self.parameters.formula:  # type: ignore
            formula = generate_formula(formula_data=self.parameters.formula)  # type: ignore
            dummy = get_formula_transformed_data(formula, data)
            included_columns = [
                column for column in dummy.columns if column != "dataset"
            ]
            group_var_names = included_columns
            is_categorical = get_extended_iscategorical(
                is_categorical,
                included_columns,
            )
            labels = get_extended_labels(labels, included_columns)
        else:
            formula = None
            group_var_names = var_names

        all_group_stats = MonoidMapping()
        for dataset in datasets:
            data_group = data[data.dataset == dataset]
            for varname in group_var_names:
                if formula:
                    stats = get_model_stats_monoid_from_formula(
                        varname,
                        data_group,
                        is_categorical[varname],
                        formula,
                    )
                else:
                    stats = get_model_stats_monoid(
                        varname,
                        data_group,
                        is_categorical[varname],
                    )
                all_group_stats[dataset, varname] = stats
        self.push_and_add(all_group_stats=all_group_stats)
        self.push_and_agree(var_names=var_names)
        self.push_and_agree(is_categorical=is_categorical)
        self.push_and_agree(labels=labels)

    def global_(self):
        var_names = self.fetch("var_names")
        is_categorical = self.fetch("is_categorical")
        labels = self.fetch("labels")
        datasets = self.parameters.dataset  # type: ignore

        raw_out = init_raw_out([labels[var] for var in var_names], datasets)

        single_out = raw_out["single"]
        all_single_stats = self.fetch("all_single_stats")
        for (varname, dataset), single_stats in all_single_stats.items():
            current_out = single_out[labels[varname]][dataset]
            current_out["num_datapoints"] = single_stats.n_obs
            current_out["num_nulls"] = single_stats.n_nulls
            current_out["num_total"] = single_stats.n_obs + single_stats.n_nulls
            if not single_stats.enough_data:
                current_out["data"] = "NOT ENOUGH DATA"
                continue
            if is_categorical[varname]:
                current_out["data"] = get_counts_and_percentages(single_stats.counter)
            else:
                current_out["data"] = {
                    "mean": round(single_stats.mean, 2),
                    "std": round(single_stats.std, 2),
                    "min": round(single_stats.min_, 2),
                    "max": round(single_stats.max_, 2),
                }

        group_out = raw_out["model"]
        all_group_stats = self.fetch("all_group_stats")
        for (dataset, varname), group_stats in all_group_stats.items():
            current_out = group_out[dataset]
            group_stats = all_group_stats[dataset, varname]
            current_out["num_datapoints"] = group_stats.n_obs
            current_out["num_nulls"] = group_stats.n_nulls
            current_out["num_total"] = group_stats.n_obs + group_stats.n_nulls
            if not group_stats.enough_data:
                current_out["data"][labels[varname]] = "NOT ENOUGH DATA"
                continue
            if is_categorical[varname]:
                current_out["data"][labels[varname]] = get_counts_and_percentages(
                    group_stats.counter
                )
            else:
                current_out["data"][labels[varname]] = {
                    "mean": round(group_stats.mean, 2),
                    "std": round(group_stats.std, 2),
                    "min": round(group_stats.min_, 2),
                    "max": round(group_stats.max_, 2),
                }

        self.result = AlgorithmResult(raw_data=raw_out)


def get_formula_transformed_data(formula, data):
    processed_data = patsy.dmatrix(
        formula,
        data,
        return_type="dataframe",
    )
    del processed_data["Intercept"]
    return processed_data


def init_raw_out(varnames, datasets):
    raw_out = dict()

    raw_out["single"] = dict()
    for varname in varnames:
        raw_out["single"][varname] = dict()
        for dataset in datasets:
            raw_out["single"][varname][dataset] = dict()

    raw_out["model"] = dict()
    for dataset in datasets:
        raw_out["model"][dataset] = dict()
        raw_out["model"][dataset]["data"] = dict()
    return raw_out


def get_df_for_single_var(var_name, df, dataset):
    if var_name != "dataset":
        varlst = [var_name, "dataset"]
    else:
        varlst = [var_name]
    df = df[varlst]
    df = df[df.dataset == dataset]
    df = df[var_name]
    return df


def get_extended_labels(labels, new_columns):
    labels = dict(labels)
    for column in new_columns:
        if column not in labels:
            labels[column] = column
    formatted_labels = {}
    for column, label in labels.items():
        # Format id labels
        label = re.sub(r"I(\([^()]+\))", r"\g<1>", label)
        # Format numpy labels
        label = re.sub(r"np.(exp|log)(.+)", r"\g<1>\g<2>", label)
        # Format patsy labels
        label = re.sub(r"patsy.(center|standardize)(.+)", r"\g<1>\g<2>", label)
        formatted_labels[column] = label
    return formatted_labels


def get_extended_iscategorical(is_categorical, new_columns):
    is_categorical = dict(is_categorical)
    for column in new_columns:
        if column not in is_categorical:
            is_categorical[column] = 0
    return is_categorical


class NumericalVarStats(object):
    def __init__(self, n_obs, n_nulls, sx, sxx, min_, max_):
        self.n_obs = n_obs
        self.n_nulls = n_nulls
        self.enough_data = n_obs >= PRIVACY_THRESHOLD
        self.sx = sx if self.enough_data else 0
        self.sxx = sxx if self.enough_data else 0
        self.min_ = min_ if self.enough_data else int(1e9)
        self.max_ = max_ if self.enough_data else -int(1e9)

    @property
    def mean(self):
        return self.sx / self.n_obs

    @property
    def std(self):
        return ((self.sxx - self.n_obs * (self.mean ** 2)) / (self.n_obs - 1)) ** 0.5

    @property
    def upper_ci(self):
        return self.mean + self.std

    @property
    def lower_ci(self):
        return self.mean - self.std

    def __add__(self, other):
        return NumericalVarStats(
            n_obs=self.n_obs + other.n_obs,
            n_nulls=self.n_nulls + other.n_nulls,
            sx=self.sx + other.sx,
            sxx=self.sxx + other.sxx,
            min_=min(self.min_, other.min_),
            max_=max(self.max_, other.max_),
        )


class CategoricalVarStats(object):
    def __init__(self, n_obs, n_nulls, counter):
        self.n_obs = n_obs
        self.n_nulls = n_nulls
        self.enough_data = n_obs >= PRIVACY_THRESHOLD
        self.counter = counter if self.enough_data else Counter()

    def __add__(self, other):
        return CategoricalVarStats(
            n_obs=self.n_obs + other.n_obs,
            n_nulls=self.n_nulls + other.n_nulls,
            counter=self.counter + other.counter,
        )


def get_single_stats_monoid(df, is_categorical):
    n_tot = len(df)
    df = df.dropna()
    n_obs = len(df)
    n_nulls = n_tot - n_obs
    if is_categorical:
        return get_categorical_stats_monoid(df, n_obs, n_nulls)
    return get_numerical_stats_monoid(df, n_obs, n_nulls)


def get_numerical_stats_monoid(df, n_obs, n_nulls):
    sx = df.sum()
    sxx = (df * df).sum()
    min_ = df.min()
    max_ = df.max()
    return NumericalVarStats(n_obs, n_nulls, sx, sxx, min_, max_)


def get_categorical_stats_monoid(df, n_obs, n_nulls):
    counter = Counter(df)
    return CategoricalVarStats(n_obs, n_nulls, counter)


def get_model_stats_monoid_from_formula(
    varname,
    data_group,
    is_categorical,
    formula,
):
    n_tot = len(data_group)
    data_group = data_group.dropna()
    data_group = get_formula_transformed_data(formula, data_group)
    n_obs = len(data_group)
    n_nulls = n_tot - n_obs
    df = data_group[varname]
    if is_categorical:
        return get_categorical_stats_monoid(df, n_obs, n_nulls)
    return get_numerical_stats_monoid(df, n_obs, n_nulls)


def get_model_stats_monoid(varname, data_group, is_categorical):
    n_tot = len(data_group)
    data_group = data_group.dropna()
    n_obs = len(data_group)
    n_nulls = n_tot - n_obs
    df = data_group[varname]
    if is_categorical:
        return get_categorical_stats_monoid(df, n_obs, n_nulls)
    return get_numerical_stats_monoid(df, n_obs, n_nulls)


class MonoidMapping(dict):
    def __add__(self, other):
        all_keys = set(self.keys()) | set(other.keys())
        result = {}
        for key in all_keys:
            if key in self and key in other:
                result[key] = self[key] + other[key]
            elif key in self and key not in other:
                result[key] = self[key]
            else:
                result[key] = other[key]
        return MonoidMapping(result)


def get_counts_and_percentages(counter):
    if isinstance(counter, pd.Series):
        counter = {key: counter[key] for key in counter.index}
    total = sum(counter.values())
    return {
        key: {"count": value, "percentage": round(100 * value / total, ndigits=2)}
        for key, value in counter.items()
    }
