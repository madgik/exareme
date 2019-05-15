from __future__ import division
from __future__ import print_function

import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData


# Set the data class that will transfer the data between local-global
class PearsonCorrelationLocalDT(TransferData):
    def __init__(self, args):
        if len(args) != 8:
            raise ValueError('Illegal number of arguments.')
        self.nn = args[0]
        self.sx = args[1]
        self.sy = args[2]
        self.sxx = args[3]
        self.sxy = args[4]
        self.syy = args[5]
        self.schema_X = args[6]
        self.schema_Y = args[7]

    def get_data(self):
        return (
            self.nn, self.sx, self.sy,
            self.sxx, self.sxy, self.syy,
            self.schema_X, self.schema_Y
        )

    def __add__(self, other):
        assert self.schema_X == other.schema_X, 'Local schema_X do not agree.'
        assert self.schema_Y == other.schema_Y, 'Local schema_Y do not agree.'
        result = PearsonCorrelationLocalDT((
            self.nn + other.nn,
            self.sx + other.sx,
            self.sy + other.sy,
            self.sxx + other.sxx,
            self.sxy + other.sxy,
            self.syy + other.syy,
            self.schema_X,
            self.schema_Y
        ))
        return result
