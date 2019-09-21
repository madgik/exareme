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
from lib import DescrStatsLocal_DT


def descr_stats_global(global_in):
    transf_var_list, num_vars = global_in.get_data()
    results = []
    for var in transf_var_list:

        if var.is_categorical:
            is_categorical, var_name, count, freqs = var.get_data()
            freqs = {str(key): freqs[key] for key in freqs.keys()}
        else:
            is_categorical, var_name, nn, sx, sxx, xmin, xmax = var.get_data()
            mean = sx / nn
            std = (sxx / nn - mean ** 2) ** 0.5
            upper_ci = mean + std
            lower_ci = mean - std

        if is_categorical:
            # Raw data
            results = [{
                'Label'         : var_name,
                'Count'         : count,
                'Frequency'           : freqs
            }]

            schema = {
                "fields": [
                    {"name": "Label", "type": "string"},
                    {"name": "Count", "type": "integer"},
                    {"name": "Frequencies", "type": "object"},
                ]
            }
        else:
            # Raw data
            results.append({
                'Label'         : var_name,
                'Count'         : nn,
                'Min'           : xmin,
                'Max'           : xmax,
                'Mean'          : mean,
                'Std.Err.'      : std,
                'Mean + Std.Err': upper_ci,
                'Mean - Std.Err': lower_ci
            })

    schema = {
        "fields": [
            {"name": "Label", "type": "string"},
            {"name": "Count", "type": "integer"},
            {"name": "Min", "type": "real"},
            {"name": "Max", "type": "real"},
            {"name": "Mean", "type": "real"},
            {"name": "Std.Err.", "type": "real"},
            {"name": "Mean + Std.Err", "type": "real"},
            {"name": "Mean - Std.Err", "type": "real"}
        ]
    }


    result = {
        'result': [
            {
                "type": "application/vnd.dataresource+json",
                "data": {
                    "data": results,
                    "schema": schema
                }
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

    local_out = DescrStatsLocal_DT.load(local_dbs)
    # Run algorithm global step
    global_out = descr_stats_global(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
