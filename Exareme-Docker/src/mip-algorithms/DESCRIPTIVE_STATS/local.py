# Forward compatibility
from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser

import numpy as np
import numpy.ma as ma

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
from algorithm_utils import query_with_privacy, ExaremeError, PrivacyError, PRIVACY_MAGIC_NUMBER

from lib import DescrStatsLocalDT


def descr_stats_local(local_in):
    # Unpack data
    x, var_name = local_in

    xm = np.ma.masked_invalid(x)
    nn = xm.count()
    if nn < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Removing missing values results in illegal number of datapoints in local db.')
    sx = xm.sum()
    sxx = (xm * xm).sum()
    xmin = xm.min()
    xmax = xm.max()

    local_out = DescrStatsLocalDT(nn, sx, sxx, xmin, xmax, var_name)

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
    if args.x == '':
        raise ExaremeError('Field x must be non empty.')
    var_name = args.x
    _, data = query_with_privacy(fname_db=fname_loc_db, query=query)
    x = np.array(data, dtype=np.float64)
    local_in = x, var_name

    # Run algorithm local step
    local_out = descr_stats_local(local_in=local_in)

    # Return the output data
    local_out.transfer()


if __name__ == '__main__':
    main()
