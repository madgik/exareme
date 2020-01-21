from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import sys
from argparse import ArgumentParser
from os import path

import numpy as np

sys.path.append(
        path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData, query_from_formula, ExaremeError
from log_regr_lib import LogRegrInit_Loc2Glob_TD


def logregr_local_init(local_in):
    # Unpack local input
    Y, X = local_in
    n_obs = len(Y)
    n_cols = len(X.columns)
    Y = Y.iloc[:, 1]
    y_name = Y.name
    x_names = list(X.columns)
    Y, X = np.array(Y), np.array(X)

    # Pack state and results
    local_state = StateData(X=X, Y=Y)
    local_out = LogRegrInit_Loc2Glob_TD(n_obs, n_cols, y_name, x_names)
    return local_state, local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    # Algo arguments
    parser.add_argument('-x', required=True, help='Variable names in x, comma separated.')
    parser.add_argument('-y', required=True, help='Variable names in y, comma separated.')
    parser.add_argument('-dataset', required=True)
    parser.add_argument('-formula', required=True, help='A string holding a patsy formula.')
    # Exareme arguments
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-data_table', required=True)
    parser.add_argument('-metadata_table', required=True)
    parser.add_argument('-metadata_code_column', required=True)
    parser.add_argument('-metadata_isCategorical_column', required=True)

    args, unknown = parser.parse_known_args()
    args_x = list(
            args.x
                .replace(' ', '')
                .split(',')
    )
    args_y = args.y.strip()
    varibles = ([args_y], args_x)
    dataset = args.dataset
    formula = args.formula
    formula = formula.replace('_', '~')  # TODO Fix tilda problem and remove

    cur_state_pkl = args.cur_state_pkl
    input_local_DB = args.input_local_DB
    data_table = args.data_table
    metadata_table = args.metadata_table
    metadata_code_column = args.metadata_code_column
    metadata_isCategorical_column = args.metadata_isCategorical_column

    # Get data from local DB
    Y, X = query_from_formula(fname_db=input_local_DB,
                              formula=formula,
                              variables=varibles,
                              dataset=dataset,
                              data_table=data_table,
                              metadata_table=metadata_table,
                              metadata_code_column=metadata_code_column,
                              metadata_isCategorical_column=metadata_isCategorical_column,
                              no_intercept=False,
                              coding=None)
    if len(Y.columns) != 2:
        raise ExaremeError('Variable must contain only two categories.')
    local_in = Y, X

    # Run algorithm local step
    local_state, local_out = logregr_local_init(local_in=local_in)
    # Save local state
    local_state.save(fname=cur_state_pkl)
    # Transfer local output (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main()
