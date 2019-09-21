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

from lib import DescrStatsLocal_DT, Variable, TransferVariable


def descr_stats_local(local_in):
    # Unpack data
    var_list = local_in
    transf_var_list = []
    for i in xrange(len(var_list)):
        x, var_name, is_categorical, enums = var_list[i].get_data()

        if not is_categorical:
            xm = np.ma.masked_invalid(x)
            nn = xm.count()
            if nn < PRIVACY_MAGIC_NUMBER:
                raise PrivacyError('Removing missing values results in illegal number of datapoints in local db.')
            sx = xm.sum()
            sxx = (xm * xm).sum()
            xmin = xm.min()
            xmax = xm.max()
            transf_var = TransferVariable(is_categorical, var_name, nn, sx, sxx, xmin, xmax)
            transf_var_list.append(transf_var)

        else:
            freqs = dict()
            for enum in enums:
                freqs[enum] = x.count(enum)
            count = sum(freqs.values())
            transf_var = TransferVariable(is_categorical, var_name, count, freqs)
            transf_var_list.append(transf_var)


    local_out = DescrStatsLocal_DT(transf_var_list)
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

    conn = sqlite3.connect(fname_loc_db)
    cur = conn.cursor()

    # Get data
    var_names = list(
            args.x
                .replace(' ', '')
                .split(',')
    )
    schema, data = query_with_privacy(fname_db=fname_loc_db, query=query)
    idx_X = [schema.index(v) for v in var_names if v in schema]
    var_list = []
    for idx in idx_X:
        var_name = schema[idx]

        # Query metadata table for isCategorical and enumerations  (TODO rewrite this part correctly)
        cur.execute("select isCategorical from metadata where code = '" + var_name + "';")
        is_categorical = cur.fetchall()[0][0]
        if is_categorical:
            cur.execute("select enumerations from metadata where code = '" + var_name + "';")
            enums = cur.fetchall()[0][0].split(',')
        else:
            enums = None
        ##############################################################################################
        if is_categorical:
            x = [d[0] for d in data]
        else:
            x = np.array(data, dtype=np.float64)
        var_list.append(Variable(var_name, x, is_categorical, enums))

    local_in = var_list

    # Run algorithm local step
    local_out = descr_stats_local(local_in=local_in)

    # Return the output data
    local_out.transfer()


if __name__ == '__main__':
    main()
