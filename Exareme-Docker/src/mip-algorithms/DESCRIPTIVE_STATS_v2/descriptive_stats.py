from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from itertools import ifilterfalse, ifilter
from collections import Counter

from mipframework import Algorithm, AlgorithmResult, TabularDataResource
from mipframework.constants import PRIVACY_THRESHOLD


class DescriptiveStats(Algorithm):
    def __init__(self, cli_args):
        super(DescriptiveStats, self).__init__(__file__, cli_args, intercept=False)

    def local_(self):
        data = self.data.full
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
        for dataset in self.parameters.dataset:
            data_group = data[data.dataset == dataset]
            n_obs = len(data_group)
            self.push_and_add(**{"n_obs_" + dataset: n_obs})
            if n_obs <= PRIVACY_THRESHOLD:
                continue
            for numerical in numericals:
                numvar = data_group[numerical]
                sx = numvar.sum()
                self.push_and_add(**{"sx_" + numerical + "_" + dataset: sx})
                sxx = (numvar * numvar).sum()
                self.push_and_add(**{"sxx_" + numerical + "_" + dataset: sxx})
                min_ = numvar.min()
                self.push_and_min(**{"min_" + numerical + "_" + dataset: min_})
                max_ = numvar.max()
                self.push_and_max(**{"max_" + numerical + "_" + dataset: max_})
            for categorical in categoricals:
                counter = Counter(data_group[categorical])
                self.push_and_add(**{"counter_" + categorical + "_" + dataset: counter})

    def global_(self):
        numericals = self.fetch("numericals")
        categoricals = self.fetch("categoricals")
        tables = []

        global fields

        raw_out = dict()
        for dataset in self.parameters.dataset:
            n_obs = self.fetch("n_obs_" + dataset)
            if n_obs > PRIVACY_THRESHOLD:
                raw_out[dataset] = dict()
            for numerical in numericals:
                if n_obs <= PRIVACY_THRESHOLD:
                    tables.append(empty_table(n_obs, numerical, dataset))
                    continue
                sx = self.fetch("sx_" + numerical + "_" + dataset)
                sxx = self.fetch("sxx_" + numerical + "_" + dataset)
                min_ = self.fetch("min_" + numerical + "_" + dataset)
                max_ = self.fetch("max_" + numerical + "_" + dataset)
                mean = sx / n_obs
                std = ((sxx - n_obs * (mean ** 2)) / (n_obs - 1)) ** 0.5
                upper_ci = mean + std
                lower_ci = mean - std
                raw_out[dataset][numerical] = {
                    "mean": mean,
                    "std": std,
                    "min": min_,
                    "max": max_,
                }
                tables.append(
                    TabularDataResource(
                        fields=fields,
                        data=(
                            numerical,
                            n_obs,
                            min_,
                            max_,
                            mean,
                            std,
                            upper_ci,
                            lower_ci,
                            None,
                        ),
                        title=dataset,
                    )
                )
            for categorical in categoricals:
                if n_obs == 0:
                    tables.append(empty_table(n_obs, categorical, dataset))
                    continue
                counter = self.fetch("counter_" + categorical + "_" + dataset)
                raw_out[dataset][categorical] = dict(counter)
                tables.append(
                    TabularDataResource(
                        fields=fields,
                        data=(
                            categorical,
                            n_obs,
                            None,
                            None,
                            None,
                            None,
                            None,
                            None,
                            dict(counter),
                        ),
                        title=dataset,
                    )
                )

        self.result = AlgorithmResult(raw_data=raw_out, tables=tables)


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


def empty_table(n_obs, var, dataset):
    return TabularDataResource(
        fields=fields,
        data=(
            var,
            n_obs,
            "NOT ENOUGH DATA",
            "NOT ENOUGH DATA",
            "NOT ENOUGH DATA",
            "NOT ENOUGH DATA",
            "NOT ENOUGH DATA",
            "NOT ENOUGH DATA",
            "NOT ENOUGH DATA",
        ),
        title=dataset,
    )


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-y",
        "leftaccumbensarea, subjectage,righthippocampus,lefthippocampus, gender",
        "-pathology",
        "dementia",
        "-dataset",
        "adni, ppmi, edsd",
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
