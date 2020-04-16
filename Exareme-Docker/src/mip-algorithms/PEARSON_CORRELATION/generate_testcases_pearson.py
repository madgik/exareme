from pathlib import Path

from scipy.stats import pearsonr

from mipframework.algorithmtest import AlgorithmTest


class PearsonTest(AlgorithmTest):
    def get_expected(self, alg_input):

        # Get data and remove missing values
        x_names = alg_input[0]["value"]
        y_names = alg_input[1]["value"]
        variables = y_names
        if x_names != "":
            variables += "," + x_names
        data = self.get_data(variables)
        data = data.dropna()
        n_obs = len(data)

        # If n_obs < n_cols reject
        if data.shape[0] < data.shape[1]:
            return None

        # Assign data to X, Y
        X = Y = data[y_names.split(",")]
        if x_names != "":
            X = data[x_names.split(",")]

        # If Y has single value X cannot be empty
        if x_names == "" and Y.shape[1] == 1:
            return None

        correlation = []
        p_values = []
        for j, y_col in enumerate(Y):
            correlation.append([])
            p_values.append([])
            for i, x_col in enumerate(X):
                r, p = pearsonr(X[x_col], Y[y_col])
                correlation[j].append(r)
                p_values[j].append(p)
        return {
            "n_obs": n_obs,
            "Pearson correlation coefficient": correlation,
            "p-value": p_values,
        }


if __name__ == "__main__":
    prop_path = dbs_folder = Path(__file__).parent / "properties.json"
    logistic_regression_test = PearsonTest(prop_path.as_posix())
    logistic_regression_test.generate_test_cases(num_tests=100)
    logistic_regression_test.to_json("pearson_expected.json")
