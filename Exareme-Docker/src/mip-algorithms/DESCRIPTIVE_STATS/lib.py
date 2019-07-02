from __future__ import division
from __future__ import print_function

import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData, ExaremeError


class DescrStatsLocalDT(TransferData):
    def __init__(self, *args):
        if len(args) != 6:
            raise ExaremeError('Illegal number of arguments.')
        self.nn = args[0]
        self.sx = args[1]
        self.sxx = args[2]
        self.xmin = args[3]
        self.xmax = args[4]
        self.schema_X = args[5]

    def get_data(self):
        return self.nn, self.sx, self.sxx, self.xmin, self.xmax, self.schema_X

    def __add__(self, other):
        return DescrStatsLocalDT(
                self.nn + other.nn,
                self.sx + other.sx,
                self.sxx + other.sxx,
                min(self.xmin, other.xmin),
                max(self.xmax, other.xmax),
                self.schema_X,
        )
