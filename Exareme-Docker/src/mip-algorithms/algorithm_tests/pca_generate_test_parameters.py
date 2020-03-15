from sklearn.decomposition import PCA

from algorithm_test import AlgorithmTest


class PCATest(AlgorithmTest):
    def get_expected(self, alg_input):
        variables = alg_input[0]['value']
        standardize = alg_input[1]['value']
        data = self.get_data(variables)
        if len(data) == 0 or data.shape[0] < data.shape[1]:
            return None
        if standardize:
            data = (data - data.mean()) / data.std()
        pca = PCA()
        pca.fit(data)
        return [
            {
                'name' : 'eigen_vals',
                'value': pca.explained_variance_.tolist()
            },
            {
                'name': 'eigen_vecs',
                'value': pca.components_.tolist()
            }
        ]


if __name__ == '__main__':
    pca_test = PCATest('/Users/zazon/madgik/exareme/Exareme-Docker/src/mip-algorithms/PCA/properties.json')
    test_cases = pca_test.generate_test_cases()
    pass
