from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/MULTIPLE_HISTOGRAMS/')

from algorithm_utils import StateData,Global2Local_TD
from multhist_lib import multipleHist2_Loc2Glob_TD


def run_local_step(args_X, args_Y, args_bins, dataSchema, CategoricalVariablesWithDistinctValues, dataFrame, globalStatistics):

    # Local Part of the algorithm
    Hist = dict()
    for varx in args_X:
        if varx in CategoricalVariablesWithDistinctValues: # varx is  categorical
            #print varx ," IS CATEGORICAL"
            # Histogram categorical of varx
            df_count = dataFrame.groupby(varx)[varx].count()
            for groupLevelx in CategoricalVariablesWithDistinctValues[varx]:
                if groupLevelx in dataFrame[varx].unique():
                    Hist[varx,None,groupLevelx,None] = { "count" : df_count[groupLevelx], "level" : None, "hist" : None}
                else:
                    Hist[varx,None,groupLevelx,None] = { "count" : 0, "level" : None, "hist" : None}
            for vary in args_Y:
                #Histogram thn varx group by var y
                #print vary
                df_count = dataFrame.groupby([varx,vary])[varx].count()
                for groupLevelx in CategoricalVariablesWithDistinctValues[varx]:
                    for groupLevely in CategoricalVariablesWithDistinctValues[vary]:
                        if (groupLevelx, groupLevely) in zip(dataFrame[varx],dataFrame[vary]) :
                            Hist[varx,vary,groupLevelx,groupLevely] = { "count" : df_count[groupLevelx][groupLevely], "level" : groupLevely, "hist" : None}
                        else:
                            Hist[varx,vary,groupLevelx,groupLevely] = { "count" : 0, "level" : groupLevely, "hist" : None }

        if varx not in  CategoricalVariablesWithDistinctValues: # varx is not categorical
             #print varx
             Hist[varx,None,None,None] =  { "count" : 0, "level" : None, "hist" : [x.tolist() for x in np.histogram(dataFrame[varx], range = [ globalStatistics[varx,None,None,None]['min'],
                                                                                                globalStatistics[varx,None,None,None]['max']],  bins = args_bins[varx])] }
             #Histogram thn varx group by var y
             for vary in args_Y:
                 #print vary
                 dfs = dataFrame.groupby(vary)[varx]
                 for groupLevely in CategoricalVariablesWithDistinctValues[vary]:
                    #print groupLevely
                    if groupLevely in dfs.groups:
                        #print "yes"
                        df = dfs.get_group(groupLevely)
                        Hist[varx,vary,None,groupLevely] = { "count" : None, "level" : None, "hist" : [x.tolist() for x in np.histogram(df, range = [ globalStatistics[varx,vary,None,groupLevely]['min'],
                                                                                                                  globalStatistics[varx,vary,None,groupLevely]['max']],  bins = args_bins[varx])]}
                    else:
                        #print "no"
                        Hist[varx,vary,None,groupLevely] = { "count" : None, "level" : None, "hist" : None}

    return Hist


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-global_step_db', required=True, help='Path to db holding global step results.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_prev_state).get_data()
    # Load global node output
    globalStatistics = Global2Local_TD.load(global_db).get_data()['global_in']
    #raise ValueError(globalStatistics,local_state['args_X'])

    # Run algorithm local step
    Hist = run_local_step(local_state['args_X'], local_state['args_Y'] ,
                               local_state['args_bins'], local_state['dataSchema'],
                               local_state['CategoricalVariablesWithDistinctValues'], local_state['dataFrame'],
                               globalStatistics)


    # Pack results
    local_out = multipleHist2_Loc2Glob_TD(local_state['args_X'], local_state['args_Y'] , local_state['CategoricalVariablesWithDistinctValues'], Hist)
    # Return the output data
    local_out.transfer()


if __name__ == '__main__':
    main()
