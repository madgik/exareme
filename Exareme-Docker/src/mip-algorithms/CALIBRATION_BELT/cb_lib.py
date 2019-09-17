from __future__ import division
from __future__ import print_function

import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
from algorithm_utils import TransferData

PREC = 1e-7  # Precission used in termination_condition


class CBInit_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 4:
            raise ValueError('Illegal number of arguments.')
        self.n_obs = args[0]
        self.e_name = args[1]
        self.o_name = args[2]
        self.max_deg = args[3]

    def get_data(self):
        return self.n_obs, self.e_name, self.o_name, self.max_deg

    def __add__(self, other):
        assert self.e_name == other.e_name, "Local e names do not agree."
        assert self.o_name == other.o_name, "Local o names do not agree."
        assert self.max_deg == other.max_deg, "Local max_deg do not agree."
        return CBInit_Loc2Glob_TD(
                self.n_obs + other.n_obs,
                self.e_name,
                self.o_name,
                self.max_deg
        )

class CBIter_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 3:
            raise ValueError('Illegal number of arguments.')
        self.ll_dict = args[0]
        self.grad_dict = args[1]
        self.hess_dict = args[2]

    def get_data(self):
        return self.ll_dict, self.grad_dict, self.hess_dict

    def __add__(self, other):
        # assert len(self.gradient) == len(other.gradient), "Local gradient sizes do not agree."
        # assert self.hessian.shape == other.hessian.shape, "Local Hessian sizes do not agree."
        return CBIter_Loc2Glob_TD(
                {deg: self.ll_dict[deg] + other.ll_dict[deg] for deg in range(1, len(self.ll_dict) + 1)},
                {deg: self.grad_dict[deg] + other.grad_dict[deg] for deg in range(1, len(self.ll_dict) + 1)},
                {deg: self.hess_dict[deg] + other.hess_dict[deg] for deg in range(1, len(self.ll_dict) + 1)},
        )

class CBIter_Glob2Loc_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 1:
            raise ValueError('Illegal number of arguments.')
        self.coeff_dict = args[0]

    def get_data(self):
        return self.coeff_dict

class CBFinal_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 3:
            raise ValueError('Illegal number of arguments.')
        self.ll_dict = args[0]
        self.grad_dict = args[1]
        self.hess_dict = args[2]

    def get_data(self):
        return self.ll_dict, self.grad_dict, self.hess_dict

    def __add__(self, other):
        # assert len(self.gradient) == len(other.gradient), "Local gradient sizes do not agree."
        # assert self.hessian.shape == other.hessian.shape, "Local Hessian sizes do not agree."
        return CBIter_Loc2Glob_TD(
                {deg: self.ll_dict[deg] + other.ll_dict[deg] for deg in range(1, len(self.ll_dict) + 1)},
                {deg: self.grad_dict[deg] + other.grad_dict[deg] for deg in range(1, len(self.ll_dict) + 1)},
                {deg: self.hess_dict[deg] + other.hess_dict[deg] for deg in range(1, len(self.ll_dict) + 1)},
        )