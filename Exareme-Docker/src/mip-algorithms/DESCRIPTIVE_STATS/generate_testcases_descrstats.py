from itertools import ifilterfalse, ifilter
from pathlib import Path

from mipframework.algorithmtest import AlgorithmTest
from DESCRIPTIVE_STATS.descriptive_stats import get_counts_and_percentages


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
            varlabel = metadata.label[numerical]
            out["single"][varlabel] = dict()
            data_df = self.get_data(numerical + ",dataset", datasets)
            for dataset in datasets.split(","):
                numvar = data_df[data_df.dataset == dataset][numerical]
                out["single"][varlabel][dataset] = dict()
                n_total = len(numvar)
                numvar = numvar.dropna()
                n_obs = len(numvar)
                if n_obs <= 0:
                    return None
                mean = numvar.mean()
                std = numvar.std()
                min_ = numvar.min()
                max_ = numvar.max()
                out["single"][varlabel][dataset]["num_total"] = n_total
                out["single"][varlabel][dataset]["num_datapoints"] = n_obs
                out["single"][varlabel][dataset]["num_nulls"] = n_total - n_obs
                out["single"][varlabel][dataset]["data"] = {
                    "mean": mean,
                    "std": std,
                    "min": min_,
                    "max": max_,
                }
        for categorical in categoricals:
            varlabel = metadata.label[categorical]
            out["single"][varlabel] = dict()
            data_df = self.get_data(categorical + ",dataset", datasets)
            for dataset in datasets.split(","):
                out["single"][varlabel][dataset] = dict()
                catvar = data_df[data_df.dataset == dataset][categorical]
                n_total = len(catvar)
                catvar = catvar.dropna()
                n_obs = len(catvar)
                if n_obs <= 0:
                    return None
                counts = catvar.value_counts()
                out["single"][varlabel][dataset]["num_total"] = n_total
                out["single"][varlabel][dataset]["num_datapoints"] = n_obs
                out["single"][varlabel][dataset]["num_nulls"] = n_total - n_obs
                out["single"][varlabel][dataset]["data"] = get_counts_and_percentages(
                    counts
                )

        # # Model
        data = self.get_data(y_names + ",dataset", datasets)
        if len(data) == 0:  # todo check if this is problematic in the algorithm
            return None
        out["model"] = dict()
        for dataset in datasets.split(","):
            data_group = data[data.dataset == dataset]
            n_total = len(data_group)
            data_group = data_group.dropna()
            n_obs = len(data_group)
            if n_obs == 0:
                return None
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
            out["model"][dataset]["num_datapoints"] = n_obs
            out["model"][dataset]["num_total"] = n_total
            out["model"][dataset]["num_nulls"] = n_total - n_obs
            for numerical in numericals:
                varlabel = metadata.label[numerical]
                out["model"][dataset]["data"][varlabel] = {
                    "mean": means[numerical],
                    "std": stds[numerical],
                    "min": mins[numerical],
                    "max": maxs[numerical],
                }
            for i, categorical in enumerate(categoricals):
                varlabel = metadata.label[categorical]
                if counts[i].name != categorical:
                    raise ValueError("WAT??")
                out["model"][dataset]["data"][varlabel] = get_counts_and_percentages(
                    counts[i]
                )
        return out


if __name__ == "__main__":
    prop_path = dbs_folder = Path(__file__).parent / "properties.json"
    descriptive_stats_test = DescriptiveStatisticsTest(
        prop_path.as_posix(), dropna=False
    )
    descriptive_stats_test.generate_test_cases(num_tests=100)
    descriptive_stats_test.to_json("descriptive_stats_expected.json")
