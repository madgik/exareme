from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import itertools

import numpy as np
import pandas as pd
import scipy.stats

from mipframework import Algorithm
from mipframework import AlgorithmResult
from mipframework import TabularDataResource
from mipframework.highcharts import LineWithErrorbars
from utils.algorithm_utils import ExaremeError


class Anova(Algorithm):
    def __init__(self, cli_args):
        super(Anova, self).__init__(__file__, cli_args, intercept=False)

    def local_(self):
        X = self.data.full
        variable = self.parameters.y[0]
        covariable = self.parameters.x[0]
        var_label = self.metadata.label[variable]
        covar_label = self.metadata.label[covariable]
        covar_enums = self.metadata.enumerations[covariable]

        model = AdditiveAnovaModel(X, variable, covariable)

        self.push_and_add(model=model)
        self.push_and_agree(var_label=var_label)
        self.push_and_agree(covar_label=covar_label)
        self.push_and_agree(covar_enums=covar_enums)

    def global_(self):
        model = self.fetch("model")
        var_label = self.fetch("var_label")
        covar_label = self.fetch("covar_label")
        covar_enums = self.fetch("covar_enums")

        if len(model.group_stats) < 2:
            raise ExaremeError("Cannot perform Anova when there is only one level")

        res = model.get_anova_table()

        anova_table = TabularDataResource(
            fields=["", "df", "sum sq", "mean sq", "F value", "Pr(>F)"],
            data=[
                [
                    covar_label,
                    res["df_explained"],
                    res["ss_explained"],
                    res["ms_explained"],
                    res["f_stat"],
                    res["p_value"],
                ],
                [
                    "Residual",
                    res["df_residual"],
                    res["ss_residual"],
                    res["ms_residual"],
                    None,
                    None,
                ],
            ],
            title="Anova Summary",
        )

        tuckey_data = pairwise_tuckey(model, covar_enums)
        tuckey_hsd_table = TabularDataResource(
            fields=list(tuckey_data.columns),
            data=list([list(row) for row in tuckey_data.values]),
            title="Tuckey Honest Significant Differences",
        )

        mean_plot = create_mean_plot(
            model.group_stats, var_label, covar_label, covar_enums
        )

        self.result = AlgorithmResult(
            raw_data={"anova_table": res},
            tables=[anova_table, tuckey_hsd_table],
            highcharts=[mean_plot],
        )


class AdditiveAnovaModel(object):
    def __init__(self, X=None, variable=None, covariable=None):
        self._table = None
        if X is not None and variable and covariable:
            self.variable = variable
            self.covariable = covariable
            self.var_sq = variable + "_sq"
            X[self.var_sq] = X[variable] ** 2

            self.n_obs = X.shape[0]

            self.overall_stats = self.get_overall_stats(X)

            self.group_stats = self.get_group_stats(X)

    def __add__(self, other):
        result = AdditiveAnovaModel()

        assert self.variable == other.variable, "variable names do not agree"
        result.variable = self.variable

        assert self.covariable == other.covariable, "covariable names do not agree"
        result.covariable = self.covariable

        result.n_obs = self.n_obs + other.n_obs

        result.overall_stats = self.overall_stats + other.overall_stats

        result.group_stats = self.group_stats.add(other.group_stats, fill_value=0)

        return result

    def get_overall_stats(self, X):
        variable = self.variable
        var_sq = self.var_sq
        overall_stats = X[variable].agg(["count", "sum"])
        overall_ssq = X[var_sq].sum()
        overall_stats = overall_stats.append(
            pd.Series(data=overall_ssq, index=["sum_sq"])
        )
        return overall_stats

    def get_group_stats(self, X):
        variable = self.variable
        covar = self.covariable
        var_sq = self.var_sq
        group_stats = X[[variable, covar]].groupby(covar).agg(["count", "sum"])
        group_stats.columns = ["count", "sum"]
        group_ssq = X[[var_sq, covar]].groupby(covar).sum()
        group_ssq.columns = ["sum_sq"]
        group_stats = group_stats.join(group_ssq)
        return group_stats

    def get_df_explained(self):
        return len(self.group_stats) - 1

    def get_df_residual(self):
        return self.n_obs - len(self.group_stats)

    def get_ss_residual(self):
        overall_sum_sq = self.overall_stats["sum_sq"]
        group_sum = self.group_stats["sum"]
        group_count = self.group_stats["count"]
        return overall_sum_sq - sum(group_sum ** 2 / group_count)

    def get_ss_total(self):
        overall_sum_sq = self.overall_stats["sum_sq"]
        overall_sum = self.overall_stats["sum"]
        overall_count = self.overall_stats["count"]
        return overall_sum_sq - (overall_sum ** 2 / overall_count)

    def get_ss_explained(self):
        group_sum = self.group_stats["sum"]
        group_count = self.group_stats["count"]
        return sum((self.overall_mean - group_sum / group_count) ** 2 * group_count)

    def get_anova_table(self):
        df_explained = self.get_df_explained()
        df_residual = self.get_df_residual()
        ss_explained = self.get_ss_explained()
        ss_residual = self.get_ss_residual()
        ms_explained = ss_explained / df_explained
        ms_residual = ss_residual / df_residual
        f_stat = ms_explained / ms_residual
        p_value = 1 - scipy.stats.f.cdf(f_stat, df_explained, df_residual)
        return dict(
            df_explained=df_explained,
            df_residual=df_residual,
            ss_explained=ss_explained,
            ss_residual=ss_residual,
            ms_explained=ms_explained,
            ms_residual=ms_residual,
            f_stat=f_stat,
            p_value=p_value,
        )

    @property
    def table(self):
        if self._table is None:
            table = pd.DataFrame(
                columns=["df", "sum_sq", "mean_sq", "F", "PR(>F)"],
                index=[self.covariable, "Residual"],
            )
            df_explained = self.get_df_explained()
            df_residual = self.get_df_residual()
            ss_explained = self.get_ss_explained()
            ss_residual = self.get_ss_residual()
            ms_explained = ss_explained / df_explained
            ms_residual = ss_residual / df_residual
            f_stat = ms_explained / ms_residual
            p_value = 1 - scipy.stats.f.cdf(f_stat, df_explained, df_residual)
            table.loc[self.covariable] = {
                "df": df_explained,
                "sum_sq": ss_explained,
                "mean_sq": ms_explained,
                "F": f_stat,
                "PR(>F)": p_value,
            }
            table.loc["Residual"] = {
                "df": df_residual,
                "sum_sq": ss_residual,
                "mean_sq": ms_residual,
                "F": None,
                "PR(>F)": None,
            }
            self._table = table
            return table

        return self._table

    @property
    def overall_mean(self):
        return self.overall_stats["sum"] / self.overall_stats["count"]

    def to_dict(self):  # useful for debugging
        dd = {
            "variable": self.variable,
            "covariable": self.covariable,
            "n_obs": self.n_obs,
            "overall_stats": self.overall_stats.tolist(),
            "group_stats": self.group_stats.values.tolist(),
        }
        return dd


