from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from itertools import ifilterfalse, ifilter
from collections import Counter

from mipframework import Algorithm, AlgorithmResult
from mipframework.constants import PRIVACY_THRESHOLD


class DescriptiveStats(Algorithm):
    def __init__(self, cli_args):
        super(DescriptiveStats, self).__init__(
            __file__, cli_args, intercept=False, privacy=False
        )

    def local_(self):
        numericals = list(
            ifilterfalse(
                lambda var: self.metadata.is_categorical[var], self.parameters.y
            )
        )
        categoricals = list(
            ifilter(lambda var: self.metadata.is_categorical[var], self.parameters.y)
        )
        self.push_and_agree(numericals=numericals)
        self.push_and_agree(categoricals=categoricals)
        self.push_and_agree(labels=self.metadata.label)
        # Single variables
        var_names = self.parameters.var_names
        datasets = self.parameters.dataset
        filter_ = self.parameters.filter
        single_vars = {
            vname: self.data.db.select_vars_from_data(
                [vname, "dataset"], datasets, filter_
            )
            for vname in var_names
        }
        for vname, var in single_vars.items():
            for dataset in datasets:
                var_subgroup = var[var.dataset == dataset]
                n_obs = len(var_subgroup)
                self.push_and_add(
                    **{"single__" + "n_obs_" + vname + "_" + dataset: n_obs}
                )
                if vname in numericals:
                    numvar = var_subgroup[vname]
                    sx = numvar.sum()
                    self.push_and_add(
                        **{"single__" + "sx_" + vname + "_" + dataset: sx}
                    )
                    sxx = (numvar * numvar).sum()
                    self.push_and_add(
                        **{"single__" + "sxx_" + vname + "_" + dataset: sxx}
                    )
                    min_ = numvar.min()
                    self.push_and_min(
                        **{"single__" + "min_" + vname + "_" + dataset: min_}
                    )
                    max_ = numvar.max()
                    self.push_and_max(
                        **{"single__" + "max_" + vname + "_" + dataset: max_}
                    )
                elif vname in categoricals:
                    counter = Counter(var_subgroup[vname])
                    self.push_and_add(
                        **{"single__" + "counter_" + vname + "_" + dataset: counter}
                    )
        # Set of variables
        data = self.data.full
        for dataset in datasets:
            data_group = data[data.dataset == dataset]
            n_obs = len(data_group)
            self.push_and_add(**{"model__" + "n_obs_" + dataset: n_obs})
            if n_obs <= PRIVACY_THRESHOLD:
                continue
            for numerical in numericals:
                numvar = data_group[numerical]
                sx = numvar.sum()
                self.push_and_add(**{"model__" + "sx_" + numerical + "_" + dataset: sx})
                sxx = (numvar * numvar).sum()
                self.push_and_add(
                    **{"model__" + "sxx_" + numerical + "_" + dataset: sxx}
                )
                min_ = numvar.min()
                self.push_and_min(
                    **{"model__" + "min_" + numerical + "_" + dataset: min_}
                )
                max_ = numvar.max()
                self.push_and_max(
                    **{"model__" + "max_" + numerical + "_" + dataset: max_}
                )
            for categorical in categoricals:
                counter = Counter(data_group[categorical])
                self.push_and_add(
                    **{"model__" + "counter_" + categorical + "_" + dataset: counter}
                )

    def global_(self):
        numericals = self.fetch("numericals")
        categoricals = self.fetch("categoricals")

        global fields
        raw_out = dict()
        datasets = self.parameters.dataset

        # Single variables
        raw_out["single"] = dict()
        for numerical in numericals:
            raw_out["single"][numerical] = dict()
            for dataset in datasets:
                raw_out["single"][numerical][dataset] = dict()
                n_obs = self.fetch("single__" + "n_obs_" + numerical + "_" + dataset)
                if n_obs <= PRIVACY_THRESHOLD:
                    raw_out["single"][numerical][dataset]["num_datapoints"] = n_obs
                    raw_out["single"][numerical][dataset]["data"] = "NOT ENOUGH DATA"
                else:
                    sx = self.fetch("single__" + "sx_" + numerical + "_" + dataset)
                    sxx = self.fetch("single__" + "sxx_" + numerical + "_" + dataset)
                    min_ = self.fetch("single__" + "min_" + numerical + "_" + dataset)
                    max_ = self.fetch("single__" + "max_" + numerical + "_" + dataset)
                    mean = sx / n_obs
                    std = ((sxx - n_obs * (mean ** 2)) / (n_obs - 1)) ** 0.5
                    upper_ci = mean + std
                    lower_ci = mean - std
                    raw_out["single"][numerical][dataset]["num_datapoints"] = n_obs
                    raw_out["single"][numerical][dataset]["data"] = {
                        "mean": mean,
                        "std": std,
                        "min": min_,
                        "max": max_,
                        "upper_confidence": upper_ci,
                        "lower_confidence": lower_ci,
                    }
        for categorical in categoricals:
            raw_out["single"][categorical] = dict()
            for dataset in datasets:
                raw_out["single"][categorical][dataset] = dict()
                n_obs = self.fetch("single__" + "n_obs_" + categorical + "_" + dataset)
                if n_obs <= PRIVACY_THRESHOLD:
                    raw_out["single"][categorical][dataset]["num_datapoints"] = n_obs
                    raw_out["single"][categorical][dataset]["data"] = "NOT ENOUGH DATA"
                else:
                    counter = self.fetch(
                        "single__" + "counter_" + categorical + "_" + dataset
                    )
                    raw_out["single"][categorical][dataset]["num_datapoints"] = n_obs
                    raw_out["single"][categorical][dataset]["data"] = dict(counter)

        # Model
        raw_out["model"] = dict()
        for dataset in datasets:
            n_obs = self.fetch("model__" + "n_obs_" + dataset)
            raw_out["model"][dataset] = dict()
            raw_out["model"][dataset]["data"] = dict()
            raw_out["model"][dataset]["num_datapoints"] = n_obs
            for numerical in numericals:
                if n_obs <= PRIVACY_THRESHOLD:
                    raw_out["model"][dataset]["data"] = "NOT ENOUGH DATA"
                    continue
                sx = self.fetch("model__" + "sx_" + numerical + "_" + dataset)
                sxx = self.fetch("model__" + "sxx_" + numerical + "_" + dataset)
                min_ = self.fetch("model__" + "min_" + numerical + "_" + dataset)
                max_ = self.fetch("model__" + "max_" + numerical + "_" + dataset)
                mean = sx / n_obs
                std = ((sxx - n_obs * (mean ** 2)) / (n_obs - 1)) ** 0.5
                upper_ci = mean + std
                lower_ci = mean - std
                raw_out["model"][dataset]["data"][numerical] = {
                    "mean": mean,
                    "std": std,
                    "min": min_,
                    "max": max_,
                    "upper_confidence": upper_ci,
                    "lower_confidence": lower_ci,
                }
            for categorical in categoricals:
                if n_obs <= PRIVACY_THRESHOLD:
                    raw_out["model"][dataset]["data"] = "NOT ENOUGH DATA"
                    continue
                counter = self.fetch(
                    "model__" + "counter_" + categorical + "_" + dataset
                )
                raw_out["model"][dataset]["data"][categorical] = dict(counter)
        self.result = AlgorithmResult(raw_data=raw_out)


fields = [
    "Label",
    "Count",
    "Min",
    "Max",
    "Mean",
    "Std.Err.",
    "Mean + Std.Err",
    "Mean - Std.Err",
    "Frequencies",
]


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-y",
        "ppmicategory",
        "-pathology",
        "dementia",
        "-dataset",
        "ppmi",
        "-filter",
        "",
    ]
    runner = create_runner(
        DescriptiveStats, algorithm_args=algorithm_args, num_workers=1,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
