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
    num_vars = len(var_names)
    results = []
    num_tests = 0
    while num_tests < 100:
        ii = np.random.randint(1, num_vars)
        x_name = var_names[ii]
        if x_name == 'dataset':
            break
        cur.execute("select isCategorical from metadata where code = '" + x_name + "';")
        is_categorical = cur.fetchall()[0][0]
        if is_categorical:
            x = adni[x_name]
            freqs = dict()
            for val in x.unique():
                if type(val) == str:
                    f = x.where(x == val).count()
                    freqs[val] = f
                else:
                    if not np.isnan(val):
                        f = x.where(x == val).count()
                        freqs[val] = f

            if set([type(key) for key in freqs.keys()]) == {np.float64}:
                freqs = {str(int(key)): int(freqs[key]) for key in freqs.keys()}
            else:
                freqs = {str(key): int(freqs[key]) for key in freqs.keys()}
            count = sum(freqs.values())
            if count > 0:
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
            try:
                xm = np.ma.masked_invalid(adni[x_name])
            except:
                pass
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

    results = {"results": results}
    with open('descr_stats_runs.json', 'w') as f:
        json.dump(results, f)


if __name__ == '__main__':
    main()
