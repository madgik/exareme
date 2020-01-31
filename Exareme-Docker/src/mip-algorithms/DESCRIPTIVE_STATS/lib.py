from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from utils.algorithm_utils import TransferData


class DescrStatsLocal_DT(TransferData):
    def __init__(self, transf_var_list):
        self.transf_var_list = transf_var_list
        self.num_vars = len(transf_var_list)

    def get_data(self):
        return self.transf_var_list, self.num_vars

    def __add__(self, other):
        sum_transf_var_list = []
        for i in xrange(self.num_vars):
            if self.transf_var_list[i].is_categorical:
                new_transf_var = TransferVariable(
                        self.transf_var_list[i].is_categorical,
                        self.transf_var_list[i].var_name,
                        self.transf_var_list[i].count + other.transf_var_list[i].count,
                        {cat: self.transf_var_list[i].freqs[cat] + other.transf_var_list[i].freqs[cat] for cat in
                         self.transf_var_list[i].freqs.keys()},
                )
                sum_transf_var_list.append(new_transf_var)
            else:
                new_transf_var = TransferVariable(
                        self.transf_var_list[i].is_categorical,
                        self.transf_var_list[i].var_name,
                        self.transf_var_list[i].nn + other.transf_var_list[i].nn,
                        self.transf_var_list[i].sx + other.transf_var_list[i].sx,
                        self.transf_var_list[i].sxx + other.transf_var_list[i].sxx,
                        min(self.transf_var_list[i].xmin, other.transf_var_list[i].xmin),
                        max(self.transf_var_list[i].xmax, other.transf_var_list[i].xmax)
                )
                sum_transf_var_list.append(new_transf_var)
        return DescrStatsLocal_DT(sum_transf_var_list)


class TransferVariable(object):
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


class Variable(object):
    def __init__(self, var_name, x, is_categorical, enums):
        self.var_name = var_name
        self.x = x
        self.is_categorical = is_categorical
        self.enums = enums

    def get_data(self):
        return self.var_name, self.x, self.is_categorical, self.enums
