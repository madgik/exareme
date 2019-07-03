# Forward compatibility
from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser

import numpy as np
import numpy.ma as ma

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/PCA/')

from algorithm_utils import StateData, query_with_privacy, ExaremeError, PrivacyError, PRIVACY_MAGIC_NUMBER
from lib import PCA1_Loc2Glob_TD


def pca_local(local_in):
    # Unpack data
    X, schema_X = local_in
    n_obs, n_cols = len(X), len(X[0])

    # Init statistics
    nn = np.empty(n_cols, dtype=np.int)
    sx = np.empty(n_cols, dtype=np.float)

    mask = [True in np.isnan(X[row, :]) for row in range(n_obs)]
    for i in xrange(n_cols):
        x = X[:, i]
        xm = ma.masked_array(x, mask=mask)
        nn[i] = n_obs - sum(mask)
        if nn[i] < PRIVACY_MAGIC_NUMBER:
            raise PrivacyError('Removing missing values results in illegal number of datapoints in local db.')
        sx[i] = xm.sum()

        local_state = StateData(X=X, schema_X=schema_X)
        local_out = PCA1_Loc2Glob_TD(nn, sx)

    return local_state, local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-x', required=True, help='Variable names in x, comma separated.')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-db_query', required=True, help='Query to be executed on local db.')
    args, unknown = parser.parse_known_args()
    query = args.db_query
    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_loc_db = path.abspath(args.input_local_DB)
    if args.x == '':
        raise ExaremeError('Field x must be non empty.')
    schema_X = list(
            args.x
                .replace(' ', '')
                .split(',')
    )
    schema, data = query_with_privacy(fname_db=fname_loc_db, query=query)
    idx_X = [schema.index(v) for v in schema_X if v in schema]
    data = np.array(data, dtype=np.float64)
    X = data[:, idx_X]
    local_in = X, schema

    # Run algorithm local step
    local_state, local_out = pca_local(local_in=local_in)
    # Save local state
    local_state.save(fname=fname_cur_state)
    # Transfer local outpu
    local_out.transfer()


if __name__ == '__main__':
    main()
