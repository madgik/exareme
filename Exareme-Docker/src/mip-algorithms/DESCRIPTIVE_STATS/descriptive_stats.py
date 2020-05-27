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
            __file__, cli_args, intercept=False, privacy=False, dropna=False
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
        df = self.data.full
        var_names = self.parameters.var_names
        datasets = self.parameters.dataset

        for var_name in var_names:
            for dataset in datasets:
                if var_name != "dataset":
                    varlst = [var_name, "dataset"]
                else:
                    varlst = [var_name]
                single_df = df[varlst]
                single_df = single_df.dropna()
                single_df = single_df[single_df.dataset == dataset]
                single_df = single_df[var_name]
                n_obs = len(single_df)
                kwarg = {"single__" + "n_obs_" + var_name + "_" + dataset: n_obs}
                self.push_and_add(**kwarg)
                if var_name in numericals:
                    X = single_df
                    if n_obs <= PRIVACY_THRESHOLD:
                        sx, sxx, min_, max_ = 0, 0, int(1e9), -int(1e9)
                    else:
                        sx = X.sum()
                        sxx = (X * X).sum()
                        min_ = X.min()
                        max_ = X.max()
                    kwarg = {"single__" + "sx_" + var_name + "_" + dataset: sx}
                    self.push_and_add(**kwarg)
                    kwarg = {"single__" + "sxx_" + var_name + "_" + dataset: sxx}
                    self.push_and_add(**kwarg)
                    kwarg = {"single__" + "min_" + var_name + "_" + dataset: min_}
                    self.push_and_min(**kwarg)
                    kwarg = {"single__" + "max_" + var_name + "_" + dataset: max_}
                    self.push_and_max(**kwarg)
                elif var_name in categoricals:
                    if n_obs <= PRIVACY_THRESHOLD:
                        counter = Counter()
                    else:
                        counter = Counter(single_df)
                    kwarg = {
                        "single__" + "counter_" + var_name + "_" + dataset: counter
                    }
                    self.push_and_add(**kwarg)

        # Set of variables
        data = self.data.full.dropna()
        for dataset in datasets:
            data_group = data[data.dataset == dataset]
            n_obs = len(data_group)
            self.push_and_add(**{"model__" + "n_obs_" + dataset: n_obs})
            for var in numericals + categoricals:
                if var in numericals:
                    numerical = var
                    numvar = data_group[numerical]
                    if n_obs <= PRIVACY_THRESHOLD:
                        sx, sxx, min_, max_ = 0, 0, int(1e9), -int(1e9)
                    else:
                        sx = numvar.sum()
                        sxx = (numvar * numvar).sum()
                        min_ = numvar.min()
                        max_ = numvar.max()
                    kwarg = {"model__" + "sx_" + numerical + "_" + dataset: sx}
                    self.push_and_add(**kwarg)
                    kwarg = {"model__" + "sxx_" + numerical + "_" + dataset: sxx}
                    self.push_and_add(**kwarg)
                    kwarg = {"model__" + "min_" + numerical + "_" + dataset: min_}
                    self.push_and_min(**kwarg)
                    kwarg = {"model__" + "max_" + numerical + "_" + dataset: max_}
                    self.push_and_max(**kwarg)
                elif var in categoricals:
                    categorical = var
                    if n_obs <= PRIVACY_THRESHOLD:
                        counter = Counter()
                    else:
                        counter = Counter(data_group[categorical])
                    kwarg = {
                        "model__" + "counter_" + categorical + "_" + dataset: counter
                    }
                    self.push_and_add(**kwarg)

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
                    raw_out["model"][dataset]["data"][numerical] = "NOT ENOUGH DATA"
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
                    raw_out["model"][dataset]["data"][categorical] = "NOT ENOUGH DATA"
                    continue
                counter = self.fetch(
                    "model__" + "counter_" + categorical + "_" + dataset
                )
                raw_out["model"][dataset]["data"][categorical] = dict(counter)
        self.result = AlgorithmResult(raw_data=raw_out)


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-y",
        "rightphgparahippocampalgyrus, gender, alzheimerbroadcategory, rs10498633_t",
        "-pathology",
        "dementia",
        "-dataset",
        "lille_simulation, lille_simulation1",
        "-filter",
        "",
    ]
    runner = create_runner(
        DescriptiveStats, algorithm_args=algorithm_args, num_workers=2,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
