from __future__ import division
from __future__ import print_function

import sys
import numpy as np
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData, ExaremeError

def add_vals(a,b):
    if a == None and b == None:
        return None
    else:
        return(a or 0 ) +( b or 0)


def comp_min_val(a,b):
    if a == None and b == None:
        return None
    elif a == None and b != None:
        return b
    elif a != None and b == None:
        return a
    else:
        return min(a,b)

def comp_max_val(a,b):
    if a == None and b == None:
        return None
    elif a == None and b != None:
        return b
    elif a != None and b == None:
        return a
    else:
        return max(a,b)

class multipleHist1_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ExaremeError('Illegal number of arguments.')
        self.localstatistics = args[0]

    def get_data(self):
        return self.localstatistics

    def __add__(self, other):
        result = dict()
        for key in self.localstatistics:
            result[key] = {
                "count": self.localstatistics[key]['count'] + other.localstatistics[key]['count'] ,
                 "min" : comp_min_val (self.localstatistics[key]['min'], other.localstatistics[key]['min']),
                 "max" : comp_max_val (self.localstatistics[key]['max'], other.localstatistics[key]['max'])
                 }
        #raise ValueError(result)
        return multipleHist1_Loc2Glob_TD(result)



class multipleHist2_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 4:
            raise ExaremeError('Illegal number of arguments.')
        self.args_X = args[0]
        self.args_Y = args[1]
        self.CategoricalVariablesWithDistinctValues = args[2]
        self.Hist = args[3]

    def get_data(self):
        return self.args_X, self.args_Y, self.CategoricalVariablesWithDistinctValues , self.Hist

    def __add__(self, other):
        result = dict()

        for key in self.Hist:
            globalHist = [0]*len(self.Hist[key]['Data'])
            if any(isinstance(i, int) for i in self.Hist[key]['Data']):
                for i in xrange(len(self.Hist[key]['Data'])):
                    globalHist[i] = add_vals(self.Hist[key]['Data'][i] , other.Hist[key]['Data'][i])

            if any(isinstance(i, list) for i in self.Hist[key]['Data']):
                for i in xrange(len(self.Hist[key]['Data'])):
                    globalHist[i] = [0]*len(self.Hist[key]['Data'][i])
                    for j in xrange(len(self.Hist[key]['Data'][i])):
                        globalHist[i][j] =  add_vals(self.Hist[key]['Data'][i][j] , other.Hist[key]['Data'][i][j])

            result[key] = { "Data": globalHist,
                                "Categoriesx" : self.Hist[key]['Categoriesx'],
                                "Categoriesy" : self.Hist[key]['Categoriesy'] }
        #raise ValueError(self.Hist, other.Hist,result)
        return multipleHist2_Loc2Glob_TD(self.args_X, self.args_Y,self.CategoricalVariablesWithDistinctValues, result)
