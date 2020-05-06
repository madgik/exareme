from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import sqlite3
import json
import pandas as pd
import numpy as np
import logging

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/MULTIPLE_HISTOGRAMS/')

from algorithm_utils import StateData,query_database,variable_categorical_getDistinctValues, init_logger, ExaremeError, PrivacyError, PRIVACY_MAGIC_NUMBER
from multhist_lib import multipleHist1_Loc2Glob_TD

def run_local_step(args_X, args_Y, args_bins, dataSchema, CategoricalVariablesWithDistinctValues, dataFrame):

    localstatistics = dict()

    for varx in args_X:
        if varx in CategoricalVariablesWithDistinctValues: # varx is  categorical
            #print varx ," IS CATEGORICAL"
            df_count = dataFrame.groupby(varx)[varx].count()
            for groupLevelx in CategoricalVariablesWithDistinctValues[varx]:
                if groupLevelx in dataFrame[varx].unique():
                    localstatistics[varx,None,groupLevelx,None] = { "count": df_count[groupLevelx], "min": None, "max": None }
                else:
                    localstatistics[varx,None,groupLevelx,None] = { "count": 0, "min": None, "max": None }
            for vary in args_Y: # If args_Y exists
                #print vary
                df_count = dataFrame.groupby([varx,vary])[varx].count()
                for groupLevelx in CategoricalVariablesWithDistinctValues[varx]:
                    for groupLevely in CategoricalVariablesWithDistinctValues[vary]:
                        if (groupLevelx, groupLevely) in zip(dataFrame[varx],dataFrame[vary]) :
                            localstatistics[varx,vary,groupLevelx,groupLevely] = { "count": df_count[groupLevelx][groupLevely], "min": None, "max": None }
                        else:
                            localstatistics[varx,vary,groupLevelx,groupLevely] = { "count": 0, "min": None, "max": None }

        elif varx not in CategoricalVariablesWithDistinctValues:  # varx is not Categorical
            #print varx ," IS NOT CATEGORICAL"
            localstatistics[varx,None,None,None] = {"count": dataFrame[varx].count(), "min":  dataFrame[varx].min(), "max":  dataFrame[varx].max()}
            for vary in args_Y:
                #print vary
                df_count = dataFrame.groupby(vary)[varx].count()
                df_min = dataFrame.groupby(vary)[varx].min()
                df_max = dataFrame.groupby(vary)[varx].max()
                for groupLevely in CategoricalVariablesWithDistinctValues[vary]:
                    if groupLevely in dataFrame[vary].unique():
                        #print groupLevely, vary
                        localstatistics[varx,vary,None,groupLevely] = { "count": df_count[groupLevely], "min": dataFrame[varx].min(), "max": dataFrame[varx].max() }
                    else:
                        localstatistics[varx,vary,None,groupLevely] = { "count": 0, "min": None, "max": None }

    return localstatistics

def main():

    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-x', required=True, help='Variable names, comma seperated ')
    parser.add_argument('-y', required=True, help='Categorical variables names, comma seperated.')
    parser.add_argument('-bins', required=True, help='Dictionary of variables names (key) and number of bins (value)')
    parser.add_argument('-input_local_DB', required=True, help='Path to local db.')
    parser.add_argument('-db_query', required=True, help='Query to be executed on local db.')
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    args, unknown = parser.parse_known_args()
    query = args.db_query
    fname_cur_state = path.abspath(args.cur_state_pkl)
    fname_loc_db = path.abspath(args.input_local_DB)

    #if args.x == '':
     #   raise ExaremeError('Field x must be non empty.')

    # Get data
    if args.x == '':
        args_X = list(args.y.replace(' ', '').split(','))
        args_Y = []
        varNames = "'" + "','".join(list(args.y.replace(' ', '').split(','))) + "'"
    else:
        args_X = list(args.y.replace(' ', '').split(','))
        args_Y = list(args.x.replace(' ', '').split(','))
        varNames = "'" + "','".join(list(args.y.replace(' ', '').split(','))) + "','" + "','".join(
                        list(args.x.replace(' ', '').split(','))) + "'"
    if args.bins == '':
        args_bins = {}
    else:
        args_bins = json.loads(args.bins)
        #args_bins = dict( (str(key), val) for key, val in args_bins.items())

    queryMetadata = "select * from metadata where code in (" + varNames  + ");"
    dataSchema, metadataSchema, metadata, dataFrame  = query_database(fname_db=fname_loc_db, queryData=query, queryMetadata=queryMetadata)
    CategoricalVariablesWithDistinctValues = variable_categorical_getDistinctValues(metadata)

    #Checking bins input
    for varx in args_X:
        if varx not in CategoricalVariablesWithDistinctValues:
            if varx not in args_bins:
                raise ExaremeError('Bin value is not defined for one at least non-categorical variable. i.e. ' + varx)

     # Run algorithm local step
    localStatistics = run_local_step(args_X, args_Y, args_bins, dataSchema, CategoricalVariablesWithDistinctValues, dataFrame)

    # Save local state
    local_state = StateData(args_X = args_X, args_Y = args_Y, args_bins = args_bins,  dataSchema = dataSchema,
                                CategoricalVariablesWithDistinctValues = CategoricalVariablesWithDistinctValues, dataFrame = dataFrame)
    local_state.save(fname = fname_cur_state)

    init_logger()
    logging.debug(["args_X, args_Y, args_bins, dataSchema, CategoricalVariablesWithDistinctValues:", args_X, args_Y, args_bins, dataSchema, CategoricalVariablesWithDistinctValues])
    logging.debug(["dataFrame:",dataFrame[0:5]])
    logging.debug(["localStatistics:", localStatistics])

    # Transfer local output
    local_out = multipleHist1_Loc2Glob_TD(localStatistics)
    #raise ValueError( local_out.get_data())
    local_out.transfer()

if __name__ == '__main__':
    main()
