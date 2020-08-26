from __future__ import division
from __future__ import print_function

import sys
from os import path
import numpy as np
from argparse import ArgumentParser
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/MULTIPLE_HISTOGRAMS/')

from algorithm_utils import Global2Local_TD, init_logger
from multhist_lib import multipleHist1_Loc2Glob_TD

def main(args):
    # Parse arguments
    sys.argv = args
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    #Get global data
    globalStatistics = multipleHist1_Loc2Glob_TD.load(local_dbs).get_data()

    init_logger()
    logging.info(["globalStatistics:", globalStatistics])

    #raise ValueError(globalStatistics)
    global_out = Global2Local_TD(global_in = globalStatistics)

    # Return the algorithm's output
    global_out.transfer()


if __name__ == '__main__':
    main()
