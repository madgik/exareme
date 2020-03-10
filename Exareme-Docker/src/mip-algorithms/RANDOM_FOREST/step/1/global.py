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
from RF_lib import RFIter1_Loc2Glob_TD, RF_Glob2Loc_TD

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-local_step_dbs', required=True, help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()

    #1. Load previous global state and merge local outputs
    global_state = StateData.load(path.abspath(args.prev_state_pkl)).data
    rF_activePaths = RFIter1_Loc2Glob_TD.load(path.abspath(args.local_step_dbs)).get_data()

    #2. Run algorithm: -->pass

    #3. Save global state
    global_state1 = StateData(  stepsNo = global_state['stepsNo'] + 1  ,
                                args_Y = global_state['args_Y'],
                                args_n_trees = global_state['args_n_trees'] ,
                                args_max_depth = global_state['args_max_depth'],
                                rF_DataSchema = global_state['rF_DataSchema'],
                                rF_categoricalVariables = global_state['rF_categoricalVariables'],
                                rF_globalTree = global_state['rF_globalTree'],
                                rF_activePaths = rF_activePaths,
                                rF_Logs = global_state['rF_Logs'] )
    global_state1.save(fname= path.abspath(args.cur_state_pkl))

    #4. Return global output
    global_out = RF_Glob2Loc_TD(global_state['rF_globalTree'], rF_activePaths, global_state['rF_Logs'])
    global_out.transfer()

if __name__ == '__main__':
    main()
