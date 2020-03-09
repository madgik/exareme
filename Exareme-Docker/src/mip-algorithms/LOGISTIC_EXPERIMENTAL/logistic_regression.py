from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from collections import namedtuple

import numpy as np
import scipy.stats as st
from mip_algorithms import Algorithm, AlgorithmResult, TabularDataResource, create_runner
from mip_algorithms.constants import P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR, PREC, MAX_ITER, CONFIDENCE
from mip_algorithms.highcharts import ConfusionMatrix, ROC
from scipy.special import expit


class LogisticRegression(Algorithm):
    def __init__(self, cli_args):
        super(LogisticRegression, self).__init__(__file__, cli_args)

    def local_init(self):
        Y, X = self.data.variables, self.data.covariables
        Y = Y.iloc[:, 1]
        n_obs = len(Y)
        n_cols = len(X.columns)
        y_name = Y.name
        x_names = list(X.columns)

        self.store(Y=Y)

        self.push_and_add(n_obs=n_obs)
        self.push_and_agree(n_cols=n_cols)
        self.push_and_agree(y_name=y_name)
        self.push_and_agree(x_names=x_names)

    def global_init(self):
        n_obs = self.fetch('n_obs')
        n_cols = self.fetch('n_cols')
        y_name = self.fetch('y_name')
        x_names = self.fetch('x_names')
        # Init model
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
        X = self.data.covariables
        Y = self.load('Y')
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

        self.store(coeff=coeff)

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
        # Verify termination condition
        delta = abs(ll_new - ll_old)
        iter_ += 1
        if delta < PREC or iter_ >= MAX_ITER:
            self.terminate()

        self.store(ll=ll_new)
        self.store(iter_=iter_)
        self.store(coeff=coeff)
        self.store(hess=hess)

        self.push(coeff=coeff)

    def local_final(self):
        Y = self.load('Y')

        thresholds = np.linspace(1.0, 0.0, num=101)
        true_positives = np.zeros(len(thresholds))
        true_negatives = np.zeros(len(thresholds))
        false_positives = np.zeros(len(thresholds))
        false_negatives = np.zeros(len(thresholds))
        for i, thr in enumerate(thresholds):
            tp, tn, fp, fn = 0, 0, 0, 0
            yhat = self.predict(threshold=thr)
            for yi, yhi in zip(Y, yhat):
                if yi == yhi == 1:
                    tp += 1
                elif yi == 0 and yhi == 1:
                    fp += 1
                elif yi == 1 and yhi == 0:
                    fn += 1
                elif yi == yhi == 0:
                    tn += 1
            true_positives[i] = tp
            true_negatives[i] = tn
            false_positives[i] = fp
            false_negatives[i] = fn
        half_idx = np.where(thresholds == 0.5)[0][0]

        self.push_and_add(y_sum=np.sum(Y))
        self.push_and_add(true_positives=true_positives)
        self.push_and_add(true_negatives=true_negatives)
        self.push_and_add(false_positives=false_positives)
        self.push_and_add(false_negatives=false_negatives)
        self.push_and_agree(half_idx=half_idx)

    def global_final(self):
        x_names = self.load('x_names')
        coeff = self.load('coeff')
        ll = self.load('ll')

        smr = self.compute_summary()
        accuracy, confusion_mat, f1, precision, recall = self.compute_confusion_matrix()
        roc_curve, auc, gini = self.compute_roc()

        # Format output data
        # JSON raw
        raw_data = {
            'Covariates'                 : [
                {
                    'Variable'   : x_names[i],
                    'Coefficient': coeff[i],
                    'std.err.'   : smr.stderr[i],
                    'z score'    : smr.z_scores[i],
                    'p value'    : smr.p_values[i] if smr.p_values[i] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
                    'Lower C.I.' : smr.low_ci[i],
                    'Upper C.I.' : smr.high_ci[i]
                }
                for i in range(len(x_names))
            ],
            'Model degrees of freedom'   : smr.df_mod,
            'Residual degrees of freedom': smr.df_resid,
            'Log-likelihood'             : ll,
            'Null model log-likelihood'  : smr.ll0,
            'AIC'                        : smr.aic,
            'BIC'                        : smr.bic,
            'McFadden pseudo-R^2'        : smr.r2_mcf,
            'Cox-Snell pseudo-R^2'       : smr.r2_cs,
            'Confusion matrix'           : confusion_mat,
            'Accuracy'                   : accuracy,
            'Precision'                  : precision,
            'Recall'                     : recall,
            'F1 score'                   : f1,
            'AUC'                        : auc,
            'Gini coefficient'           : gini,
        }
        # Tabular summary 1
        summary1_tabular = []
        for i in range(len(x_names)):
            summary1_tabular.append([
                x_names[i],
                coeff[i],
                smr.stderr[i],
                smr.z_scores[i],
                str(smr.p_values[i]) if smr.p_values[i] >= P_VALUE_CUTOFF else P_VALUE_CUTOFF_STR,
                smr.low_ci[i],
                smr.high_ci[i]
            ])
        table1 = TabularDataResource(
                fields=["variable", "coefficient", "std.err.", "z-score", "p-value", "lower c.i.", "upper c.i."],
                data=summary1_tabular,
                title='Logistic Regression Summary').render()
        table2 = TabularDataResource(
                fields=["model degrees of freedom", "residual degrees of freedom",
                        "log-likelihood", "null model log-likelihood", "AIC", "BIC",
                        "McFadden pseudo-R^2", "Cox-Snell pseudo-R^2"],
                data=[[smr.df_mod, smr.df_resid, ll, smr.ll0, smr.aic, smr.bic, smr.r2_mcf, smr.r2_cs]],
                title='Logistic Regression Summary').render()
        hc_confmat = ConfusionMatrix(title='Confusion Matrix', confusion_matrix=confusion_mat).render()
        hc_roc = ROC(title='ROC Curve', roc_curve=roc_curve, auc=auc, gini=gini).render()
        self.result = AlgorithmResult(raw_data=raw_data, tables=[table1, table2], highcharts=[hc_confmat, hc_roc])

    def predict(self, x=None, coeff=None, threshold=0.5):
        if x is None:
            x = self.data.covariables
        if coeff is None:
            coeff = self.load('coeff')
        return np.array([1 if prob >= threshold else 0 for prob in expit(np.dot(x, coeff))])

    def compute_roc(self):
        true_positives = self.fetch('true_positives')
        true_negatives = self.fetch('true_negatives')
        false_positives = self.fetch('false_positives')
        false_negatives = self.fetch('false_negatives')
        fp_rate = [fp / (fp + tn) if fp != 0 or tn != 0 else 1 for fp, tn in zip(false_positives, true_negatives)]
        tp_rate = [tp / (tp + fn) if tp != 0 or fn != 0 else 1 for tp, fn in zip(true_positives, false_negatives)]
        roc_curve = list(zip(fp_rate, tp_rate))
        auc = 0.0
        for t in range(1, len(fp_rate)):
            auc += 0.5 * (fp_rate[t] - fp_rate[t - 1]) * (tp_rate[t] + tp_rate[t - 1])
        gini = 2 * auc - 1
        return roc_curve, auc, gini

    def compute_confusion_matrix(self):
        n_obs = self.load('n_obs')
        true_positives = self.fetch('true_positives')
        true_negatives = self.fetch('true_negatives')
        false_positives = self.fetch('false_positives')
        false_negatives = self.fetch('false_negatives')
        half_idx = self.fetch('half_idx')
        TP = true_positives[half_idx]
        TN = true_negatives[half_idx]
        FP = false_positives[half_idx]
        FN = false_negatives[half_idx]
        confusion_mat = {'TP': TP, 'TN': TN, 'FP': FP, 'FN': FN}
        accuracy = (TP + TN) / n_obs
        try:
            precision = TP / (TP + FP)
        except ZeroDivisionError:
            precision = 1
        try:
            recall = TP / (TP + FN)
        except ZeroDivisionError:
            recall = 1
        f1 = 2 * (precision * recall) / (precision + recall)
        return accuracy, confusion_mat, f1, precision, recall

    def compute_summary(self):
        n_obs = self.load('n_obs')
        n_cols = self.load('n_cols')
        coeff = self.load('coeff')
        ll = self.load('ll')
        hess = self.load('hess')
        y_sum = self.fetch('y_sum')
        # Stats
        stderr = np.sqrt(
                np.diag(
                        np.linalg.inv(hess)
                )
        )
        z_scores = np.divide(coeff, stderr)
        p_values = (lambda z: st.norm.sf(abs(z)) * 2)(z_scores)
        # Confidence intervals
        low_ci = np.array(
                [st.norm.ppf((1. - CONFIDENCE) / 2., loc=c, scale=s) for c, s in zip(coeff, stderr)]
        )
        high_ci = np.array(
                [st.norm.ppf((1. + CONFIDENCE) / 2., loc=c, scale=s) for c, s in zip(coeff, stderr)]
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
        # pseudo-R^2 McFadden and Cox-Snell
        r2_mcf = 1 - ll / ll0
        r2_cs = 1 - np.exp(2 * (ll0 - ll) / n_obs)

        summary = LogisticRegressionSummary(aic=aic, bic=bic, df_mod=df_mod, df_resid=df_resid, high_ci=high_ci,
                                            ll0=ll0, low_ci=low_ci, p_values=p_values, r2_cs=r2_cs, r2_mcf=r2_mcf,
                                            stderr=stderr, z_scores=z_scores)
        return summary


LogisticRegressionSummary = namedtuple('LogisticRegressionSummary',
                                       ['aic', 'bic', 'df_mod', 'df_resid', 'high_ci', 'll0', 'low_ci', 'p_values',
                                        'r2_cs', 'r2_mcf', 'stderr', 'z_scores', ])

if __name__ == '__main__':
    import time
    algorithm_args = [
        '-x', 'lefthippocampus',
        '-y', 'alzheimerbroadcategory',
        '-pathology', 'dementia',
        '-dataset', 'adni, ppmi, edsd',
        '-filter', '''
                    {
                        "condition": "OR",
                        "rules": [
                            {
                                "id": "alzheimerbroadcategory",
                                "field": "alzheimerbroadcategory",
                                "type": "string",
                                "input": "text",
                                "operator": "equal",
                                "value": "AD"
                            },
                            {
                                "id": "alzheimerbroadcategory",
                                "field": "alzheimerbroadcategory",
                                "type": "string",
                                "input": "text",
                                "operator": "equal",
                                "value": "CN"
                            }
                        ],
                        "valid": true
                    }
        ''',
        '-formula', '',
    ]
    runner = create_runner(for_class='LogisticRegression', found_in='LOGISTIC_EXPERIMENTAL/logistic_regression',
                           alg_type='iterative', num_workers=3, algorithm_args=algorithm_args)
    start = time.time()
    runner.run()
    end = time.time()
    print('Completed in ', end - start)