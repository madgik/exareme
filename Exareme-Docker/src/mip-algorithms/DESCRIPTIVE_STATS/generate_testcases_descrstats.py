from itertools import ifilterfalse, ifilter
from pathlib import Path

from mipframework.algorithmtest import AlgorithmTest


class DescriptiveStatisticsTest(AlgorithmTest):
    def get_expected(self, alg_input):
        y_names = alg_input[0]["value"]
        datasets = alg_input[1]["value"]
        metadata = self.get_metadata(y_names)
        varnames = y_names.split(",")

        numericals = list(
            ifilterfalse(lambda var: metadata.is_categorical[var], varnames)
        )
        categoricals = list(ifilter(lambda var: metadata.is_categorical[var], varnames))

        out = dict()
        # Single
        out["single"] = dict()
        for numerical in numericals:
            out["single"][numerical] = dict()
            vartab = self.get_data(numerical + ",dataset", datasets)
            for dataset in datasets.split(","):
                numvar = vartab[vartab.dataset == dataset][numerical]
                out["single"][numerical][dataset] = dict()
                n_obs = len(numvar)
                if n_obs <= 0:
                    return None
                mean = numvar.mean()
                std = numvar.std()
                min_ = numvar.min()
                max_ = numvar.max()
                out["single"][numerical][dataset]["num_datapoints"] = n_obs
                out["single"][numerical][dataset]["data"] = {
                    "mean": mean,
                    "std": std,
                    "min": min_,
                    "max": max_,
                    "upper_confidence": mean + std,
                    "lower_confidence": mean - std,
                }
        for categorical in categoricals:
            out["single"][categorical] = dict()
            vartab = self.get_data(categorical + ",dataset", datasets)
            for dataset in datasets.split(","):
                out["single"][categorical][dataset] = dict()
                catvar = vartab[vartab.dataset == dataset][categorical]
                n_obs = len(catvar)
                if n_obs <= 0:
                    return None
                counts = catvar.value_counts()
                out["single"][categorical][dataset]["num_datapoints"] = n_obs
                out["single"][categorical][dataset]["data"] = dict(counts)

        # # Model
        data = self.get_data(y_names + ",dataset", datasets)
        if len(data) == 0:  # todo check if this is problematic in the algorithm
            return None
        out["model"] = dict()
        for dataset in datasets.split(","):
            data_group = data[data.dataset == dataset]
            # if len(data_group) == 0:
            #     continue
            df_num = data_group[numericals]
            df_cat = data_group[categoricals]
            means = df_num.mean()
            stds = df_num.std()
            mins = df_num.min()
            maxs = df_num.max()
            counts = [
                df_cat[categorical].value_counts() for categorical in categoricals
            ]
            out["model"][dataset] = dict()
            out["model"][dataset]["data"] = dict()
            out["model"][dataset]["num_datapoints"] = len(data_group)
            for numerical in numericals:
                out["model"][dataset]["data"][numerical] = {
                    "mean": means[numerical],
                    "std": stds[numerical],
                    "min": mins[numerical],
                    "max": maxs[numerical],
                    "upper_confidence": means[numerical] + stds[numerical],
                    "lower_confidence": means[numerical] - stds[numerical],
                }
            for i, categorical in enumerate(categoricals):
                if counts[i].name != categorical:
                    raise ValueError("WAT??")
                out["model"][dataset]["data"][categorical] = dict(counts[i])
        return out


if __name__ == "__main__":
    prop_path = dbs_folder = Path(__file__).parent / "properties.json"
    descriptive_stats_test = DescriptiveStatisticsTest(prop_path.as_posix())
    descriptive_stats_test.generate_test_cases(num_tests=100)
    descriptive_stats_test.to_json("descriptive_stats_expected.json")
