from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import sqlite3
import json
import pandas as pd
import time

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')

from algorithm_utils import query_database, variable_categorical_getDistinctValues, StateData, PrivacyError, PRIVACY_MAGIC_NUMBER
from cart_lib import CartInit_Loc2Glob_TD, cart_init_1_local

def main(args):

    t1 = time.localtime(time.time())

    # Parse arguments
    sys.argv =args
    parser = ArgumentParser()
    parser.add_argument('-x', required=True, help='Independent variable names, comma separated.')
    parser.add_argument('-y', required=True, help='Dependent variable name')
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-db_query', required=True, help='Query to be executed on local db.')
    args, unknown = parser.parse_known_args()
    query = args.db_query
    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_loc_db = path.abspath(args.input_local_DB)

    # Get variable
    args_X = list(args.x.replace(' ', '').split(','))
    args_Y = [args.y.replace(' ', '')]

    #1. Query database and metadata
    queryMetadata = "select * from metadata where code in (" + "'" + "','".join(args_X) + "','" + "','".join(args_Y) + "'"  + ");"
    dataSchema, metadataSchema, metadata, dataFrame  = query_database(fname_db=fname_loc_db, queryData=query, queryMetadata=queryMetadata)
    CategoricalVariables = variable_categorical_getDistinctValues(metadata)

    #2. Run algorithm
    dataFrame, CategoricalVariables = cart_init_1_local(dataFrame, dataSchema, CategoricalVariables)
    if len(dataFrame) < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('The Experiment could not run with the input provided because there are insufficient data.')

    #3. Save local state
    local_state = StateData( dataFrame = dataFrame,
                             args_X = args_X,
                             args_Y = args_Y,
                             CategoricalVariables = CategoricalVariables)
    local_state.save(fname = fname_cur_state)

    # Transfer local output
    local_out = CartInit_Loc2Glob_TD(args_X, args_Y, CategoricalVariables, t1)
    local_out.transfer()

if __name__ == '__main__':
    main()
