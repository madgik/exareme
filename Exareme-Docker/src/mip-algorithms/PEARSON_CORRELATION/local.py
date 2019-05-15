from __future__ import division
from __future__ import print_function

import sys
import sqlite3
from os import path
from argparse import ArgumentParser

import numpy as np
import numpy.ma as ma

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from pearsonc_lib import PearsonCorrelationLocalDT


def pearsonr_local(local_in):
    """Local step in Pearson correlation coefficient. Statistics are computed in each local database and then they
    are sent to the master node to be aggregated accordingly.
    The computed statistics are: `nn` number of observations, `sx` and `sy` sums of linear terms \sum_i x_i and
    \sum_i y_i respectively, `sxx`, `sxy` and `syy` sums of quadratic terms \sum_i x_i x_i, \sum_i x_i y_i and
    \sum_i y_i y_i respectively, where x and y are vectors of the pair of variables under consideration, pulled from
    matrices X and Y respectively.

    Parameters
    ----------
    local_in : numpy.array, numpy.array, list, list
        Tuple holding matrices X and Y as numpy arrays and lists of variable names schema_X and schema_Y

    Returns
    -------
    local_out: PearsonCorrelationLocalDT
       Object holding the computed statistics as well as schema_X, schema_Y do be transferred to the master node.
    """
    # Unpack data
    X, Y, schema_X, schema_Y = local_in
    n_obs, n_cols = len(X), len(X[0])
    assert (len(Y), len(Y[0])) == (n_obs, n_cols), 'Matrices X and Y should have the same size'

    # Create output schema forming x, y variable pairs
    schema_out = [None] * (n_cols)
    for i in xrange(n_cols):
        schema_out[i] = schema_X[i] + '_' + schema_Y[i]

    # Init statistics
    nn = np.empty(n_cols, dtype=np.int)
    sx = np.empty(n_cols, dtype=np.float)
    sy = np.empty(n_cols, dtype=np.float)
    sxx = np.empty(n_cols, dtype=np.float)
    sxy = np.empty(n_cols, dtype=np.float)
    syy = np.empty(n_cols, dtype=np.float)

    # Create mask
    mask = [True in np.isnan(X[row, :]) or True in np.isnan(Y[row, :]) for row in range(n_obs)]
    for i in xrange(n_cols):
        # Create masked arrays
        x, y = X[:, i], Y[:, i]
        xm = ma.masked_array(x, mask=mask)
        ym = ma.masked_array(y, mask=mask)
        # Compute local statistics
        nn[i] = n_obs - sum(mask)
        sx[i] = xm.filled(0).sum()
        sy[i] = ym.filled(0).sum()
        sxx[i] = (xm.filled(0) * xm.filled(0)).sum()
        sxy[i] = (xm.filled(0) * ym.filled(0)).sum()
        syy[i] = (ym.filled(0) * ym.filled(0)).sum()
        local_out = PearsonCorrelationLocalDT((nn, sx, sy, sxx, sxy, syy, schema_X, schema_Y))

    return local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-X', required=True, help='Variable names in X, comma separated.')
    parser.add_argument('-Y', required=True, help='Variable names in Y, comma separated.')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-db_query', required=True, help='Query to be executed on local db.')
    args, unknown = parser.parse_known_args()
    query = args.db_query
    fname_loc_db = path.abspath(args.input_local_DB)
    args_X = list(
            args.X
                .replace(' ', '')
                .split(',')
    )
    args_Y = list(
            args.Y
                .replace(' ', '')
                .split(',')
    )
    # Populate schemata, treating cases Y=empty and Y=not empty accordingly (R function `cor` is imitated)
    schema_X, schema_Y = [], []
    if args_Y == ['']:
        for i in range(len(args_X)):
            for j in range(i + 1, len(args_X)):
                schema_X.append(args_X[i])
                schema_Y.append(args_X[j])
    else:
        for i in range(len(args_X)):
            for j in range(len(args_Y)):
                schema_X.append(args_X[i])
                schema_Y.append(args_Y[j])

    # Read data and split between X and Y matrices according to schemata
    conn = sqlite3.connect(fname_loc_db)
    cur = conn.cursor()
    cur.execute(query)
    schema = [description[0] for description in cur.description]
    try:
        data = np.array(cur.fetchall(), dtype=np.float64)
    except ValueError:
        print('Values in X and Y must be numbers or blanks')
    idx_X = [schema.index(v) for v in schema_X if v in schema]
    idx_Y = [schema.index(v) for v in schema_Y if v in schema]
    X = data[:, idx_X]
    Y = data[:, idx_Y]
    local_in = X, Y, schema_X, schema_Y

    # Run algorithm local step
    local_out = pearsonr_local(local_in=local_in)

    # Return the output data (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main()
