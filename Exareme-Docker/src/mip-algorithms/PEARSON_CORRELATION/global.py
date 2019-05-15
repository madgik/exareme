import sys
import math
import json
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

import numpy as np
import scipy.special as special

from algorithm_utils import get_parameters, set_algorithms_output_data
from pearsonc_lib import PearsonCorrelationLocalDT


def pearsonc_global(global_in):
    nn, sx, sy, sxx, sxy, syy, schema_X, schema_Y = global_in.get_data()
    n_cols = len(nn)
    schema_out = [None] * n_cols
    result_list = []
    for i in xrange(n_cols):
        schema_out[i] = schema_X[i] + '_' + schema_Y[i]
        # compute pearson correlation coefficient and p-value
        if nn[i] == 0:
            r = None
            prob = None
        else:
            d = (math.sqrt(nn[i] * sxx[i] - sx[i] * sx[i]) * math.sqrt(nn[i] * syy[i] - sy[i] * sy[i]))
            if d == 0:
                r = 0
            else:
                r = float((nn[i] * sxy[i] - sx[i] * sy[i]) / d)
            r = max(min(r, 1.0), -1.0)  # if abs(r) > 1 correct: artifact of floating point arithmetic.
            df = nn[i] - 2
            if abs(r) == 1.0:
                prob = 0.0
            else:
                t_squared = r ** 2 * (df / ((1.0 - r) * (1.0 + r)))
                prob = special.betainc(
                        0.5 * df, 0.5, np.fmin(np.asarray(df / (df + t_squared)), 1.0)
                )
        result_list.append({
            'Variable pair'                  : schema_out[i],
            'Pearson correlation coefficient': r,
            'p-value'                        : prob if prob >= 2.2e-16 else 0.0
        })
    global_out = json.dumps({'result': result_list})
    return global_out


def main():
    # read parameters
    parameters = get_parameters(sys.argv[1:])
    if not parameters or len(parameters) < 1:
        raise ValueError("There should be 1 parameter")
    # get data from local db
    localdbs = parameters.get("-local_step_dbs")
    if localdbs == None:
        raise ValueError("local_step_dbs not provided as parameter.")
    local_out = PearsonCorrelationLocalDT.load(localdbs)
    # run algorithm global step
    global_out = pearsonc_global(global_in=local_out)
    # return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
