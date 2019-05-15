from __future__ import division
from __future__ import print_function

import sys
import math
import json
from os import path
import numpy as np
import scipy.special as special
import scipy.stats as st
from argparse import ArgumentParser

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import set_algorithms_output_data
from pearsonc_lib import PearsonCorrelationLocalDT


def pearsonr_global(global_in):
    """Global step in Pearson correlation coefficient. Local statistics, computed in local step, are aggregated and
    then Pearson correlation coefficient `r`, p-value `prob` and lower and upper confidence intervals at 95% `ci_lo`
    and `ci_hi` are computed. Pearson correlation is computed according to standard formula
    (see https://en.wikipedia.org/wiki/Pearson_correlation_coefficient#For_a_sample). The p-value is computed using
    the incomplete beta integral method. The lower and upper confidence intervals are computed usign the Fisher
    information (see https://en.wikipedia.org/wiki/Pearson_correlation_coefficient#Using_the_Fisher_transformation).

    Parameters
    ----------
    global_in : PearsonCorrelationLocalDT
        Object holding aggregated values of statistics computed in local step.

    Returns
    -------
    global_out : str
        JSON string containing a list of results, one for each variable pair, where each result hold the variable
        pair names, the Pearson coefficient, the p-value and the lower and upper confidence intervals.
    """
    nn, sx, sy, sxx, sxy, syy, schema_X, schema_Y = global_in.get_data()
    n_cols = len(nn)
    schema_out = [None] * n_cols
    result_list = []
    for i in xrange(n_cols):
        schema_out[i] = schema_X[i] + '_' + schema_Y[i]
        # Compute pearson correlation coefficient and p-value
        if nn[i] == 0:
            r = None
            prob = None
        else:
            d = (math.sqrt(nn[i] * sxx[i] - sx[i] * sx[i]) * math.sqrt(nn[i] * syy[i] - sy[i] * sy[i]))
            if d == 0:
                r = 0
            else:
                r = float((nn[i] * sxy[i] - sx[i] * sy[i]) / d)
            r = max(min(r, 1.0), -1.0)  # If abs(r) > 1 correct it: artifact of floating point arithmetic.
            df = nn[i] - 2
            if abs(r) == 1.0:
                prob = 0.0
            else:
                t_squared = r ** 2 * (df / ((1.0 - r) * (1.0 + r)))
                prob = special.betainc(
                        0.5 * df, 0.5, np.fmin(np.asarray(df / (df + t_squared)), 1.0)
                )
        # Compute 95% confidence intervals
        alpha = 0.05  # Two-tail test with confidence intervals 95%
        r_z = np.arctanh(r)
        se = 1 / np.sqrt(nn[i] - 3)
        z = st.norm.ppf(1 - alpha / 2)
        lo_z, hi_z = r_z - z * se, r_z + z * se
        ci_lo, ci_hi = np.tanh((lo_z, hi_z))
        result_list.append({
            'Variable pair'                  : schema_out[i],
            'Pearson correlation coefficient': r,
            'p-value'                        : prob if prob >= 0.001 else '< 0.001',
            'C.I. Lower'                     : ci_lo,
            'C.I. Upper'                     : ci_hi
        })
    global_out = json.dumps({'result': result_list})
    return global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = PearsonCorrelationLocalDT.load(local_dbs)
    # Run algorithm global step
    global_out = pearsonr_global(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
