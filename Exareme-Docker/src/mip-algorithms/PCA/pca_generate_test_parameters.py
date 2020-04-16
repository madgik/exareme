from pathlib import Path

from sklearn.decomposition import PCA

from mipframework.algorithmtest import AlgorithmTest


class PCATest(AlgorithmTest):
    def get_expected(self, alg_input):
        variables = alg_input[0]["value"]
        data = self.get_data(variables)
        data = data.dropna()
        n_obs = len(data)
        if n_obs == 0 or data.shape[0] < data.shape[1]:
            return None
        data -= data.mean()
        data /= data.std()
        pca = PCA()
        pca.fit(data)
        return {
            "n_obs": n_obs,
            "eigen_vals": pca.explained_variance_.tolist(),
            "eigen_vecs": pca.components_.tolist(),
        }


if __name__ == "__main__":
    prop_path = Path(__file__).parent / "properties.json"
    pca_test = PCATest(prop_path.as_posix())
    pca_test.generate_test_cases()
    pca_test.to_json("pca_expected.json")
