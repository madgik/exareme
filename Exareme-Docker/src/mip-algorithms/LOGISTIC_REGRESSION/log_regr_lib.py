from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from utils.algorithm_utils import TransferData

PREC = 1e-7  # Precission used in termination_condition


class LogRegrInit_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        self.n_obs = args[0]
        self.n_cols = args[1]
        self.y_name = args[2]
        self.x_names = args[3]

    def get_data(self):
        return self.n_obs, self.n_cols, self.y_name, self.x_names

    def __add__(self, other):
        assert self.n_cols == other.n_cols, "Local n_cols do not agree."
        return LogRegrInit_Loc2Glob_TD(
                self.n_obs + other.n_obs,
                self.n_cols,
                self.y_name,
                self.x_names,
        )


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
        return LogRegrIter_Loc2Glob_TD(
                self.ll + other.ll,
                self.gradient + other.gradient,
                self.hessian + other.hessian
        )


class LogRegrIter_Glob2Loc_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        self.coeffs = args[0]

    def get_data(self):
        return self.coeffs


class LogRegrFinal_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 9:
            raise ValueError('Illegal number of arguments.')
        self.ll = args[0]
        self.gradient = args[1]
        self.hessian = args[2]
        self.y_sum = args[3]
        self.y_sqsum = args[4]
        self.ssres = args[5]
        self.posneg = args[6]
        self.FP_rate_frac = args[7]
        self.TP_rate_frac = args[8]

    def get_data(self):
        return self.ll, self.gradient, self.hessian, \
               self.y_sum, self.y_sqsum, self.ssres, \
               self.posneg, self.FP_rate_frac, self.TP_rate_frac

    def __add__(self, other):
        assert len(self.gradient) == len(other.gradient), "Local gradient sizes do not agree."
        assert self.hessian.shape == other.hessian.shape, "Local Hessian sizes do not agree."
        return LogRegrFinal_Loc2Glob_TD(
                self.ll + other.ll,
                self.gradient + other.gradient,
                self.hessian + other.hessian,
                self.y_sum + other.y_sum,
                self.y_sqsum + other.y_sqsum,
                self.ssres + other.ssres,
                {
                    'TP': self.posneg['TP'] + other.posneg['TP'],
                    'FP': self.posneg['FP'] + other.posneg['FP'],
                    'TN': self.posneg['TN'] + other.posneg['TN'],
                    'FN': self.posneg['FN'] + other.posneg['FN']
                },
                [(s[0] + o[0], s[1] + o[1]) for s, o in zip(self.FP_rate_frac, other.FP_rate_frac)],
                [(s[0] + o[0], s[1] + o[1]) for s, o in zip(self.TP_rate_frac, other.TP_rate_frac)]
        )
