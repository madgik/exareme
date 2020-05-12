from itertools import ifilterfalse, ifilter
from pathlib import Path

from mipframework.algorithmtest import AlgorithmTest


class DescriptiveStatisticsTest(AlgorithmTest):
    def get_expected(self, alg_input):
        y_names = alg_input[0]["value"]
        datasets = alg_input[1]["value"]
        data = self.get_data(y_names + ",dataset", datasets)
        if len(data) == 0:
            return None
        metadata = self.get_metadata(y_names)

        varnames = y_names.split(",")
        numericals = list(
            ifilterfalse(lambda var: metadata.is_categorical[var], varnames)
        )
        categoricals = list(ifilter(lambda var: metadata.is_categorical[var], varnames))

        out = dict()
        for dataset in datasets.split(","):
            data_group = data[data.dataset == dataset]
            if len(data_group) == 0:
                continue
            df_num = data_group[numericals]
            df_cat = data_group[categoricals]
            means = df_num.mean()
            stds = df_num.std()
            mins = df_num.min()
            maxs = df_num.max()
            counts = [
                df_cat[categorical].value_counts() for categorical in categoricals
            ]
            out[dataset] = dict()
            for numerical in numericals:
                out[dataset][numerical] = {
                    "mean": means[numerical],
                    "std": stds[numerical],
                    "min": mins[numerical],
                    "max": maxs[numerical],
                }
            for i, categorical in enumerate(categoricals):
                if counts[i].name != categorical:
                    raise ValueError("WAT??")
                out[dataset][categorical] = dict(counts[i])
        return out


if __name__ == "__main__":
    prop_path = dbs_folder = Path(__file__).parent / "properties.json"
    descriptive_stats_test = DescriptiveStatisticsTest(prop_path.as_posix())
    descriptive_stats_test.generate_test_cases(num_tests=100)
    descriptive_stats_test.to_json("descriptive_stats_expected.json")
