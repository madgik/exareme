from sklearn.decomposition import PCA
import numpy as np

from algorithm_test import AlgorithmTest


class PCATest(AlgorithmTest):
    def get_expected(self, alg_input):
        variables = alg_input[0]['value']
        standardize = alg_input[1]['value']
        data = self.get_data(variables)
        data = data.dropna()
        n_obs = len(data)
        if n_obs == 0 or data.shape[0] < data.shape[1]:
            return None
        if standardize:
            data = (data - data.mean()) / data.std()
        grammian = np.dot(data.T, data)
        covariance_matrix = grammian / (n_obs - 1)
        pca = PCA()
        pca.fit(data)
        return [
            {
                'name': 'n_obs',
                'value': n_obs
            },
            {
                'name' : 'eigen_vals',
                'value': pca.explained_variance_.tolist()
            },
            {
                'name': 'eigen_vecs',
                'value': pca.components_.tolist()
            },
            {
                'name': 'covariance_matrix',
                'value': covariance_matrix.tolist()
            }
        ]


if __name__ == '__main__':
    pca_test = PCATest('/Users/zazon/madgik/exareme/Exareme-Docker/src/mip-algorithms/PCA/properties.json')
    pca_test.generate_test_cases()
    pca_test.to_json('pca_expected.json')
