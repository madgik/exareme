from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import sqlite3
import json
import pandas as pd

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART_PREDICT/')

from algorithm_utils import query_database, variable_categorical_getDistinctValues, StateData, PrivacyError, PRIVACY_MAGIC_NUMBER
from cartPredict_lib import cart_1_local, Cart_Loc2Glob_TD

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-x', required=True, help='Independent variable names, comma separated.')
    parser.add_argument('-y', required=True, help='Dependent variable name')
    parser.add_argument('-tree', required=True, help='Dependent variable name')
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-db_query', required=True, help='Query to be executed on local db.')
    args, unknown = parser.parse_known_args()

    #Get variable
    args_X = list(args.x.replace(' ', '').split(','))
    args_Y = [args.y.replace(' ', '')]
    args_globalTreeJ = json.loads(args.tree)

    init_logger()
    logging.warning("Init1Local: args_X, args_Y")
    logging.warning([args_X, args_Y])

    #1. Query database and metadata
    queryMetadata = "select * from metadata where code in (" + "'" + "','".join(args_X) + "','" + "','".join(args_Y) + "'"  + ");"
    dataSchema, metadataSchema, metadata, dataFrame  = query_database(fname_db=path.abspath(args.input_local_DB), queryData=args.db_query, queryMetadata=queryMetadata)
    categoricalVariables = variable_categorical_getDistinctValues(metadata)

    #2. Run algorithm
    confusionMatrix, mse, counts = cart_1_local(dataFrame, dataSchema, categoricalVariables, args_X, args_Y, args_globalTreeJ)

    #3. Save local state : Pass/

    #4. Transfer local output
    local_out = Cart_Loc2Glob_TD(args_X, args_Y, categoricalVariables, confusionMatrix, mse, counts)
    #raise ValueError( local_out.get_data())
    local_out.transfer()

if __name__ == '__main__':
    main()
