from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import pandas as pd

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/CART_PREDICT/')

from algorithm_utils import init_logger, StateData, set_algorithms_output_data
from cartPredict_lib import cart_1_global, Cart_Loc2Glob_TD

def main():
    # Parse arguments
    parser = ArgumentParser()
    #parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-local_step_dbs', required=True, help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()

    #1. Merge local outputs
    args_X, args_Y, categoricalVariables, confusionMatrix, mse, counts, predictions = Cart_Loc2Glob_TD.load(path.abspath(args.local_step_dbs)).get_data()


    global_out = cart_1_global(args_X, args_Y, categoricalVariables, confusionMatrix, mse, counts, predictions)
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
