from __future__ import division
from __future__ import print_function

import json
import sys
from os import path
import numpy as np
import numpy.ma as ma

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from utils.algorithm_utils import StateData, TransferAndAggregateData, query_with_privacy, ExaremeError, \
    PrivacyError, PRIVACY_MAGIC_NUMBER, make_json_raw


def get_data(args):
    query = args.db_query

    fname_loc_db = path.abspath(args.input_local_DB)
    if args.x == '':
        raise ExaremeError('Field x must be non empty.')
    schema_X = list(
            args.x
                .replace(' ', '')
                .split(',')
    )
    schema, data = query_with_privacy(fname_db=fname_loc_db, query=query)
    idx_X = [schema.index(v) for v in schema_X if v in schema]
    data = np.array(data, dtype=np.float64)
    X = data[:, idx_X]
    return X, schema

def local_1(local_in):
    # Unpack data
    X, schema_X = local_in
    n_obs, n_cols = len(X), len(X[0])

    # Init statistics
    nn = np.empty(n_cols, dtype=np.int)
    sx = np.empty(n_cols, dtype=np.float)

    mask = [True in np.isnan(X[row, :]) for row in range(n_obs)]
    for i in xrange(n_cols):
        x = X[:, i]
        xm = ma.masked_array(x, mask=mask)
        nn[i] = n_obs - sum(mask)
        if nn[i] < PRIVACY_MAGIC_NUMBER:
            raise PrivacyError('Removing missing values results in illegal number of datapoints in local db.')
        sx[i] = xm.sum()

        local_state = StateData(X=X, schema_X=schema_X)
        local_out = TransferAndAggregateData(nn=(nn, 'add'), sx=(sx, 'add'))

    return local_state, local_out

def global_1(global_in):
    data = global_in.get_data()
    nn, sx = data['nn'], data['sx']
    n_cols = len(nn)
    mean = np.empty(n_cols, dtype=np.float)
    for i in xrange(n_cols):
        mean[i] = sx[i] / nn[i]

    global_out = TransferAndAggregateData(mean=(mean, 'do_nothing'))

    return global_out

def local_2(local_state, local_in):
    # Unpack local state
    X, schema_X = local_state['X'], local_state['schema_X']
    # Unpack local input
    data = local_in.get_data()
    mean = data['mean']

    # Substract the mean of each variable
    n_obs = len(X)
    X = X - mean
    gramian = np.dot(np.transpose(X), X)

    # Pack results
    local_out = TransferAndAggregateData(gramian=(gramian, 'add'), n_obs=(n_obs, 'add'), schema_X=(schema_X, 'do_nothing'))
    return local_out

def global_2(global_in):
    # Unpack global input
    data = global_in.get_data()
    gramian, n_obs, schema_X = data['gramian'], data['n_obs'], data['schema_X']
    covar_matr = np.divide(gramian, n_obs - 1)

    eigen_vals, eigen_vecs = np.linalg.eig(covar_matr)

    idx = eigen_vals.argsort()[::-1]
    eigen_vals = eigen_vals[idx]
    eigen_vecs = eigen_vecs[:, idx]

    json_raw = make_json_raw(eigenvalues=eigen_vals, eigenvectors=eigen_vecs, var_names=schema_X)
    # Write output to JSON
    result = {
        "result": [
            # Raw results
            {
                "type": "application/json",
                "data": json_raw
            },
            # # Tabular data resource summary
            # {
            #     "type": "application/vnd.dataresource+json",
            #     "data":
            #         {
            #             "name"   : "Pearson correlation summary",
            #             "profile": "tabular-data-resource",
            #             "data"   : tabular_data_summary[1:],
            #             "schema" : {
            #                 "fields": tabular_data_summary_schema_fields
            #             }
            #         }
            #
            # },
            # # Highchart correlation matrix
            # {
            #     "type": "application/vnd.highcharts+json",
            #     "data": hichart_correl_matr
            # }
        ]
    }
    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        raise ValueError('Result contains NaNs.')
    return global_out
