from __future__ import division
from __future__ import print_function

import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
from algorithm_utils import TransferData


class LogRegrInit_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 5:
            raise ValueError('Illegal number of arguments.')
        self.n_obs = args[0]
        self.n_cols = args[1]
        self.y_val_dict = args[2]
        self.schema_X = args[3]
        self.schema_Y = args[4]

    def get_data(self):
        return self.n_obs, self.n_cols, self.y_val_dict, self.schema_X, self.schema_Y

    def __add__(self, other):
        assert self.n_cols == other.n_cols, "Local n_cols do not agree."
        assert self.y_val_dict == other.y_val_dict, "Local y_val_dict do not agree."
        assert self.schema_X == other.X_schema, "Local schema_X do not agree."
        assert self.schema_Y == other.Y_schema, "Local schema_Y do not agree."
        return LogRegrInit_Loc2Glob_TD((
            self.n_obs + other.n_obs,
            self.n_cols,
            self.y_val_dict,
            self.schema_X,
            self.schema_Y
        ))


class LogRegrIter_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 3:
            raise ValueError('Illegal number of arguments.')
        self.ll = args[0]
        self.gradient = args[1]
        self.hessian = args[2]

    def get_data(self):
        return self.ll, self.gradient, self.hessian

    def __add__(self, other):
        assert len(self.gradient) == len(other.gradient), "Local gradient sizes do not agree."
        assert self.hessian.shape == other.hessian.shape, "Local Hessian sizes do not agree."
        return LogRegrIter_Loc2Glob_TD((
            self.ll + other.ll,
            self.gradient + other.gradient,
            self.hessian + other.hessian
        ))


class LogRegrIter_Glob2Loc_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        self.coeffs = args[0]

    def get_data(self):
        return self.coeffs
