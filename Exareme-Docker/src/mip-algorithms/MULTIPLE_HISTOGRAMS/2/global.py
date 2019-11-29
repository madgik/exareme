from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/MULTIPLE_HISTOGRAMS/')

from algorithm_utils import set_algorithms_output_data, PRIVACY_MAGIC_NUMBER
from multhist_lib import multipleHist2_Loc2Glob_TD


def highchartsbasiccolumn(title, ytitle, categoriesList, mydatajson):
    a = '<span style="font-size:10px">{point.key}</span><table>'
    b = '<tr><td style="color:{series.color};padding:0">{series.name}: </td><td style="padding:0"><b>{point.y:.1f} mm</b></td></tr>'
    myresult =  {
        "type" : "application/vnd.highcharts+json",
        "data" : { "chart" : { "type": "column" },
                   "title" : { "text": title },
                   "xAxis" : { "categories": categoriesList, "crosshair": True },
                   "yAxis":  { "min": 0, "title": { "text": ytitle }},

                   "tooltip": { "headerFormat": a,
                                "pointFormat":  b},

                   "plotOptions": { "column": { "pointPadding": 0.2, "borderWidth": 0} },
                   "series": mydatajson
        }
    }
    return myresult

def histogramToHighchart (Hist):

    myjsonresult  =  { "result" : []}
    for key in Hist:
        variableName, covariableName = key
        #title
        title = "Histogram of " + variableName
        if covariableName is not None:
            title += " grouped by " + covariableName
        #print title
        #mydatajson
        mydatajson = []
        if covariableName is None:
            mydatajson.append({
                    "name" : "All",
                    "data" :  Hist[key]['Data']
                })
        else:
            for i in  range(len(Hist[key]['Categoriesy'])):
                mydatajson.append({
                            "name" : Hist[key]['Categoriesy'][i],
                            "data" : Hist[key]['Data'][i]
                        })

        myhighchart = highchartsbasiccolumn(title, "Count", Hist[key]['Categoriesx'], mydatajson)
        myjsonresult["result"].append(myhighchart)
    return json.dumps(myjsonresult)

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    # Load local nodes output
    args_X, args_Y,CategoricalVariablesWithDistinctValues, GlobalHist = multipleHist2_Loc2Glob_TD.load(local_dbs).get_data()

    # Histogram modification due to privacy --> Move it to local.py
    for key in GlobalHist:
        for i in xrange(len(GlobalHist[key]['Data'])):
            if isinstance(GlobalHist[key]['Data'][i], list):
                for j in xrange(len(GlobalHist[key]['Data'][i])):
                    if GlobalHist[key]['Data'][i][j] <= PRIVACY_MAGIC_NUMBER:
                     GlobalHist[key]['Data'][i][j] = 0
            else:
                if GlobalHist[key]['Data'][i] <= PRIVACY_MAGIC_NUMBER:
                    GlobalHist[key]['Data'][i] = 0

    # Return the algorithm's output
    #raise ValueError (args_X, args_Y,CategoricalVariablesWithDistinctValues,GlobalHist)
    global_out = histogramToHighchart(GlobalHist)
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
