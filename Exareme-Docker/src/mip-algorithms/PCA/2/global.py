from __future__ import division
from __future__ import print_function

import json
import sys
from os import path, getcwd

import numpy as np

_ALGORITHM_TYPE = 'python_multiple_local_global'

if _ALGORITHM_TYPE == 'python_local_global':
    dir_levels = 2
elif _ALGORITHM_TYPE == 'python_multiple_local_global':
    dir_levels = 3
elif _ALGORITHM_TYPE == 'python_iterative':
    if path.basename(getcwd()) == 'termination_condition':
        dir_levels = 3
    else:
        dir_levels = 4
else:
    raise ValueError('_ALGORITHM_TYPE unknown type.')
new_path = path.abspath(__file__)
for _ in range(dir_levels):
    new_path = path.dirname(new_path)
sys.path.append(new_path)

from utils.algorithm_utils import set_algorithms_output_data, make_json_raw, TransferData, parse_exareme_args


def pca_global(global_in):
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


def main(args):
    local_dbs = path.abspath(args.local_step_dbs)

    # Load local nodes output
    local_out = TransferData.load(local_dbs)
    # Run algorithm global step
    global_out = pca_global(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    args = parse_exareme_args()
    main(args)
