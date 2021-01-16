from pathlib import Path

import statsmodels.api as sm
from statsmodels.formula.api import ols

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

        # If n_obs < n_cols reject
        if n_obs <= PRIVACY_MAGIC_NUMBER:
            return None
        if data[x_names].unique().shape[0] < 2:
            return None

        formula = "{y} ~ {x}".format(y=y_names, x=x_names)
        lm = ols(formula, data=data).fit()
        aov = sm.stats.anova_lm(lm)

        result = aov.to_dict()
        # del result['F']['Residual']
        # del result['PR(>F)']['Residual']

        expected_out = dict()
        expected_out["df_residual"] = result["df"]["Residual"]
        expected_out["df_explained"] = result["df"][x_names]
        expected_out["ss_residual"] = result["sum_sq"]["Residual"]
        expected_out["ss_explained"] = result["sum_sq"][x_names]
        expected_out["ms_residual"] = result["mean_sq"]["Residual"]
        expected_out["ms_explained"] = result["mean_sq"][x_names]
        expected_out["p_value"] = result["PR(>F)"][x_names]
        expected_out["f_stat"] = result["F"][x_names]

        return expected_out


if __name__ == "__main__":
    prop_path = dbs_folder = Path(__file__).parent / "properties.json"
    algtest = AnovaTest(prop_path.as_posix())
    algtest.generate_test_cases(num_tests=100)
    algtest.to_json("anova_expected.json")
