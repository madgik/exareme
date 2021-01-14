from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import numpy as np
import pandas as pd
import scipy.stats

from mipframework import Algorithm
from mipframework import AlgorithmResult


class Anova(Algorithm):
    def __init__(self, cli_args):
        super(Anova, self).__init__(__file__, cli_args, intercept=False)

    def local_(self):
        X = self.data.full
        variable = self.parameters.y[0]
        covariables = self.parameters.x
        model = AdditiveAnoveModel(X, variable, covariables)

        self.push_and_add(model=model)

    def global_(self):
        model = self.fetch("model")

        df_explained = model.get_df_explained()
        df_residual = model.get_df_residual(df_explained)

        ss_residual = model.get_ss_residual()
        ss_explained = model.get_ss_explained(model.covariables[0])

        table = model.get_anova_table()

        self.result = AlgorithmResult(
            raw_data={
                "model": model.to_dict(),
                "df_explained": df_explained,
                "df_residual": df_residual,
                "ss_residual": ss_residual,
                "ss_explained": ss_explained,
                "table": table,
            },
            tables=[],
            highcharts=[],
        )


class AdditiveAnoveModel(object):
    def __init__(self, X=None, variable=None, covariables=None):
        if X is not None and variable and covariables:
            var_sq = variable + "_sq"
            X[var_sq] = X[variable] ** 2

            self.variable = variable
            self.covariables = covariables

            self.n_obs = X.shape[0]

            self.overall_stats = self.get_overall_stats(X, variable, var_sq)

            self.group_stats = self.get_group_stats(X, variable, var_sq, covariables)

    def __add__(self, other):
        result = AdditiveAnoveModel()

        assert self.variable == other.variable, "variable names do not agree"
        result.variable = self.variable

        assert self.covariables == other.covariables, "covariables names do not agree"
        result.covariables = self.covariables

        result.n_obs = self.n_obs + other.n_obs

        result.overall_stats = self.overall_stats + other.overall_stats

        result.group_stats = {
            k: v1.add(v2, fill_value=0)
            for (k, v1), (_, v2) in zip(
                self.group_stats.items(), other.group_stats.items()
            )
        }

        return result

    @staticmethod
    def get_overall_stats(X, variable, var_sq):
        overall_stats = X[variable].agg(["count", "sum"])
        overall_ssq = X[var_sq].sum()
        overall_stats = overall_stats.append(
            pd.Series(data=overall_ssq, index=["sum_sq"])
        )
        return overall_stats

    @staticmethod
    def get_group_stats(X, variable, var_sq, covariables):
        group_stats = dict()
        for cov in covariables:
            group_stat = X[[variable, cov]].groupby(cov).agg(["count", "sum"])
            group_stat.columns = ["count", "sum"]
            group_sum_sq = X[[var_sq, cov]].groupby(cov).agg(["sum"])
            group_sum_sq.columns = ["sum_sq"]
            group_stats[cov] = group_stat.join(group_sum_sq)
        return group_stats

    def get_df_explained(self):
        return {cov: len(group) - 1 for cov, group in self.group_stats.items()}

    def get_df_residual(self, df_explained):
        return {cov: self.n_obs - dfe for cov, dfe in df_explained.items()}

    def get_ss_residual(self):
        ss_residual = dict()
        for cov, group in self.group_stats.items():
            ssr = self.overall_stats["sum_sq"] - sum(group["sum"] ** 2 / group["count"])
            ss_residual[cov] = ssr
        return ss_residual

    def get_ss_total(self):
        return self.overall_stats["sum_sq"] - (
            self.overall_stats["sum"] ** 2 / self.overall_stats["count"]
        )

    def get_ss_explained(self, covariable):
        return sum(
            (
                self.overall_mean
                - self.group_stats[covariable]["sum"]
                / self.group_stats[covariable]["count"]
            )
            ** 2
            * self.group_stats[covariable]["count"]
        )

    def get_anova_table(self):
        df_explained = self.get_df_explained()
        df_residual = self.get_df_residual(df_explained)
        ss_explained = self.get_ss_explained(self.covariables[0])
        ss_residual = self.get_ss_residual()
        ms_explained = ss_explained / df_explained
        ms_residual = ss_residual / df_residual
        f_stat = ms_explained / ms_residual
        p_value = scipy.stats.f.cdf(f_stat, df_explained, df_residual)
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
    def overall_mean(self):
        return self.overall_stats["sum"] / self.overall_stats["count"]

    def to_dict(self):  # useful for debugging
        dd = {
            "variable": self.variable,
            "covariables": self.covariables,
            "n_obs": self.n_obs,
            "overall_stats": self.overall_stats.tolist(),
            "group_stats": {k: v.values.tolist() for k, v in self.group_stats.items()},
        }
        return dd


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
        "edsd",
        "-filter",
        "",
    ]
    runner = create_runner(Anova, algorithm_args=algorithm_args, num_workers=1,)
    start = time.time()
    runner.run()
    end = time.time()
