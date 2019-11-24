from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/MULTIPLE_HISTOGRAMS/')

from algorithm_utils import set_algorithms_output_data
from multhist_lib import multipleHist2_Loc2Glob_TD


args_X= ['lefthippocampus']
args_Y = ['gender']
CategoricalVariablesWithDistinctValues=  {'gender': ['M', 'F']}
GlobalHist= {('lefthippocampus', None, None, None): {'count': 0, 'hist': [[34, 785, 101], [1.3047, 2.353766666666667, 3.4028333333333336, 4.4519]], 'level': None}, ('lefthippocampus', 'gender', None, 'M'): {'count': None, 'hist': [[1, 363, 82], [1.3047, 2.353766666666667, 3.4028333333333336, 4.4519]], 'level': None}, ('lefthippocampus', 'gender', None, 'F'): {'count': None, 'hist': [[33, 422, 19], [1.3047, 2.353766666666667, 3.4028333333333336, 4.4519]], 'level': None}}

def DictToJson(GlobalHist):
    Series = {}
    for varx in args_X:
        print GlobalHist[(varx,None,None,None)]
        for vary in args_Y:
            for groupLevely in CategoricalVariablesWithDistinctValues[vary]:
                print GlobalHist[(varx,vary,None,groupLevely)]

###################################TODO na bgalw to groupLevelx apo to key kai na to bvalw sto val tou dict

    for key in GlobalHist:
        variableName, covariableName, variableLevel,covariableLevel = key

        count = GlobalHist[key]['count']
        HistCounts = GlobalHist[key]['hist'][0]
        HistRanges = GlobalHist[key]['hist'][1]
        level = GlobalHist[key]['level']
        print variableName, covariableName, variableLevel,covariableLevel
        print count, HistCounts, HistRanges,level



def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    # Load local nodes output
    args_X, args_Y,CategoricalVariablesWithDistinctValues, GlobalHist = multipleHist2_Loc2Glob_TD.load(local_dbs).get_data()
    # Return the algorithm's output
    raise ValueError (args_X, args_Y,CategoricalVariablesWithDistinctValues,GlobalHist)
    set_algorithms_output_data(GlobalHist)


if __name__ == '__main__':
    main()
