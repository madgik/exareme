from __future__ import division
from __future__ import print_function

import sys
import numpy as np
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData, ExaremeError


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
                "count": np.nansum (self.localstatistics[key]['count'], other.localstatistics[key]['count']) ,
                "min" :  np.nanmin (self.localstatistics[key]['min'] , other.localstatistics[key]['min']),
                "max" :  np.nanmax (self.localstatistics[key]['max'] , other.localstatistics[key]['max'])
                 }
        raise ValueError(result)
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
        resultHist = dict()
        for key in self.Hist:
            resultHist[key] = { "Data ": self.Hist[key]['Data'] +  other.Hist[key]['Data'],
                                "Categoriesx" : self.Hist[key]['Categoriesx'],
                                "Categoriesy" : self.Hist[key]['Categoriesy'] }
        return multipleHist2_Loc2Glob_TD(self.args_X, self.args_Y,self.CategoricalVariablesWithDistinctValues, self.Hist ,resultHist)
