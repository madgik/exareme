from __future__ import division, print_function

import numpy as np
import pandas as pd
import json
import sqlite3


def main():
    conn = sqlite3.connect(
            "/home/jason/madgik/exareme/Exareme-Docker/src/mip-algorithms/DESCRIPTIVE_STATS/unittest_generation/datasets.db")
    cur = conn.cursor()
    adni = pd.read_csv('../../unit_tests/data/dementia/CSVs/adni.csv')

    var_names = adni.axes[1]
    dataset_idx = list(adni.axes[1]).index('dataset')
    subjectcode_idx = list(adni.axes[1]).index('subjectcode')
    num_vars = len(var_names)
    idx_range = list(range(num_vars))
    idx_range.pop(dataset_idx)
    idx_range.pop(subjectcode_idx)
    results = []
    num_tests = 0
    var_idx = np.random.permutation(idx_range)[:115]
    for ii in var_idx:
        x_name = var_names[ii]
        cur.execute("select isCategorical from metadata where code = '" + x_name + "';")
        is_categorical = cur.fetchall()[0][0]
        if is_categorical:
            cur.execute("select enumerations from metadata where code = '" + x_name + "';")
            enums = cur.fetchall()[0][0].split(',')
            x = adni[x_name]
            freqs = dict()
            if type(x[0]) != str:
                x = [int(xi) if not np.isnan(xi) else xi for xi in x]
                for enum in enums:
                    freqs[enum] = x.count(enum)
            else:
                for enum in enums:
                    f = x.where(x == enum).count()
                    freqs[enum] = f

            if set([type(key) for key in freqs.keys()]) == {np.float64}:
                freqs = {str(int(key)): int(freqs[key]) for key in freqs.keys()}
            else:
                freqs = {str(key): int(freqs[key]) for key in freqs.keys()}
            count = sum(freqs.values())
            input_data = [
                {
                    "name" : "x",
                    "value": x_name
                },
                {
                    "name" : "dataset",
                    "value": "adni"
                },
                {
                    "name" : "filter",
                    "value": ""
                },
                {
                    "name" : "pathology",
                    "value": "dementia"
                }
            ]
            output_data = {
                'Label'    : x_name,
                'Count'    : int(count),
                'Frequency': freqs
            }
            results.append({
                "input" : input_data,
                "output": output_data
            })
            num_tests += 1
        else:
            xm = np.ma.masked_invalid(adni[x_name])
            if False in xm.mask:
                input_data = [
                    {
                        "name" : "x",
                        "value": x_name
                    },
                    {
                        "name" : "dataset",
                        "value": "adni"
                    },
                    {
                        "name" : "filter",
                        "value": ""
                    },
                    {
                        "name" : "pathology",
                        "value": "dementia"
                    }
                ]
                output_data = {
                    'Label'         : str(x_name),
                    'Count'         : int(xm.count()),
                    'Min'           : float(xm.min()),
                    'Max'           : float(xm.max()),
                    'Mean'          : float(xm.mean()),
                    'Std.Err.'      : float(xm.std()),
                    'Mean + Std.Err': float(xm.mean() + xm.std()),
                    'Mean - Std.Err': float(xm.mean() - xm.std())
                }
                results.append({
                    "input" : input_data,
                    "output": output_data
                })
                num_tests += 1

    print(num_tests)
    results = {"results": results}
    with open('descr_stats_runs.json', 'w') as f:
        json.dump(results, f)


if __name__ == '__main__':
    main()
