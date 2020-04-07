from __future__ import division
from __future__ import print_function

import numpy as np

from mipframework import Algorithm, AlgorithmResult


class PCA(Algorithm):
    def __init__(self, cli_args):
        super(PCA, self).__init__(__file__, cli_args, intercept=False)

    def local_init(self):
        X = self.data.variables

        n_obs = len(X)
        X = np.array(X)
        sx = X.sum(axis=0)
        sxx = (X ** 2).sum(axis=0)

        self.push_and_add(n_obs=n_obs)
        self.push_and_add(sx=sx)
        self.push_and_add(sxx=sxx)

    def global_init(self):
        n_obs = self.fetch("n_obs")
        sx = self.fetch("sx")
        sxx = self.fetch("sxx")

        means = sx / n_obs
        sigmas = ((sxx - n_obs * means ** 2) / (n_obs - 1)) ** 0.5

        self.store(n_obs=n_obs)
        self.push(means=means)
        self.push(sigmas=sigmas)

    def local_final(self):
        X = self.data.variables
        means = self.fetch("means")
        sigmas = self.fetch("sigmas")

        var_names = list(X.columns)
        X -= means
        X /= sigmas
        gramian = np.dot(X.T, X)

        self.push_and_add(gramian=gramian)
        self.push_and_agree(var_names=var_names)

    def global_final(self):
        n_obs = self.load("n_obs")
        gramian = self.fetch("gramian")
        # var_names = self.fetch("var_names")

        covariance = np.divide(gramian, n_obs - 1)
        eigen_values, eigen_vectors = np.linalg.eigh(covariance)

        idx = eigen_values.argsort()[::-1]
        eigen_values = eigen_values[idx]
        eigen_vectors = eigen_vectors[:, idx]
        eigen_vectors = eigen_vectors.T

        self.result = AlgorithmResult(
            raw_data={
                "eigen_values": eigen_values.tolist(),
                "eigen_vectors": eigen_vectors.tolist(),
            }
        )


# class PCAResult(object):
#     def __init__(self, n_obs, var_names, eigen_vals, eigen_vecs):
#         self.n_obs = n_obs
#         self.var_names = var_names
#         self.eigen_vals = eigen_vals
#         self.eigen_vecs = eigen_vecs
#
#     def get_json_raw(self):
#         return make_json_raw(
#                 n_obs=self.n_obs,
#                 var_names=self.var_names,
#                 eigen_vals=self.eigen_vals,
#                 eigen_vecs=self.eigen_vecs
#         )
#
#     def get_eigenval_table(self):
#         tabular_data = dict()
#         tabular_data["name"] = "Eigenvalues"
#         tabular_data["profile"] = "tabular-data-resource"
#         tabular_data["data"] = [self.eigen_vals.tolist()]
#         tabular_data["schema"] = {
#             "fields": [{"name": n, "type": "number"} for n in self.var_names]
#         }
#         return tabular_data
#
#     def get_eigenvec_table(self):
#         tabular_data = dict()
#         tabular_data["name"] = "Eigenvectors"
#         tabular_data["profile"] = "tabular-data-resource"
#         tabular_data["data"] = []
#         for ei in self.eigen_vecs.T:
#             tabular_data["data"].append(ei.tolist())
#         tabular_data["schema"] = {
#             "fields": [{"name": n, "type": "number"} for n in self.var_names]
#         }
#         return tabular_data
#
#     def get_highchart_eigen_scree(self):
#         return {
#             "chart"      : {
#                 "type": 'line'
#             },
#             "title"      : {
#                 "text": 'Eigenvalues'
#             },
#             "xAxis"      : {
#                 "categories": list(range(len(self.eigen_vals))),
#                 "title"     : {
#                     "text": 'Dimension'
#                 }
#             },
#             "yAxis"      : {
#                 "title": {
#                     "text": 'Eigenvalue'
#                 }
#             },
#             "plotOptions": {
#                 "line": {
#                     "dataLabels"         : {
#                         "enabled": True
#                     },
#                     "label"              : False,
#                     "enableMouseTracking": False
#                 }
#             },
#             "series"     : [
#                 {
#                     "type": 'column',
#                     "data": [round(ei, 2) for ei in self.eigen_vals]
#                 },
#                 {
#                     "type" : 'line',
#                     "data" : [round(ei, 2) for ei in self.eigen_vals],
#                     "color": '#0A1E6E'
#                 }
#             ],
#             "legend"     : {
#                 "enabled": False
#             }
#         }
#
#     def get_bubbles(self):
#         max_elem = max([max([abs(elem) for elem in evec]) for evec in self.eigen_vecs])
#         return {
#             "chart"      : {
#                 "type"           : 'bubble',
#                 "plotBorderWidth": 1,
#                 "zoomType"       : 'xy'
#             },
#
#             "legend"     : {
#                 "enabled": False
#             },
#
#             "title"      : {
#                 "text": 'Eigenvectors bubble chart'
#             },
#             "xAxis"      : {
#                 "gridLineWidth": 1,
#                 "title"        : {
#                     "text": 'Dimensions'
#                 },
#                 "tickLength"   : 500,
#                 "categories"   : list(range(len(self.eigen_vals)))
#             },
#
#             "yAxis"      : {
#                 "startOnTick": False,
#                 "endOnTick"  : False,
#                 "title"      : {
#                     "text": 'Variables'
#                 },
#                 "categories" : self.var_names,
#                 "maxPadding" : 0.2,
#             },
#             "plotOptions": {
#                 "bubble": {
#                     "minSize": 0,
#                     "maxSize": 50
#                 }
#             },
#
#             "colorAxis"  : {
#                 "min": 0,
#                 "max": max_elem
#             },
#             "series"     : [{
#                 "colorKey": 'z',
#                 "data"    : [{'x': i, 'y': j, 'z': abs(elem)}
#                              for i, evec in enumerate(self.eigen_vecs)
#                              for j, elem in enumerate(evec)]
#             }]
#         }
#
#     def get_output(self):
#         result = {
#             "result": [
#                 # Raw results
#                 {
#                     "type": "application/json",
#                     "data": self.get_json_raw()
#                 },
#                 # Tabular eigenvalues
#                 {
#                     "type": "application/vnd.dataresource+json",
#                     "data": self.get_eigenval_table()
#                 },
#                 # Tabular eigenvectors
#                 {
#                     "type": "application/vnd.dataresource+json",
#                     "data": self.get_eigenvec_table()
#                 },
#                 # Highchart Eigenvalues scree plot
#                 {
#                     "type": "application/vnd.highcharts+json",
#                     "data": self.get_highchart_eigen_scree()
#                 },
#                 # Highchart Eigenvectors bubble plot
#                 {
#                     "type": "application/vnd.highcharts+json",
#                     "data": self.get_bubbles()
#                 }
#             ]
#         }
#         return result


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-x",
        "rightioginferioroccipitalgyrus,rightmfcmedialfrontalcortex",
        "-y",
        "subjectage,rightventraldc,rightaccumbensarea",
        "-pathology",
        "dementia, leftaccumbensarea",
        "-dataset",
        "adni",
        "-filter",
        "",
        "-formula",
        "",
        "-coding",
        "",
    ]
    runner = create_runner(
        for_class="PCA",
        found_in="PCA/pca_lib",
        alg_type="multiple-local-global",
        algorithm_args=algorithm_args,
        num_workers=1,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
