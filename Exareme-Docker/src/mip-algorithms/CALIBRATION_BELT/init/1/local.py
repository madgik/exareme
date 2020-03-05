from __future__ import division
from __future__ import print_function

import sys
import sqlite3
from argparse import ArgumentParser
from os import path

import numpy as np
from scipy.special import logit

sys.path.append(
    path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/CALIBRATION_BELT/')

from algorithm_utils import StateData, PrivacyError
from cb_lib import CBInit_Loc2Glob_TD


# ======================= Remove after testing!! ======================= #
PRIVACY_MAGIC_NUMBER = 1

def query_with_privacy(fname_db, query):
    conn = sqlite3.connect(fname_db)
    cur = conn.cursor()
    cur.execute(query)
    schema = [description[0] for description in cur.description]
    data = cur.fetchall()
    if len(data) < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Query results in illegal number of datapoints.')
    return schema, data
# ====================================================================== #

def cb_local_init(local_in):
    # Unpack local input
    e_vec, o_vec, e_name, o_name, max_deg = local_in
    n_obs = len(e_vec)
    # Apply logit to e_vec
    ge_vec = logit(e_vec)
    X_matrices = dict()
    for deg in range(1, max_deg + 1):
        X = [np.ones(len(e_vec))]
        for d in range(1, deg + 1):
            X = np.append(X, [np.power(ge_vec, d)], axis=0)
        X_matrices[deg] = X.transpose()
    Y = o_vec
    e_domain = np.min(e_vec), np.max(e_vec)
    # Pack state and results
    local_state = StateData(X_matrices=X_matrices, Y=Y, max_deg=max_deg)
    local_out = CBInit_Loc2Glob_TD(n_obs, e_name, o_name, max_deg, e_domain)
    return local_state, local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-x', required=True, help='Expected outcomes.')
    parser.add_argument('-y', required=True, help='Observed outcomes.')
    parser.add_argument('-max_deg', required=True, help='Maximum degree of calibration curve.')
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-db_query', required=True, help='Query to be executed on local db.')
    args, unknown = parser.parse_known_args()
    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_loc_db = path.abspath(args.input_local_DB)
    query = args.db_query
    e_name = args.x.strip()
    o_name = args.y.strip()
    max_deg = int(args.max_deg)
    assert 1 < max_deg <= 4, "Max deg should be between 2 and 4 for `devel`=`external` or between 3 and 4 for " \
                             "`devel`=`internal`."

    # Get data from local DB
    schema, data = query_with_privacy(fname_db=fname_loc_db, query=query)
    idx_e = schema.index(e_name)
    idx_o = schema.index(o_name)
    e_vec = np.array([data[i][idx_e] for i in range(len(data))])
    o_vec = np.array([data[i][idx_o] for i in range(len(data))])

    # Remove rows with missing values
    mask_e = [ei is None for ei in e_vec]
    mask_o = [oi is None for oi in o_vec]
    mask = np.logical_or(mask_e, mask_o)
    e_vec, o_vec = np.array(e_vec[~mask], dtype=np.float64), np.array(o_vec[~mask], dtype=np.int8)
    # todo perform privacy check here!
    assert min(e_vec) >= 0. and max(e_vec) <= 1., "Variable e should take values only in [0, 1]"
    assert set(o_vec).issubset({0, 1}), "Variable o should only contain values 0 and 1."

    local_in = e_vec, o_vec, e_name, o_name, max_deg
    # Run algorithm local step
    local_state, local_out = cb_local_init(local_in=local_in)
    # Save local state
    local_state.save(fname=fname_cur_state)
    # Transfer local output (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main()
