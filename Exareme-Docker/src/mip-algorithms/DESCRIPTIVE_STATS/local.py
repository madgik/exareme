# Forward compatibility
from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import sqlite3

import numpy as np
import numpy.ma as ma

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
from algorithm_utils import query_with_privacy, ExaremeError, PrivacyError, PRIVACY_MAGIC_NUMBER

from lib import DescrStatsLocal_DT


def descr_stats_local(local_in):
    # Unpack data
    x, var_name, is_categorical = local_in

    if not is_categorical:
        xm = np.ma.masked_invalid(x)
        nn = xm.count()
        if nn < PRIVACY_MAGIC_NUMBER:
            raise PrivacyError('Removing missing values results in illegal number of datapoints in local db.')
        sx = xm.sum()
        sxx = (xm * xm).sum()
        xmin = xm.min()
        xmax = xm.max()

        local_out = DescrStatsLocal_DT(is_categorical, var_name, nn, sx, sxx, xmin, xmax)
    else:
        cats = set(x) - {''}
        freqs = dict()
        for cat in cats:
            freqs[cat] = x.count(cat)
        count = sum(freqs.values())

        local_out = DescrStatsLocal_DT(is_categorical, var_name, count, freqs)

    return local_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-x', required=True, help='Variable name.')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-db_query', required=True, help='Query to be executed on local db.')
    args, unknown = parser.parse_known_args()
    query = args.db_query
    fname_loc_db = path.abspath(args.input_local_DB)

    # Get data
    if args.x == '':
        raise ExaremeError('Field x must be non empty.')
    var_name = args.x
    _, data = query_with_privacy(fname_db=fname_loc_db, query=query)

    # Query metadata table to check if variable is categorical  (TODO rewrite this part correctly)
    conn = sqlite3.connect(fname_loc_db)
    cur = conn.cursor()
    cur.execute("select isCategorical from metadata where code = '" + var_name + "';")
    is_categorical = cur.fetchall()[0][0]
    ##############################################################################################
    if is_categorical:
        x = data
    else:
        x = np.array(data, dtype=np.float64)

    local_in = x, var_name, is_categorical

    # Run algorithm local step
    local_out = descr_stats_local(local_in=local_in)

    # Return the output data
    local_out.transfer()


if __name__ == '__main__':
    main()
