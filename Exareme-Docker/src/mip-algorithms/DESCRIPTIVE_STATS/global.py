from __future__ import division
from __future__ import print_function

import sys
import math
import json
from os import path
import numpy as np
from argparse import ArgumentParser

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import set_algorithms_output_data, ExaremeError, P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR
from lib import DescrStatsLocalDT


def descr_stats_global(global_in):
    nn, sx, sxx, xmin, xmax, schema_X = global_in.get_data()
    n_cols = len(nn)
    mean = np.empty(n_cols, dtype=np.float)
    std = np.empty(n_cols, dtype=np.float)
    upper_ci = np.empty(n_cols, dtype=np.float)
    lower_ci = np.empty(n_cols, dtype=np.float)
    for i in xrange(n_cols):
        mean[i] = sx[i] / nn[i]
        std[i] = (sxx[i] / nn[i] - mean[i] ** 2) ** 0.5
        upper_ci[i] = mean[i] + std[i]
        lower_ci[i] = mean[i] - std[i]

    # Raw data
    result_list = []
    for i in xrange(n_cols):
        result_list.append({
            'Variable': schema_X[i],
            'Mean': mean[i],
            'Std.Err.': std[i],
            'Upper C.I.': upper_ci[i],
            'Lower C.I.': lower_ci[i]
        })

    result = {
        'result' : [
            {
                "type": "application/json",
                "data": result_list
            }
        ]
    }

    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        print('Result contains NaNs.')
    else:
        return global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = DescrStatsLocalDT.load(local_dbs)
    # Run algorithm global step
    global_out = descr_stats_global(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()