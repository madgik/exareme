
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

from algorithm_utils import init_logger, StateData
from RF_lib import RFInit1_Glob2Loc_TD, RF_init_2_local, RFInit2_Loc2Glob_TD


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-global_step_db', required=True, help='Path to db holding global step results.')
    args, unknown = parser.parse_known_args()

    #1. Load previous local state and global output
    local_state = StateData.load(path.abspath(args.prev_state_pkl)).data
    rF_DataSchema, rF_categoricalVariables = RFInit1_Glob2Loc_TD.load(path.abspath(args.global_step_db)).get_data()

    #2. Run algorithm
    rF_dataFrame, rF_Logs = RF_init_2_local(local_state['args_Y'], local_state['dataFrame'], local_state['args_sample_percentage'], local_state['args_n_trees'], rF_DataSchema, rF_categoricalVariables )

    init_logger()
    logging.warning("Init2Local: rF_Logs,len(rF_dataFrame[i]), len(rF_dataFrame)")
    logging.warning([rF_Logs, [rF_dataFrame[i].count for i in range(local_state['args_n_trees'])], local_state['dataFrame'].count])

    #3. Save local state
    local_state = StateData( #args_X = local_state['args_X'],
                             args_Y = local_state['args_Y'],
                             args_n_trees = local_state['args_n_trees'],
                             args_n_features = local_state['args_n_features'],
                             args_sample_percentage = local_state['args_sample_percentage'],
                             args_max_depth = local_state['args_max_depth'],
                             #dataFrame = local_state['dataFrame'],
                             #dataSchema = local_state['dataSchema'],
                             #categoricalVariables = local_state['categoricalVariables'],
                             rF_dataFrame = rF_dataFrame,
                             rF_DataSchema = rF_DataSchema,
                             rF_categoricalVariables = rF_categoricalVariables,
                             rF_Logs = rF_Logs )
    local_state.save(fname = path.abspath(args.cur_state_pkl))

    #4. Transfer local output
    local_out = RFInit2_Loc2Glob_TD(rF_Logs)

    local_out.transfer()

if __name__ == '__main__':
    main()
