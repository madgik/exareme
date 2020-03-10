from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import pandas as pd
import numpy as np
import json
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/RANDOM_FOREST/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')

from algorithm_utils import init_logger, StateData
from RF_lib import RF_Glob2Loc_TD, RFIter3_Loc2Glob_TD, RF_step_3_local

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-global_step_db', required=True, help='Path to db holding global step results.')
    args, unknown = parser.parse_known_args()

    #1. Load previous local state and global output
    local_state = StateData.load(path.abspath(args.prev_state_pkl)).data
    rF_globalTree, rF_activePaths, rF_Logs = RF_Glob2Loc_TD.load(path.abspath(args.global_step_db)).get_data()

    init_logger()
    logging.warning("Step2Local: rF_globalTree, rF_activePaths, rF_Logs")
    logging.warning([rF_globalTree, rF_activePaths, rF_Logs])

    #2. Run algorithm local iteration step
    rF_activePaths = RF_step_3_local(local_state['args_Y'], local_state['args_n_trees'], local_state['rF_dataFrame'], local_state['rF_DataSchema'], local_state['rF_categoricalVariables'], rF_activePaths, rF_Logs)

    #3. Save local state
    local_state = StateData( #args_X = local_state['args_X'],
                             args_Y = local_state['args_Y'],
                             args_n_trees = local_state['args_n_trees'],
                             args_n_features = local_state['args_n_features'],
                             args_sample_percentage = local_state['args_sample_percentage'],
                             args_max_depth = local_state['args_max_depth'],
                             rF_dataFrame = local_state['rF_dataFrame'],
                             rF_DataSchema = local_state['rF_DataSchema'],
                             rF_categoricalVariables = local_state['rF_categoricalVariables'])
    local_state.save(fname=path.abspath(args.cur_state_pkl))

    #4. Transfer local output
    local_out = RFIter3_Loc2Glob_TD(rF_activePaths)
    local_out.transfer()

if __name__ == '__main__':
    main()
