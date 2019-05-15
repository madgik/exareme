import sys
import sqlite3
import math
import csv
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

import numpy as np
import numpy.ma as ma

# from utils.algorithm_utils import get_parameters
from algorithm_utils import get_parameters
from pearsonc_lib import PearsonCorrelationLocalDT


def pearsonc_local(X, Y, schema_X, schema_Y):
    n_obs, n_cols = len(X), len(X[0])
    if (len(Y), len(Y[0])) != (n_obs, n_cols):
        raise ValueError('Matrices X and Y should have the same size')

    # create output schema forming x, y variable pairs
    schema_out = [None] * (n_cols)
    for i in xrange(n_cols):
        schema_out[i] = schema_X[i] + '_' + schema_Y[i]

    # init vars
    nn = np.empty(n_cols, dtype=np.int)
    sx = np.empty(n_cols, dtype=np.float)
    sy = np.empty(n_cols, dtype=np.float)
    sxx = np.empty(n_cols, dtype=np.float)
    sxy = np.empty(n_cols, dtype=np.float)
    syy = np.empty(n_cols, dtype=np.float)

    # iterate on variable pairs
    for i in xrange(n_cols):
        # create masked arrays
        x, y = X[:, i], Y[:, i]
        mask = [np.isnan(xi) or np.isnan(xi) for xi, yi in zip(x, y)]
        xm = ma.masked_array(x, mask=mask)
        ym = ma.masked_array(y, mask=mask)
        # compute statistics
        nn[i] = n_obs - sum(mask)
        sx[i] = xm.filled(0).sum()
        sy[i] = ym.filled(0).sum()
        sxx[i] = (xm.filled(0) * xm.filled(0)).sum()
        sxy[i] = (xm.filled(0) * ym.filled(0)).sum()
        syy[i] = (ym.filled(0) * ym.filled(0)).sum()
        local_out = PearsonCorrelationLocalDT((nn, sx, sy, sxx, sxy, syy, schema_X, schema_Y))

    return local_out


def main():
    # read parameters
    parameters = get_parameters(sys.argv[1:])
    if not parameters or len(parameters) < 1:
        raise ValueError("There should be 1 parameter")
    # get db path
    fname_db = parameters.get("-input_local_DB")
    if fname_db == None:
        raise ValueError("input_local_DB not provided as parameter.")
    # get query
    query = parameters['-db_query']
    if query == None:
        raise ValueError('db_query not provided as parameter.')
    # read data from csv file
    conn = sqlite3.connect(fname_db)
    cur = conn.cursor()
    c = cur.execute(query)
    schema = [description[0] for description in cur.description]
    n_cols = len(schema) // 2
    data = np.array(cur.fetchall(), dtype=np.float64)
    schema_X, schema_Y = schema[:n_cols], schema[n_cols:]
    X, Y = data[:, :n_cols], data[:, n_cols:]

    # run algorithm local step
    local_out = pearsonc_local(X, Y, schema_X, schema_Y)

    # return the output data (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main()
