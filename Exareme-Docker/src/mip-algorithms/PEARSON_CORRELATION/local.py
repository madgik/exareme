# Forward compatibility
from __future__ import division
from __future__ import print_function

import json
import sys
from argparse import ArgumentParser
from os import path

import numpy as np

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import query_from_formula
from pearsonc_lib import PearsonCorrelationLocalDT


def pearsonr_local(local_in):
    # TODO Rewrite docstring
    """Local step in Pearson correlation coefficient. Statistics are computed in each local database and then they
    are sent to the master node to be aggregated accordingly.
    The computed statistics are: `nn` number of observations, `sx` and `sy` sums of linear terms \sum_i x_i and
    \sum_i y_i respectively, `sxx`, `sxy` and `syy` sums of quadratic terms \sum_i x_i x_i, \sum_i x_i y_i and
    \sum_i y_i y_i respectively, where x and y are vectors of the pair of variables under consideration, pulled from
    matrices right_vars and left_vars respectively.

    Parameters
    ----------
    local_in : numpy.array, numpy.array, list, list
        Tuple holding matrices right_vars and left_vars as numpy arrays and lists of variable names schema_X and schema_Y

    Returns
    -------
    local_out: PearsonCorrelationLocalDT
       Object holding the computed statistics as well as schema_X, schema_Y do be transferred to the master node.
    """
    # Unpack data
    left_vars, right_vars = local_in

    n_obs = len(right_vars)
    cm_shape = len(left_vars.columns), len(right_vars.columns)

    # Compute statistics
    sx = np.empty(cm_shape, dtype=np.float)
    sy = np.empty(cm_shape, dtype=np.float)
    sxx = np.empty(cm_shape, dtype=np.float)
    sxy = np.empty(cm_shape, dtype=np.float)
    syy = np.empty(cm_shape, dtype=np.float)
    cm_names = np.array([[''] * cm_shape[1]] * cm_shape[0], dtype=object)
    for i in xrange(cm_shape[0]):
        for j in xrange(cm_shape[1]):
            y, x = left_vars.iloc[:, i], right_vars.iloc[:, j]
            sx[i, j] = x.sum()
            sy[i, j] = y.sum()
            sxx[i, j] = (x * x).sum()
            sxy[i, j] = (x * y).sum()
            syy[i, j] = (y * y).sum()
            cm_names[i, j] = y.name + ' ~ ' + x.name

    local_out = PearsonCorrelationLocalDT(
            n_obs, sx, sy, sxx, sxy, syy, cm_names, list(left_vars.columns), list(right_vars.columns))

    return local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    # Algo arguments
    parser.add_argument('-x', required=True, help='Variable names in x, comma separated.')
    parser.add_argument('-y', required=True, help='Variable names in y, comma separated.')
    parser.add_argument('-dataset', required=True, help='Dataset name.')
    parser.add_argument('-formula', required=True, help='A string holding a patsy formula.')
    parser.add_argument('-no_intercept', required=True, help='A boolean signaling a no-intercept-by-default behaviour.')
    parser.add_argument('-coding', required=True, help='Coding method for categorical variables.')
    # Exareme arguments
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-data_table', required=True)
    parser.add_argument('-metadata_table', required=True)
    parser.add_argument('-metadata_code_column', required=True)
    parser.add_argument('-metadata_isCategorical_column', required=True)

    args, unknown = parser.parse_known_args()

    args_y = list(
            args.y
                .replace(' ', '')
                .split(',')
    )
    if args.x == '':
        variables = (args_y,)
    else:
        args_x = list(
                args.x
                    .replace(' ', '')
                    .split(',')
        )
        variables = (args_y, args_x)

    dataset = args.dataset
    formula = args.formula
    formula = formula.replace('_', '~')  # TODO Fix tilda problem and remove
    no_intercept = json.loads(args.no_intercept)
    input_local_DB = args.input_local_DB
    data_table = args.data_table
    metadata_table = args.metadata_table
    metadata_code_column = args.metadata_code_column
    metadata_isCategorical_column = args.metadata_isCategorical_column
    coding = None if args.coding == 'null' else args.coding
    left_vars, right_vars = query_from_formula(fname_db=input_local_DB, formula=formula, variables=variables,
                                               dataset=dataset,
                                               data_table=data_table, metadata_table=metadata_table,
                                               metadata_code_column=metadata_code_column,
                                               metadata_isCategorical_column=metadata_isCategorical_column,
                                               no_intercept=no_intercept, coding=coding)

    if left_vars is None:
        left_vars = right_vars
    local_in = left_vars, right_vars

    # Run algorithm local step
    local_out = pearsonr_local(local_in=local_in)

    # Return the output data (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main()
