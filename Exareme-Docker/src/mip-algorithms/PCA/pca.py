from __future__ import division
from __future__ import print_function

import numpy as np

from mipframework import Algorithm, AlgorithmResult, TabularDataResource
from mipframework.highcharts import ScreePlot
from mipframework.highcharts.user_defined import BubbleGridPlot


class PCA(Algorithm):
    def __init__(self, cli_args):
        super(PCA, self).__init__(__file__, cli_args, intercept=False)

    def local_init(self):
        X = self.data.variables
        n_obs = len(X)
        X = np.array(X)

        sx, sxx = get_local_sums(X)

        self.push_and_add(n_obs=n_obs)
        self.push_and_add(sx=sx)
        self.push_and_add(sxx=sxx)

    def global_init(self):
        n_obs = self.fetch("n_obs")
        sx = self.fetch("sx")
        sxx = self.fetch("sxx")

        means, sigmas = get_moments(n_obs, sx, sxx)

        self.store(n_obs=n_obs)
        self.push(means=means)
        self.push(sigmas=sigmas)

    def local_final(self):
        X = self.data.variables
        var_names = list(X.columns)
        means = self.fetch("means")
        sigmas = self.fetch("sigmas")

        gramian = get_standardized_gramian(X, means, sigmas)

        self.push_and_add(gramian=gramian)
        self.push_and_agree(var_names=var_names)

    def global_final(self):
        n_obs = self.load("n_obs")
        gramian = self.fetch("gramian")
        var_names = self.fetch("var_names")

        eigenvalues, eigenvectors = get_eigenstuff(gramian, n_obs)

        table_eigvals = TabularDataResource(
            fields=[name for name in var_names],
            data=[tuple(eigenvalues)],
            title="Eigenvalues",
        )

        table_eigvecs = TabularDataResource(
            fields=[name for name in var_names],
            data=[zip(*eigenvectors)],
            title="Eigenvectors",
        )

        hc_scree = ScreePlot(
            title="Eigenvalues", data=eigenvalues.tolist(), xtitle="Dimension"
        )

        hc_bubbles = BubbleGridPlot(
            title="Eigenvectors bubble chart",
            data=eigenvectors.tolist(),
            var_names=var_names,
            xtitle="Dimensions",
            ytitle="Variables",
        )

        self.result = AlgorithmResult(
            raw_data={
                "n_obs": n_obs,
                "var_names": var_names,
                "eigenvalues": eigenvalues.tolist(),
                "eigenvectors": eigenvectors.tolist(),
            },
            tables=[table_eigvals, table_eigvecs],
            highcharts=[hc_scree, hc_bubbles],
        )


def get_local_sums(X):
    sx = X.sum(axis=0)
    sxx = (X ** 2).sum(axis=0)
    return sx, sxx


def get_moments(n_obs, sx, sxx):
    means = sx / n_obs
    sigmas = ((sxx - n_obs * means ** 2) / (n_obs - 1)) ** 0.5
    return means, sigmas


def get_standardized_gramian(X, means, sigmas):
    X -= means
    X /= sigmas
    gramian = np.dot(X.T, X)
    return gramian


def get_eigenstuff(gramian, n_obs):
    covariance = np.divide(gramian, n_obs - 1)
    eigenvalues, eigenvectors = np.linalg.eigh(covariance)
    idx = eigenvalues.argsort()[::-1]
    eigenvalues = eigenvalues[idx]
    eigenvectors = eigenvectors[:, idx]
    eigenvectors = eigenvectors.T
    return eigenvalues, eigenvectors


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-y",
        "subjectage,rightventraldc,rightaccumbensarea, gender",
        "-pathology",
        "dementia, leftaccumbensarea",
        "-dataset",
        "adni",
        "-filter",
        "",
        "-formula",
        "",
        "-coding",
        "Treatment",
    ]
    runner = create_runner(PCA, algorithm_args=algorithm_args, num_workers=1,)
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
