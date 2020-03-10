from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import sqlite3
import json
import pandas as pd
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/RANDOM_FOREST/')

from algorithm_utils import init_logger, query_database, variable_categorical_getDistinctValues, StateData, ExaremeError, PRIVACY_MAGIC_NUMBER
from RF_lib import RFInit1_Loc2Glob_TD


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-x', required=True, help='Independent variable names, comma separated.')
    parser.add_argument('-y', required=True, help='Dependent variable name')
    parser.add_argument('-n_trees',  type=int, required=True, help='The number of trees in the forest.')
    parser.add_argument('-n_features',  type=int, required=True, help='The number of features to sample and pass onto each tree,')
    parser.add_argument('-sample_percentage', type=float, required=True, help='Percentage of rows randomly selected and passed onto each tree')
    parser.add_argument('-max_depth', type=int, required=True, help='Maximum depth of tree')
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-db_query', required=True, help='Query to be executed on local db.')
    args, unknown = parser.parse_known_args()

    # Get variable
    args_X = list(args.x.replace(' ', '').split(','))
    args_Y = [args.y.replace(' ', '')]
    args_n_trees =args.n_trees
    args_n_features = args.n_features
    args_sample_percentage = args.sample_percentage
    args_max_depth = args.max_depth

    init_logger()
    logging.warning("Init1Local: args_X, args_Y, args_n_trees, args_n_features, args_sample_percentage, args_max_depth")
    logging.warning([args_X, args_Y, args_n_trees, args_n_features, args_sample_percentage, args_max_depth])

    # Check inputs
    if args.n_features < len(args_X):
        raise ExaremeError('Field n_features must be positive integer of range: [1,' + str(len(args_X)) +'] ')


    #1. Query database and metadata
    queryMetadata = "select * from metadata where code in (" + "'" + "','".join(args_X) + "','" + "','".join(args_Y) + "'"  + ");"
    dataSchema, metadataSchema, metadata, dataFrame  = query_database(fname_db = path.abspath(args.input_local_DB), queryData=args.db_query, queryMetadata=queryMetadata)
    categoricalVariables = variable_categorical_getDistinctValues(metadata)

    #2. Run algorithm : Pass

    #3. Save local state
    local_state = StateData( args_X = args_X,
                             args_Y = args_Y,
                             args_n_trees = args_n_trees,
                             args_n_features = args_n_features,
                             args_sample_percentage = args_sample_percentage,
                             args_max_depth = args_max_depth,
                             dataFrame = dataFrame,
                             dataSchema = dataSchema,
                             categoricalVariables = categoricalVariables)

    local_state.save(fname = path.abspath(args.cur_state_pkl))

    #4. Transfer local output
    local_out = RFInit1_Loc2Glob_TD(args_X, args_Y, args_n_trees, args_n_features, args_sample_percentage, args_max_depth, dataSchema, categoricalVariables)

    local_out.transfer()

if __name__ == '__main__':
    main()
