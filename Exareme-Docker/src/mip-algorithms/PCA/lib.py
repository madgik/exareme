from __future__ import division
from __future__ import print_function

import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData, ExaremeError


class PCA1_Loc2Glob_DT(TransferData):
    def __init__(self, *args):
        if len(args) != 2:
            raise ExaremeError('Illegal number of arguments.')
        self.nn = args[0]
        self.sx = args[1]

    def get_data(self):
        return self.nn, self.sx

    def __add__(self, other):
        return PCA1_Loc2Glob_DT(
            self.nn + other.nn,
            self.sx + other.sx,
        )


class PCA2_Loc2Glob_DT(TransferData):
    def __init__(self, *args):
        if len(args) != 3:
            raise ExaremeError('Illegal number of arguments.')
        self.gramian = args[0]
        self.n_obs = args[1]
        self.schema_X = args[2]

    def get_data(self):
        return self.gramian, self.n_obs, self.schema_X

    def __add__(self, other):
        return PCA2_Loc2Glob_DT(
            self.gramian + other.gramian,
            self.n_obs + other.n_obs,
            self.schema_X,
        )
