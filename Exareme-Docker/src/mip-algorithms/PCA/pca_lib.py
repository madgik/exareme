from __future__ import division
from __future__ import print_function

import json
import numpy as np

from utils.algorithm_utils import StateData, TransferAndAggregateData
from utils.algorithm_utils import make_json_raw, query_from_formula


def get_data(args):
    input_local_DB = args.input_local_DB
    args_x = list(
            args.y
                .replace(' ', '')
                .split(',')
    )
    variables = (args_x,)
    dataset = args.dataset
    query_filter = args.filter
    formula = args.formula
    formula = formula.replace('_', '~')  # fixme Fix tilda problem and remove
    data_table = args.data_table
    metadata_table = args.metadata_table
    metadata_code_column = args.metadata_code_column
    metadata_isCategorical_column = args.metadata_isCategorical_column

    # Get data from local DB
    _, X = query_from_formula(fname_db=input_local_DB,
                              formula=formula,
                              variables=variables,
                              dataset=dataset,
                              query_filter=query_filter,
                              data_table=data_table,
                              metadata_table=metadata_table,
                              metadata_code_column=metadata_code_column,
                              metadata_isCategorical_column=metadata_isCategorical_column,
                              no_intercept=True,
                              coding=None)

    return X


def local_1(args, local_in):
    standardize = json.loads(args.standardize)
    # Unpack data
    X = local_in
    n_obs, n_cols = len(X), len(X.columns)
    var_names = list(X.columns)
    X = np.array(X)

    sx = X.sum(axis=0)
    if standardize:
        sxx = np.sum(np.square(X), axis=0)
        local_out = TransferAndAggregateData(n_obs=(n_obs, 'add'), sx=(sx, 'add'), sxx=(sxx, 'add'))
    else:
        local_out = TransferAndAggregateData(n_obs=(n_obs, 'add'), sx=(sx, 'add'))

    local_state = StateData(X=X, var_names=var_names)

    return local_state, local_out


def global_1(args, global_in):
    standardize = json.loads(args.standardize)
    # Unpack global input
    data = global_in.get_data()
    n_obs, sx = data['n_obs'], data['sx']
    means = sx / n_obs
    if standardize:
        sxx = data['sxx']
        sigmas = ((sxx - n_obs * means ** 2) / (n_obs - 1)) ** 0.5
        global_out = TransferAndAggregateData(means=(means, 'do_nothing'),
                                              sigmas=(sigmas, 'do_nothing'))
    else:
        global_out = TransferAndAggregateData(means=(means, 'do_nothing'))

    return global_out


def local_2(args, local_state, local_in):
    standardize = json.loads(args.standardize)
    # Unpack local state
    X, var_names = local_state['X'], local_state['var_names']
    # Unpack local input
    data = local_in.get_data()
    means = data['means']

    n_obs = len(X)
    X -= means
    if standardize:
        sigmas = data['sigmas']
        X /= sigmas
    gramian = np.dot(np.transpose(X), X)

    # Pack results
    local_out = TransferAndAggregateData(gramian=(gramian, 'add'), n_obs=(n_obs, 'add'),
                                         var_names=(var_names, 'do_nothing'))
    return local_out


def global_2(args, global_in):
    # Unpack global input
    data = global_in.get_data()
    gramian, n_obs, var_names = data['gramian'], data['n_obs'], data['var_names']

    covar_matr = np.divide(gramian, n_obs - 1)
    eigen_vals, eigen_vecs = np.linalg.eigh(covar_matr)

    idx = eigen_vals.argsort()[::-1]
    eigen_vals = eigen_vals[idx]
    eigen_vecs = eigen_vecs[:, idx]
    eigen_vecs = eigen_vecs.T

    pca_result = PCAResult(n_obs, var_names, eigen_vals, eigen_vecs)
    result = pca_result.get_output()

    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        raise ValueError('Result contains NaNs.')
    return global_out


