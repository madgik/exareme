# XXX this module runs on python 3.9 because we need pingouin to calculate
# Tukey table.

from pathlib import Path

import numpy as np
from pingouin import pairwise_tukey
import statsmodels.api as sm
from statsmodels.formula.api import ols
from statsmodels.stats.libqsturng import psturng

from mipframework.algorithmtest import AlgorithmTest
from mipframework.exceptions import PrivacyError
from utils.algorithm_utils import PRIVACY_MAGIC_NUMBER


class AnovaTest(AlgorithmTest):
    def get_expected(self, alg_input):
        # Get data and remove missing values
        x_names = alg_input[0]["value"]
        y_names = alg_input[1]["value"]
        datasets = alg_input[3]["value"]
        variables = y_names
        if x_names != "":
            variables += "," + x_names
        try:
            data = self.get_data(variables, datasets)
        except PrivacyError:
            return None
        data = data.dropna()
        n_obs = len(data)
        n_groups = len(set(data[x_names]))

        # If n_obs < n_cols reject
        if n_obs <= PRIVACY_MAGIC_NUMBER:
            return None
        if data[x_names].unique().shape[0] < 2:
            return None

        # Anova
        formula = "{y} ~ {x}".format(y=y_names, x=x_names)
        lm = ols(formula, data=data).fit()
        aov = sm.stats.anova_lm(lm)
        result = aov.to_dict()

        # # Tukey test
        tukey = pairwise_tukey(data=data, dv=y_names, between=x_names)
        tukey_results = []
        for _, row in tukey.iterrows():
            tukey_result = dict()
            tukey_result["groupA"] = row["A"]
            tukey_result["groupB"] = row["B"]
            tukey_result["meanA"] = row["mean(A)"]
            tukey_result["meanB"] = row["mean(B)"]
            tukey_result["diff"] = row["diff"]
            tukey_result["se"] = row["se"]
            tukey_result["t_stat"] = row["T"]
            # computing pval because pingouin and statsmodels implementations
            # of pstrung do not agree
            pval = psturng(
                np.sqrt(2) * np.abs(row["T"]), n_groups, result["df"]["Residual"]
            )
            tukey_result["p_tukey"] = float(pval)
            tukey_results.append(tukey_result)

        expected_out = dict()
        expected_out["df_residual"] = result["df"]["Residual"]
        expected_out["df_explained"] = result["df"][x_names]
        expected_out["ss_residual"] = result["sum_sq"]["Residual"]
        expected_out["ss_explained"] = result["sum_sq"][x_names]
        expected_out["ms_residual"] = result["mean_sq"]["Residual"]
        expected_out["ms_explained"] = result["mean_sq"][x_names]
        expected_out["p_value"] = result["PR(>F)"][x_names]
        expected_out["f_stat"] = result["F"][x_names]
        expected_out["tukey_test"] = tukey_results

        return expected_out


if __name__ == "__main__":
    prop_path = dbs_folder = Path(__file__).parent / "properties.json"
    algtest = AnovaTest(prop_path.as_posix())
    algtest.generate_test_cases(num_tests=100)
    algtest.to_json("anova_expected.json")
