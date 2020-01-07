# Forward compatibility
from __future__ import division
from __future__ import print_function

import sys
from os import path, getcwd

import numpy as np
import numpy.ma as ma

_ALGORITHM_TYPE = 'python_multiple_local_global'

if _ALGORITHM_TYPE == 'python_local_global':
    dir_levels = 2
elif _ALGORITHM_TYPE == 'python_multiple_local_global':
    dir_levels = 3
elif _ALGORITHM_TYPE == 'python_iterative':
    if path.basename(getcwd()) == 'termination_condition':
        dir_levels = 3
    else:
        dir_levels = 4
else:
    raise ValueError('_ALGORITHM_TYPE unknown type.')
new_path = path.abspath(__file__)
for _ in range(dir_levels):
    new_path = path.dirname(new_path)
sys.path.append(new_path)

from utils.algorithm_utils import StateData, TransferData, parse_exareme_args, query_with_privacy, ExaremeError, \
    PrivacyError, PRIVACY_MAGIC_NUMBER


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
        local_out = TransferData(nn=(nn, 'add'), sx=(sx, 'add'))

    return local_state, local_out


def main(args):
    # TODO a function unpack_args to get all data
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
    # Transfer local output
    local_out.transfer()


if __name__ == '__main__':
    args = parse_exareme_args()
    main(args)
