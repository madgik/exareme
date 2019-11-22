from __future__ import division
from __future__ import print_function

import sys
from os import path
import numpy as np
from argparse import ArgumentParser

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/MULTIPLE_HISTOGRAMS/')

from algorithm_utils import Global2Local_TD
from multhist_lib import multipleHist1_Loc2Glob_TD

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    globalStatistics = multipleHist1_Loc2Glob_TD.load(local_dbs)

    # Return the algorithm's output
    global_out.transfer(globalStatistics = globalStatistics)


if __name__ == '__main__':
    main()
