# Forward compatibility
from __future__ import division
from __future__ import print_function

import sys
from os import path
import json
from argparse import ArgumentParser

import numpy as np
import numpy.ma as ma

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import query_from_formula
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
    X, Y, schema_X, schema_Y, correlmatr_row_names, correlmatr_col_names = local_in
    n_obs, n_cols = len(X), len(X[0])

    # Init statistics
    nn = np.empty(n_cols, dtype=np.int)
    sx = np.empty(n_cols, dtype=np.float)
    sy = np.empty(n_cols, dtype=np.float)
    sxx = np.empty(n_cols, dtype=np.float)
    sxy = np.empty(n_cols, dtype=np.float)
    syy = np.empty(n_cols, dtype=np.float)

    for i in xrange(n_cols):
        x, y = X[:, i], Y[:, i]
        # Compute local statistics
        nn[i] = n_obs
        sx[i] = x.sum()
        sy[i] = y.sum()
        sxx[i] = (x * x).sum()
        sxy[i] = (x * y).sum()
        syy[i] = (y * y).sum()
        local_out = PearsonCorrelationLocalDT(
                (nn, sx, sy, sxx, sxy, syy, schema_X, schema_Y, correlmatr_row_names, correlmatr_col_names))

    return local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    # Algo arguments
    parser.add_argument('-variables', required=True, help='List of variables involved.')
    parser.add_argument('-formula', required=True, help='A string holding a patsy formula.')
    parser.add_argument('-no_intercept', required=True, help='A boolean signaling a no-intercept-by-default behaviour.')
    # Exareme arguments
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-data_table', required=True)
    parser.add_argument('-metadata_table', required=True)
    parser.add_argument('-metadata_code_column', required=True)
    parser.add_argument('-metadata_isCategorical_column', required=True)

    args, unknown = parser.parse_known_args()
    varibles = json.loads(args.variables)
    formula = json.loads(args.formula)
    no_intercept = json.loads(args.no_intercept)
    input_local_DB = args.input_local_DB
    data_table = args.data_table
    metadata_table = args.metadata_table
    metadata_code_column = args.metadata_code_column
    metadata_isCategorical_column = args.metadata_isCategorical_column
    Y, X = query_from_formula(fname_db=input_local_DB, formula=formula, variables=varibles,
                       data_table=data_table, metadata_table=metadata_table,
                       metadata_code_column=metadata_code_column,
                       metadata_isCategorical_column=metadata_isCategorical_column,
                       no_intercept=no_intercept)

    schema_X, schema_Y = [], []
    if Y == None:
        for i in xrange(len(X.columns)):
            for j in xrange(i + 1, len(X.columns)):
                schema_X.append(X.design_info.column_names[i])
                schema_Y.append(X.design_info.column_names[j])
        correlmatr_row_names = X.design_info.column_names
        correlmatr_col_names = X.design_info.column_names
    else:
        for i in xrange(len(X.columns)):
            for j in xrange(len(Y.columns)):
                schema_X.append(X.design_info.column_names[i])
                schema_Y.append(Y.design_info.column_names[j])
        correlmatr_col_names = X.design_info.column_names
        correlmatr_row_names = Y.design_info.column_names
    local_in = np.asarray(X), np.asarray(Y), schema_X, schema_Y, correlmatr_row_names, correlmatr_col_names

    # Run algorithm local step
    local_out = pearsonr_local(local_in=local_in)

    # Return the output data (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main()