class PCAResult(object):
    def __init__(self, n_obs, var_names, eigen_vals, eigen_vecs):
        self.n_obs = n_obs
        self.var_names = var_names
        self.eigen_vals = eigen_vals
        self.eigen_vecs = eigen_vecs

    def get_json_raw(self):
        return make_json_raw(
                n_obs=self.n_obs,
                var_names=self.var_names,
                eigen_vals=self.eigen_vals,
                eigen_vecs=self.eigen_vecs
        )

    def get_eigenval_table(self):
        tabular_data = dict()
        tabular_data["name"] = "Eigenvalues"
        tabular_data["profile"] = "tabular-data-resource"
        tabular_data["data"] = [self.eigen_vals.tolist()]
        tabular_data["schema"] = {
            "fields": [{"name": n, "type": "number"} for n in self.var_names]
        }
        return tabular_data

    def get_eigenvec_table(self):
        tabular_data = dict()
        tabular_data["name"] = "Eigenvectors"
        tabular_data["profile"] = "tabular-data-resource"
        tabular_data["data"] = []
        for ei in self.eigen_vecs.T:
            tabular_data["data"].append(ei.tolist())
        tabular_data["schema"] = {
            "fields": [{"name": n, "type": "number"} for n in self.var_names]
        }
        return tabular_data

    def get_highchart_eigen_scree(self):
        return {
            "chart"      : {
                "type": 'line'
            },
            "title"      : {
                "text": 'Eigenvalues'
            },
            "xAxis"      : {
                "categories": list(range(len(self.eigen_vals))),
                "title"     : {
                    "text": 'Dimension'
                }
            },
            "yAxis"      : {
                "title": {
                    "text": 'Eigenvalue'
                }
            },
            "plotOptions": {
                "line": {
                    "dataLabels"         : {
                        "enabled": True
                    },
                    "label"              : False,
                    "enableMouseTracking": False
                }
            },
            "series"     : [
                {
                    "type": 'column',
                    "data": [round(ei, 2) for ei in self.eigen_vals]
                },
                {
                    "type" : 'line',
                    "data" : [round(ei, 2) for ei in self.eigen_vals],
                    "color": '#0A1E6E'
                }
            ],
            "legend"     : {
                "enabled": False
            }
        }

    def get_bubbles(self):
        max_elem = max([max([abs(elem) for elem in evec]) for evec in self.eigen_vecs])
        return {
            "chart"      : {
                "type"           : 'bubble',
                "plotBorderWidth": 1,
                "zoomType"       : 'xy'
            },

            "legend"     : {
                "enabled": False
            },

            "title"      : {
                "text": 'Eigenvectors bubble chart'
            },
            "xAxis"      : {
                "gridLineWidth": 1,
                "title"        : {
                    "text": 'Dimensions'
                },
                "tickLength"   : 500,
                "categories"   : list(range(len(self.eigen_vals)))
            },

            "yAxis"      : {
                "startOnTick": False,
                "endOnTick"  : False,
                "title"      : {
                    "text": 'Variables'
                },
                "categories" : self.var_names,
                "maxPadding" : 0.2,
            },
            "plotOptions": {
                "bubble": {
                    "minSize": 0,
                    "maxSize": 50
                }
            },

            "colorAxis"  : {
                "min": 0,
                "max": max_elem
            },
            "series"     : [{
                "colorKey": 'z',
                "data"    : [{'x': i, 'y': j, 'z': abs(elem)}
                             for i, evec in enumerate(self.eigen_vecs)
                             for j, elem in enumerate(evec)]
            }]
        }

    def get_output(self):
        result = {
            "result": [
                # Raw results
                {
                    "type": "application/json",
                    "data": self.get_json_raw()
                },
                # Tabular eigenvalues
                {
                    "type": "application/vnd.dataresource+json",
                    "data": self.get_eigenval_table()
                },
                # Tabular eigenvectors
                {
                    "type": "application/vnd.dataresource+json",
                    "data": self.get_eigenvec_table()
                },
                # Highchart Eigenvalues scree plot
                {
                    "type": "application/vnd.highcharts+json",
                    "data": self.get_highchart_eigen_scree()
                },
                # Highchart Eigenvectors bubble plot
                {
                    "type": "application/vnd.highcharts+json",
                    "data": self.get_bubbles()
                }
            ]
        }
        return result
