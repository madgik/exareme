from __future__ import division
from __future__ import print_function

import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData, ExaremeError

class DescrStatsLocal_DT(TransferData):
    def __init__(self, *args):
        self.is_categorical = args[0]
        self.var_name = args[1]
        if self.is_categorical:
            self.count = args[2]
            self.freqs = args[3]
        else:
            self.nn = args[2]
            self.sx = args[3]
            self.sxx = args[4]
            self.xmin = args[5]
            self.xmax = args[6]

    def get_data(self):
        if self.is_categorical:
            return self.is_categorical, self.var_name, self.count, self.freqs
        else:
            return self.is_categorical, self.var_name, self.nn, self.sx, self.sxx, self.xmin, self.xmax

    def __add__(self, other):
        if self.is_categorical:
            assert self.freqs.keys() == other.freqs.keys(), 'Local categories do not agree.'
            return DescrStatsLocal_DT(
                    self.is_categorical,
                    self.var_name,
                    self.count + other.count,
                    {cat: self.freqs[cat] + other.freqs[cat] for cat in self.freqs.keys()},
            )
        else:
            return DescrStatsLocal_DT(
                    self.is_categorical,
                    self.var_name,
                    self.nn + other.nn,
                    self.sx + other.sx,
                    self.sxx + other.sxx,
                    min(self.xmin, other.xmin),
                    max(self.xmax, other.xmax),
            )
