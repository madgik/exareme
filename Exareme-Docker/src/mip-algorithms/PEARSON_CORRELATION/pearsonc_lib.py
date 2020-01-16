from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData, ExaremeError


# Set the data class that will transfer the data between local-global
class PearsonCorrelationLocalDT(TransferData):
    def __init__(self, args):
        if len(args) != 10:
            raise ExaremeError('Illegal number of arguments.')
        self.nn = args[0]
        self.sx = args[1]
        self.sy = args[2]
        self.sxx = args[3]
        self.sxy = args[4]
        self.syy = args[5]
        self.schema_X = args[6]
        self.schema_Y = args[7]
        self.correlmatr_row_names = args[8]
        self.correlmatr_col_names = args[9]

    def get_data(self):
        return (
            self.nn, self.sx, self.sy,
            self.sxx, self.sxy, self.syy,
            self.schema_X, self.schema_Y,
            self.correlmatr_row_names, self.correlmatr_col_names
        )

    def __add__(self, other):
        if self.schema_X != other.schema_X:
            raise ValueError("Local schema_X's do not agree.")
        if self.schema_Y != other.schema_Y:
            raise ValueError("Local schema_Y's do not agree.")
        if self.correlmatr_row_names != other.correlmatr_row_names:
            raise ValueError('Local correlmatr_row_names do not agree.')
        if self.correlmatr_col_names != other.correlmatr_col_names:
            raise ValueError('Local correlmatr_col_names do not agree.')
        result = PearsonCorrelationLocalDT((
            self.nn + other.nn,
            self.sx + other.sx,
            self.sy + other.sy,
            self.sxx + other.sxx,
            self.sxy + other.sxy,
            self.syy + other.syy,
            self.schema_X,
            self.schema_Y,
            self.correlmatr_row_names,
            self.correlmatr_col_names
        ))
        return result
