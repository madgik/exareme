from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import pandas as pd
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/RANDOM_FOREST/')

from algorithm_utils import init_logger, StateData, ExaremeError
from RF_lib import RFInit1_Loc2Glob_TD, RFInit1_Glob2Loc_TD, RF_init_1_global

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-local_step_dbs', required=True, help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()

    #1. Merge local outputs
    args_X, args_Y, args_n_trees, args_n_features, args_sample_percentage, args_max_depth, dataSchema, categoricalVariables = RFInit1_Loc2Glob_TD.load(path.abspath(args.local_step_dbs)).get_data()

    init_logger()
    logging.warning("Init1Global:args_X, args_Y, args_n_trees, args_n_features, args_sample_percentage, args_max_depth, dataSchema, categoricalVariables")
    logging.warning([args_X, args_Y, args_n_trees, args_n_features, args_sample_percentage, args_max_depth, dataSchema, categoricalVariables])

    #2. Run algorithm global step
    rF_DataSchema, rF_categoricalVariables = RF_init_1_global(args_X, args_Y, dataSchema, categoricalVariables, args_n_trees, args_n_features)

    logging.warning("Init1Global: rF_DataSchema, rF_categoricalVariables")
    logging.warning([rF_DataSchema, rF_categoricalVariables])

    #3. Save global state
    global_state = StateData( #args_X = args_X,
                              args_Y = args_Y,
                              args_n_trees = args_n_trees,
                              args_max_depth = args_max_depth,
                              rF_DataSchema = rF_DataSchema,
                              rF_categoricalVariables = rF_categoricalVariables )
    global_state.save(fname=path.abspath(args.cur_state_pkl))

    #4. Transfer global output
    global_out = RFInit1_Glob2Loc_TD(rF_DataSchema, rF_categoricalVariables)
    global_out.transfer()


if __name__ == '__main__':
    main()
