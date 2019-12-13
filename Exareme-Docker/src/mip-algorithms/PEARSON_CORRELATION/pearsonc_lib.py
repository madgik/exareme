from __future__ import division
from __future__ import print_function

import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData


# Set the data class that will transfer the data between local-global
class PearsonCorrelationLocalDT(TransferData):
    def __init__(self, *args):
        self.n_obs = args[0]
        self.sx = args[1]
        self.sy = args[2]
        self.sxx = args[3]
        self.sxy = args[4]
        self.syy = args[5]
        self.cm_names = args[6]
        self.lnames = args[7]
        self.rnames = args[8]

    def get_data(self):
        return (
            self.n_obs, self.sx, self.sy,
            self.sxx, self.sxy, self.syy,
            self.cm_names, self.lnames, self.rnames
        )

    def __add__(self, other):
        result = PearsonCorrelationLocalDT(
                self.n_obs + other.n_obs,
                self.sx + other.sx,
                self.sy + other.sy,
                self.sxx + other.sxx,
                self.sxy + other.sxy,
                self.syy + other.syy,
                self.cm_names,
                self.lnames,
                self.rnames
        )
        return result
