from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import numpy as np
import scipy.stats as st
from mip_algorithms import Algorithm, AlgorithmResult, TabularDataResource
from mip_algorithms.constants import P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR
from scipy.special import expit

PREC = 1e-7  # Precision used in termination_condition
_MAX_ITER = 40


class LogistiRegression(Algorithm):
    def __init__(self, cli_args):
        super(LogistiRegression, self).__init__(__file__, cli_args)

    def local_init(self):
        # Unpack local input
        Y, X = self.data.variables, self.data.covariables
        Y = Y.iloc[:, 1]
        n_obs = len(Y)
        n_cols = len(X.columns)
        y_name = Y.name
        x_names = list(X.columns)
        # Y, X = np.array(Y), np.array(X)  # todo remove this and stop storing X, Y

        self.push_and_add(n_obs=n_obs)
        self.push_and_agree(n_cols=n_cols)
        self.push_and_agree(y_name=y_name)
        self.push_and_agree(x_names=x_names)

    def global_init(self):
        n_obs = self.fetch('n_obs')
        n_cols = self.fetch('n_cols')
        y_name = self.fetch('y_name')
        x_names = self.fetch('x_names')
        # Init vars
        ll = - 2 * n_obs * np.log(2)
        coeff = np.zeros(n_cols)
        iter_ = 0

        self.store(n_obs=n_obs)
        self.store(n_cols=n_cols)
        self.store(ll=ll)
        self.store(iter_=iter_)
        self.store(y_name=y_name)
        self.store(x_names=x_names)

        self.push(coeff=coeff)

    def local_step(self):
        Y, X = self.data.variables, self.data.covariables
        Y = Y.iloc[:, 1]
        coeff = self.fetch('coeff')

        # Auxiliary quantities
        z = np.dot(X, coeff)
        s = expit(z)
        d = np.multiply(s, (1 - s))
        D = np.diag(d)
        # Hessian
        hess = np.dot(
                np.transpose(X),
                np.dot(D, X)
        )
        # Gradient
        grad = np.dot(
                np.transpose(X),
                np.dot(
                        D,
                        z + np.divide(Y - s, d)
                )
        )
        # Log-likelihood
        ls1, ls2 = np.log(s), np.log(1 - s)
        ll = np.dot(Y, ls1) + np.dot(1 - Y, ls2)

        self.push_and_add(ll=ll)
        self.push_and_add(grad=grad)
        self.push_and_add(hess=hess)

    def global_step(self):
        ll_old = self.load('ll')
        iter_ = self.load('iter_')

        ll_new = self.fetch('ll')
        grad = self.fetch('grad')
        hess = self.fetch('hess')

        # Compute new coefficients
        coeff = np.dot(
                np.linalg.inv(hess),
                grad
        )
        # Update termination quantities
        delta = abs(ll_new - ll_old)
        iter_ += 1
        if delta < PREC or iter_ >= _MAX_ITER:
            self.terminate()

        self.store(ll=ll_new)
        self.store(iter_=iter_)
        self.store(coeff=coeff)

        self.push(coeff=coeff)

    def local_final(self):
        Y, X = self.data.variables, self.data.covariables
        Y = Y.iloc[:, 1]
        # Unpack local input
        coeff = self.fetch('coeff')

        # Auxiliary quantities
        z = np.dot(X, coeff)
        s = expit(z)
        d = np.multiply(s, (1 - s))
        D = np.diag(d)
        # Hessian
        hess = np.dot(
                np.transpose(X),
                np.dot(D, X)
        )
        # Gradient
        grad = np.dot(
                np.transpose(X),
                np.dot(
                        D,
                        z + np.divide(Y - s, d)
                )
        )
        # Log-likelihood
        ls1, ls2 = np.log(s), np.log(1 - s)
        ll = np.dot(Y, ls1) + np.dot(1 - Y, ls2)
        # sum Y, sum Y**2, ssres (residual sum of squares), sstot (total sum of squares)
        y_sum = np.sum(Y)
        y_sqsum = y_sum  # Because Y takes values in {0, 1}
        yhat = predict(X, coeff)
        ssres = np.dot(Y - yhat, Y - yhat)
        # True positives, false positives, etc.
        posneg = {'TP': 0, 'FP': 0, 'TN': 0, 'FN': 0}
        for yi, yhi in zip(Y, yhat):
            if yi == yhi == 1:
                posneg['TP'] += 1
            elif yi == 0 and yhi == 1:
                posneg['FP'] += 1
            elif yi == 1 and yhi == 0:
                posneg['FN'] += 1
            elif yi == yhi == 0:
                posneg['TN'] += 1
        # ROC curve
        FP_rate_frac = []
        TP_rate_frac = []
        for thres in np.linspace(1.0, 0.0, num=101):
            TP, TN, FP, FN = 0, 0, 0, 0
            yhat = predict(X, coeff, threshold=thres)
            for yi, yhi in zip(Y, yhat):
                if yi == yhi == 1:
                    TP += 1
                elif yi == 0 and yhi == 1:
                    FP += 1
                elif yi == 1 and yhi == 0:
                    FN += 1
                elif yi == yhi == 0:
                    TN += 1
            FP_rate_frac.append((FP, TN + FP))
            TP_rate_frac.append((TP, TP + FN))

        self.push_and_add(ll=ll)
        self.push_and_add(grad=grad)
        self.push_and_add(hess=hess)
        self.push_and_add(y_sum=y_sum)
        self.push_and_add(y_sqsum=y_sqsum)
        self.push_and_add(ssres=ssres)
        # self.push_and_add(posneg=posneg)  # fixme these values need complex additions
        # self.push_and_add(FP_rate_frac=FP_rate_frac)
        # self.push_and_add(TP_rate_frac=TP_rate_frac)
        # local_out = LogRegrFinal_Loc2Glob_TD(ll, grad, hess, y_sum, y_sqsum, ssres, posneg, FP_rate_frac, TP_rate_frac)

    def global_final(self):
        n_obs = self.load('n_obs')
        n_cols = self.load('n_cols')
        y_name = self.load('y_name')
        x_names = self.load('x_names')
        coeff = self.load('coeff')

        ll = self.fetch('ll')
        grad = self.fetch('grad')
        hess = self.fetch('hess')
        y_sum = self.fetch('y_sum')
        y_sqsum = self.fetch('y_sqsum')
        ssres = self.fetch('ssres')
        # posneg = self.fetch('posneg')
        # FP_rate_frac = self.fetch('FP_rate_frac')
        # TP_rate_frac = self.fetch('TP_rate_frac')

        # Output summary
        # stderr
        stderr = np.sqrt(
                np.diag(
                        np.linalg.inv(hess)
                )
        )
        # z scores
        z_scores = np.divide(coeff, stderr)
        # p-values
        z_to_p = lambda z: st.norm.sf(abs(z)) * 2
        p_values = z_to_p(z_scores)
        # Confidence intervals for 95%
        lci = np.array(
                [st.norm.ppf(0.025, loc=coeff[i], scale=stderr[i]) for i in range(len(coeff))]
        )
        rci = np.array(
                [st.norm.ppf(0.975, loc=coeff[i], scale=stderr[i]) for i in range(len(coeff))]
        )
        # Degrees of freedom
        df_mod = n_cols - 1
        df_resid = n_obs - df_mod - 1
        # Null model log-likelihood
        y_mean = y_sum / n_obs
        ll0 = y_sum * np.log(y_mean) + (n_obs - y_sum) * np.log(1.0 - y_mean)
        # AIC
        aic = 2 * n_cols - 2 * ll
        # BIC
        bic = np.log(n_obs) * n_cols - 2 * ll
        # R^2 etc.
        sstot = n_obs * y_mean * (1 - y_mean)  # Using binomial variable variance formula
        r2 = 1 - ssres / sstot
        r2_adj = 1 - (1 - r2) * (n_obs - 1) / (n_obs - n_cols - 1)
        r2_mcf = 1 - ll / ll0
        r2_cs = 1 - np.exp(-ll0 * 2 * r2_mcf / n_obs)
        # Confusion matrix etc.
        # TP, TN, FP, FN = posneg['TP'], posneg['TN'], posneg['FP'], posneg['FN']
        # confusion_mat = [[TP, FP], [FN, TN]]
        # accuracy = (TP + TN) / n_obs
        # precision = TP / (TP + FP)
        # recall = TP / (TP + FN)
        # F1 = 2 * (precision * recall) / (precision + recall)
        # # ROC curve
        # FP_rate = [fpr[0] / fpr[1] for fpr in FP_rate_frac]
        # TP_rate = [tpr[0] / tpr[1] for tpr in TP_rate_frac]
        # AUC = 0.0
        # for t in range(1, len(FP_rate)):
        #     AUC += 0.5 * (FP_rate[t] - FP_rate[t - 1]) * (TP_rate[t] + TP_rate[t - 1])
        # gini = 2 * AUC - 1
        # # F-statistic
        # F_stat = ((sstot - ssres) / n_cols) / (ssres / (n_obs - n_cols - 1))

        # Format output data
        # JSON raw
        raw_data = {
            'Covariates'                 : [
                {
                    'Variable'   : x_names[i],
                    'Coefficient': coeff[i],
                    'std.err.'   : stderr[i],
                    'z score'    : z_scores[i],
                    'p value'    : p_values[i] if p_values[i] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
                    'Lower C.I.' : lci[i],
                    'Upper C.I.' : rci[i]
                }
                for i in range(len(x_names))
            ],
            'Model degrees of freedom'   : df_mod,
            'Residual degrees of freedom': df_resid,
            'Log-likelihood'             : ll,
            'Null model log-likelihood'  : ll0,
            'AIC'                        : aic,
            'BIC'                        : bic,
            'R^2'                        : r2,
            'Adjusted R^2'               : r2_adj,
            'McFadden pseudo-R^2'        : r2_mcf,
            'Cox-Snell pseudo-R^2'       : r2_cs,
            # 'Confusion matrix'           : confusion_mat,
            # 'Accuracy'                   : accuracy,
            # 'Precision'                  : precision,
            # 'Recall'                     : recall,
            # 'F1 score'                   : F1,
            # 'AUC'                        : AUC,
            # 'Gini coefficient'           : gini,
            # 'F statistic'                : F_stat
        }


        # Tabular summary 1
        summary1_tabular = []
        for i in range(len(x_names)):
            summary1_tabular.append([
                x_names[i],
                coeff[i],
                stderr[i],
                z_scores[i],
                str(p_values[i]) if p_values[i] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
                lci[i],
                rci[i]
            ])
        table1 = TabularDataResource(
                fields=["variable", "coefficient", "std.err.", "z-score", "p-value", "lower c.i.", "upper c.i."],
                data=summary1_tabular,
                title='Logistic Regression Summary').render()
        table2 = TabularDataResource(
                fields=["model degrees of freedom", "residual degrees of freedom",
                                  "log-likelihood", "null model log-likelihood", "AIC", "BIC",
                                  "R^2", "adjusted R^2", "McFadden pseudo-R^2", "Cox-Snell pseudo-R^2", "F statistic"],
                data=[[df_mod, df_resid, ll, ll0, aic, bic, r2, r2_adj, r2_mcf, r2_cs, 12345]], # todo replace 12345
                title='Logistic Regression Summary').render()

        self.result = AlgorithmResult(raw_data=raw_data, tables=[table1, table2], highcharts=[])
        # Highchart ROC
        # highchart_roc = {
        #     "chart"  : {
        #         "type"    : "area",
        #         "zoomType": "xy"
        #     },
        #     "title"  : {
        #         "text": "ROC"
        #     },
        #     "xAxis"  : {
        #         "min"  : -0.05,
        #         "max"  : 1.05,
        #         "title": {
        #             "text": "False Positive Rate"
        #         }
        #     },
        #     "yAxis"  : {
        #         "title": {
        #             "text": "True Positive Rate"
        #         }
        #     },
        #     "legend" : {
        #         "enabled": False
        #     },
        #     "series" : [{
        #         "useHTML": True,
        #         "name"   : "AUC " + str(AUC) + "<br/>Gini Coefficient " + str(gini),
        #         "label"  : {
        #             "onArea": True
        #         },
        #         "data"   : list(zip(FP_rate, TP_rate))
        #     }],
        #     'tooltip': {
        #         'enabled'     : True,
        #         'headerFormat': '',
        #         'pointFormat' : '{point.x}, {point.y}'
        #     }
        # }
        # Highchart confusion matrix
        # highchart_conf_matr = {
        #
        #     "chart"    : {
        #         "type": "heatmap",
        #
        #     },
        #     "title"    : {
        #         "useHTML": True,
        #         "text"   : "Confusion Matrix<br/><center><font size='2'>Binary categories: TODO<br/>" +
        #                    "</font></center>"
        #     },
        #     "xAxis"    : {
        #         "categories": ["Condition Positives", "Condition Negatives"]
        #     },
        #     "yAxis"    : {
        #         "categories": ["Prediction Negatives", "Prediction Positives"],
        #         "title"     : "null"
        #     },
        #     "colorAxis": {
        #         "min"     : 0,
        #         "minColor": "#FFFFFF",
        #         "maxColor": "#6699ff"
        #     },
        #     "legend"   : {
        #         "enabled": False,
        #     },
        #     "tooltip"  : {
        #         "enabled": False
        #     },
        #     "series"   : [{
        #         "dataLabels" : [{
        #             "format" : '{point.name}: {point.value}',
        #             "enabled": True,
        #             "color"  : '#333333'
        #         }],
        #         "name"       : 'Confusion Matrix',
        #         "borderWidth": 1,
        #         "data"       : [{
        #             "name" : 'True Positives',
        #             "x"    : 0,
        #             "y"    : 1,
        #             "value": TP
        #         }, {
        #             "name" : 'False Positives',
        #             "x"    : 1,
        #             "y"    : 1,
        #             "value": FP
        #         }, {
        #             "name" : 'False Negatives',
        #             "x"    : 0,
        #             "y"    : 0,
        #             "value": FN
        #         }, {
        #             "name" : 'True Negatives',
        #             "x"    : 1,
        #             "y"    : 0,
        #             "value": TN
        #         }]
        #     }]
        # }



def predict(x, coeff, threshold=0.5):
    return np.array([1 if prob >= threshold else 0 for prob in expit(np.dot(x, coeff))])

# TODO see below how to add posneg, FP_rate, TP_rate
# class LogRegrFinal_Loc2Glob_TD(TransferData):
#     def __init__(self, *args):
#         self.posneg = args[6]
#         self.FP_rate_frac = args[7]
#         self.TP_rate_frac = args[8]
#
#     def __add__(self, other):
#         return LogRegrFinal_Loc2Glob_TD(
#                 {k: v + other.posneg[k] for k, v in self.posneg.items()},
#                 [(s[0] + o[0], s[1] + o[1]) for s, o in zip(self.FP_rate_frac, other.FP_rate_frac)],
#                 [(s[0] + o[0], s[1] + o[1]) for s, o in zip(self.TP_rate_frac, other.TP_rate_frac)]
#         )