def create_mean_plot(group_stats, variable, covariable, categories):
    title = "Means plot: {v} ~ {c}".format(v=variable, c=covariable)
    means = group_stats["sum"] / group_stats["count"]
    variances = group_stats["sum_sq"] / group_stats["count"] - means ** 2
    sample_vars = (group_stats["count"] - 1) / group_stats["count"] * variances
    sample_stds = np.sqrt(sample_vars)

    categories = [c for c in categories if c in group_stats.index]
    means = [means[cat] for cat in categories]
    sample_stds = [sample_stds[cat] for cat in categories]
    data = [[m - s, m, m + s] for m, s in zip(means, sample_stds)]
    return LineWithErrorbars(
        title=title,
        data=data,
        categories=categories,
        xname=covariable,
        yname="95% CI: " + variable,
    )


def pairwise_tuckey(aov, categories):
    categories = np.array([c for c in categories if c in aov.group_stats.index])
    n_groups = len(categories)
    gnobs = aov.group_stats["count"].to_numpy()
    gmeans = (aov.group_stats["sum"] / aov.group_stats["count"]).to_numpy()
    gvar = aov.table.at[aov.covariable, "mean_sq"] / gnobs
    g1, g2 = np.array(list(itertools.combinations(np.arange(n_groups), 2))).T
    mn = gmeans[g1] - gmeans[g2]
    se = np.sqrt(gvar[g1] + gvar[g2])
    tval = mn / se
    pval = scipy.stats.t.sf(np.abs(tval), gnobs[g1].size + gnobs[g2].size - 2) * 2
    thsd = pd.DataFrame(
        columns=[
            "A",
            "B",
            "mean(A)",
            "mean(B)",
            "diff",
            "Std.Err.",
            "t value",
            "Pr(>|t|)",
        ],
        index=range(n_groups * (n_groups - 1) // 2),
    )
    thsd["A"] = categories[g1]
    thsd["B"] = categories[g2]
    thsd["mean(A)"] = gmeans[g1]
    thsd["mean(B)"] = gmeans[g2]
    thsd["diff"] = mn
    thsd["Std.Err."] = se
    thsd["t value"] = tval
    thsd["Pr(>|t|)"] = pval
    return thsd


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-y",
        "lefthippocampus",
        "-x",
        "alzheimerbroadcategory",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        "",
    ]
    runner = create_runner(Anova, algorithm_args=algorithm_args, num_workers=3,)
    start = time.time()
    runner.run()
    end = time.time()
