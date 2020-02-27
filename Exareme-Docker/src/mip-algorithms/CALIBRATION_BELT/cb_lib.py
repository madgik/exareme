from __future__ import division
from __future__ import print_function

import sys
from itertools import groupby
from math import sqrt, exp, pi, asin, acos, atan
from operator import itemgetter
from os import path

from scipy.integrate import quad
from scipy.stats import chi2

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')
from algorithm_utils import TransferData

PREC = 1e-7  # Precision used in termination_condition


class CBInit_Loc2Glob_TD(TransferData):
    def __init__(self, *args):
        if len(args) != 5:
            raise ValueError('Illegal number of arguments.')
        self.n_obs = args[0]
        self.e_name = args[1]
        self.o_name = args[2]
        self.max_deg = args[3]
        self.e_domain = args[4]

    def get_data(self):
        return self.n_obs, self.e_name, self.o_name, self.max_deg, self.e_domain

    def __add__(self, other):
        assert self.e_name == other.e_name, "Local e names do not agree."
        assert self.o_name == other.o_name, "Local o names do not agree."
        assert self.max_deg == other.max_deg, "Local max_deg do not agree."
        return CBInit_Loc2Glob_TD(
                self.n_obs + other.n_obs,
                self.e_name,
                self.o_name,
                self.max_deg,
                (min(self.e_domain[0], other.e_domain[0]), max(self.e_domain[1], other.e_domain[1]))
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
                {deg: self.ll_dict[deg] + other.ll_dict[deg] for deg in
                 range(1, len(self.ll_dict) + 1)},
                {deg: self.grad_dict[deg] + other.grad_dict[deg] for deg in
                 range(1, len(self.ll_dict) + 1)},
                {deg: self.hess_dict[deg] + other.hess_dict[deg] for deg in
                 range(1, len(self.ll_dict) + 1)},
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
        if len(args) != 4:
            raise ValueError('Illegal number of arguments.')
        self.ll_dict = args[0]
        self.grad_dict = args[1]
        self.hess_dict = args[2]
        self.logLikBisector = args[3]

    def get_data(self):
        return self.ll_dict, self.grad_dict, self.hess_dict, self.logLikBisector

    def __add__(self, other):
        # assert len(self.gradient) == len(other.gradient), "Local gradient sizes do not agree."
        # assert self.hessian.shape == other.hessian.shape, "Local Hessian sizes do not agree."
        return CBFinal_Loc2Glob_TD(
                {deg: self.ll_dict[deg] + other.ll_dict[deg] for deg in
                 range(1, len(self.ll_dict) + 1)},
                {deg: self.grad_dict[deg] + other.grad_dict[deg] for deg in
                 range(1, len(self.ll_dict) + 1)},
                {deg: self.hess_dict[deg] + other.hess_dict[deg] for deg in
                 range(1, len(self.ll_dict) + 1)},
                self.logLikBisector + other.logLikBisector
        )


def find_relative_to_bisector(x, y, region_type):
    assert len(x) == len(y), 'x and y must have the same length'
    if region_type == 'over':
        reg = [yi > xi for yi, xi in zip(y, x)]
    elif region_type == 'under':
        reg = [yi < xi for yi, xi in zip(y, x)]
    else:
        raise ValueError('region_type must either be `over` or `under`')
    idxreg = [i for i, o in enumerate(reg) if o]
    if len(idxreg) == 0:
        return 'NEVER'
    segs = ''
    for k, g in groupby(enumerate(idxreg), lambda ix: ix[0] - ix[1]):
        seg = list(map(itemgetter(1), g))
        if x[seg[0]] != x[seg[-1]]:
            segs += str(x[seg[0]]) + '-' + str(x[seg[-1]]) + ', '
        else:
            segs += str(x[seg[0]]) + ', '
    segs = segs[:-2]
    return segs


def givitiStatCdf(t, m, devel='external', thres=0.95):
    assert m in {1, 2, 3, 4}, 'm must be an integer from 1 to 4'
    assert 0 <= thres <= 1, 'thres must be a number in [0, 1]'
    pDegInc = 1 - thres
    k = chi2.ppf(q=1 - pDegInc, df=1)
    cdfValue = None
    if devel == 'external':
        if t <= (m - 1) * k:
            cdfValue = 0
        else:
            if m == 1:
                cdfValue = chi2.cdf(t, df=2)
            elif m == 2:
                cdfValue = ((chi2.cdf(t, df=1) - 1 + pDegInc
                             + (-1) * sqrt(2) / sqrt(pi) * exp(-t / 2)
                             * (sqrt(t) - sqrt(k))) / pDegInc)
            elif m == 3:
                integral1 = quad(
                        lambda y: (chi2.cdf(t - y, df=1) - 1 + pDegInc) * chi2.pdf(y, df=1),
                        k, t - k
                )[0]
                integral2 = quad(
                        lambda y: (sqrt(t - y) - sqrt(k)) * 1 / sqrt(y),
                        k, t - k
                )[0]
                num = (integral1 - exp(-t / 2) / (2 * pi) * 2 * integral2)
                den = pDegInc ** 2
                cdfValue = num / den
            elif m == 4:
                integral = quad(
                        lambda r: r ** 2 * (exp(-(r ** 2) / 2) - exp(-t / 2))
                                  * (- pi * sqrt(k) / (2 * r) + 2 * sqrt(k) / r
                                     * asin((r ** 2 / k - 1) ** (-1 / 2))
                                     - 2 * atan((1 - 2 * k / r ** 2) ** (-1 / 2))
                                     + 2 * sqrt(k) / r * atan((r ** 2 / k - 2) ** (-1 / 2))
                                     + 2 * atan(r / sqrt(k) * sqrt(r ** 2 / k - 2))
                                     - 2 * sqrt(k) / r * atan(sqrt(r ** 2 / k - 2))),
                        sqrt(3 * k), sqrt(t)
                )[0]
                cdfValue = ((2 / (pi * pDegInc ** 2)) ** (3 / 2) * integral)
    elif devel == 'internal':
        assert m != 1, 'if devel=`internal`, m must be an integer from 2 to 4'
        if t <= (m - 2) * k:
            cdfValue = 0
        else:
            if m == 2:
                cdfValue = chi2.cdf(t, df=1)
            elif m == 3:
                integral = quad(
                        lambda r: r * exp(- (r ** 2) / 2) * acos(sqrt(k) / r),
                        sqrt(k), sqrt(t)
                )[0]
                cdfValue = 2 / (pi * pDegInc) * integral
            elif m == 4:
                integral = quad(
                        lambda r: r ** 2 * exp(-(r ** 2) / 2) * (
                                atan(sqrt(r ** 2 / k * (r ** 2 / k - 2))) -
                                sqrt(k) / r * atan(sqrt(r ** 2 / k - 2)) -
                                sqrt(k) / r * acos((r ** 2 / k - 1) ** (-1 / 2))),
                        sqrt(2 * k), sqrt(t)
                )[0]
                cdfValue = (2 / pi) ** (3 / 2) * (pDegInc) ** (-2) * integral
    else:
        raise ValueError('devel argument must be either `internal` or `external`')
    if cdfValue < -0.001 or cdfValue > 1.001:
        raise ValueError('cdfValue outside [0,1].')
    elif -0.001 <= cdfValue < 0:
        return 0
    elif 1 < cdfValue <= 1.001:
        return 1
    else:
        return cdfValue


def build_cb_highchart(**kwargs):
    kwargs = {k: str(v) for k, v in kwargs.items()}
    calib_curve, calib_belt1, calib_belt2, over_bisect1, under_bisect1, over_bisect2, \
    under_bisect2, cl1, cl2, thres, n_obs, model_deg, p_values, e_name, o_name = kwargs[
                                                                                     'calib_curve'], \
                                                                                 kwargs[
                                                                                     'calib_belt1'], \
                                                                                 kwargs[
                                                                                     'calib_belt2'], \
                                                                                 kwargs[
                                                                                     'over_bisect1'], \
                                                                                 kwargs[
                                                                                     'under_bisect1'], \
                                                                                 kwargs[
                                                                                     'over_bisect2'], \
                                                                                 kwargs[
                                                                                     'under_bisect2'], \
                                                                                 kwargs['cl1'], \
                                                                                 kwargs['cl2'], \
                                                                                 kwargs['thres'], \
                                                                                 kwargs['n_obs'], \
                                                                                 kwargs[
                                                                                     'model_deg'], \
                                                                                 kwargs['p_values'], \
                                                                                 kwargs['e_name'], \
                                                                                 kwargs['o_name']
    hc = {
        "title"      : {
            "text": "GiViTI Calibration Belt"
        },

        "annotations": [
            {
                "labels"      : [
                    {
                        "point"  : {"x": 100, "y": 100},
                        "text"   : "Polynomial degree: " + model_deg + " <br/>Model selection "
                                                                       "significance level: " +
                                   thres +
                                   " <br/>p-value: " + p_values +
                                   " <br/>n: " + n_obs,
                        "padding": 10,
                        "shape"  : 'rect'
                    }
                ],
                "labelOptions": {
                    "borderRadius"   : 5,
                    "backgroundColor": "#bbd9fa",
                    "borderWidth"    : 1,
                    "borderColor"    : "#9aa2ab"
                }
            },
            {
                "labels"      : [
                    {
                        "point"  : {"x": 400, "y": 400},
                        "text"   : "Confidence level: " + cl1 + "<br/>Under the bisector: " +
                                   under_bisect1 + "<br/>Over the bisector: "
                                   + over_bisect1,
                        "padding": 10,
                        "shape"  : 'rect'
                    }
                ],
                "labelOptions": {
                    "borderRadius"   : 5,
                    "backgroundColor": "#6e7d8f",
                    "borderWidth"    : 1,
                    "borderColor"    : "#AAA"
                }
            },
            {
                "labels"      : [
                    {
                        "point"  : {"x": 400, "y": 465},
                        "text"   : "Confidence level: " + cl2 + "<br/>Under the bisector: " +
                                   under_bisect2 + "<br/>Over the bisector: "
                                   + over_bisect2,
                        "padding": 10,
                        "shape"  : 'rect'
                    }
                ],
                "labelOptions": {
                    "borderRadius"   : 5,
                    "backgroundColor": "#a5b4c7",
                    "borderWidth"    : 1,
                    "borderColor"    : "#AAA"
                }
            }
        ],

        "xAxis"      : {
            "title"  : {
                "text": "EXPECTED (" + e_name + ")"
            },
            "visible": True
        },

        "yAxis"      : {
            "title"  : {
                "text": "OBSERVED (" + o_name + ")"
            },
            "visible": True
        },

        "tooltip"    : {
            "crosshairs": True,
            "shared"    : True
        },

        "legend"     : {},

        "series"     : [
            {
                "name"     : "Observed mortality",
                "data"     : calib_curve,
                "zIndex"   : 3,
                "lineWidth": 3,
                "color"    : "Highcharts.getOptions().colors[0]",
                "marker"   : {
                    "enabled": False
                },
                "label"    : {
                    "enabled": False
                }
            },
            {
                "name"     : "Confidence level " + cl2,
                "data"     : calib_belt2,
                "type"     : "arearange",
                "lineWidth": 0,
                "linkedTo" : ":previous",
                "color"    : "#a5b4c7",
                "zIndex"   : 0,
                "marker"   : {
                    "enabled": False
                }
            },
            {
                "name"     : "Confidence level " + cl1,
                "data"     : calib_belt1,
                "type"     : "arearange",
                "lineWidth": 0,
                "linkedTo" : ":previous",
                "color"    : "#6e7d8f",
                "zIndex"   : 1,
                "marker"   : {
                    "enabled": False
                }
            },
            {
                "name"            : "Bisector",
                "data"            : [[0, 0], [1, 1]],
                "zIndex"          : 2,
                "color"           : '#fc7938',
                "lineWidth"       : 1.5,
                "dashStyle"       : "Dash",
                "allowPointSelect": False,
                "marker"          : {
                    "enabled": False
                },
                "label"           : {
                    "enabled": False
                }
            }
        ]
    }
    return hc
