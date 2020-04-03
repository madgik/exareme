from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from collections import namedtuple

import numpy as np
import scipy.stats as st
from scipy.special import expit, xlogy
from mipframework import Algorithm, AlgorithmResult
from mipframework import TabularDataResource
from mipframework import create_runner
from mipframework.highcharts import ConfusionMatrix, ROC
from mipframework.constants import (
    P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR, PREC,
    MAX_ITER, CONFIDENCE
)


class LogisticRegression(Algorithm):
    def __init__(self, cli_args):
        super(LogisticRegression, self).__init__(__file__, cli_args)

    def local_init(self):
        y, X = self.data.variables.iloc[:, 1], self.data.covariables

        n_obs = len(y)  # todo make these variables automatically available on global
        n_cols = len(X.columns)
        y_name = y.name
        x_names = list(X.columns)

        self.push_and_add(n_obs=n_obs)
        self.push_and_agree(n_cols=n_cols)
        self.push_and_agree(y_name=y_name)
        self.push_and_agree(x_names=x_names)

    def global_init(self):
        n_obs = self.fetch('n_obs')
        n_cols = self.fetch('n_cols')
        y_name = self.fetch('y_name')
        x_names = self.fetch('x_names')

        coeff, iter_, ll = init_model(n_cols, n_obs)

        self.store(n_obs=n_obs)
        self.store(n_cols=n_cols)
        self.store(ll=ll)
        self.store(iter_=iter_)
        self.store(y_name=y_name)
        self.store(x_names=x_names)
        self.push(coeff=coeff)

    def local_step(self):
        y, X = self.data.variables.iloc[:, 1], self.data.covariables
        coeff = self.fetch('coeff')

        grad, hess, ll = update_local_model_parameters(X, y, coeff)

        self.store(coeff=coeff)
        self.push_and_add(ll=ll)
        self.push_and_add(grad=grad)
        self.push_and_add(hess=hess)

    def global_step(self):
        ll_old = self.load('ll')
        ll_new = self.fetch('ll')
        iter_ = self.load('iter_')
        grad = self.fetch('grad')
        hess = self.fetch('hess')

        coeff = update_coefficients(grad, hess)

        # Verify termination condition
        delta = abs(ll_new - ll_old)
        iter_ += 1
        if delta < PREC or iter_ >= MAX_ITER:
            self.terminate()

        self.store(ll=ll_new)
        self.store(hess=hess)
        self.store(coeff=coeff)
        self.store(iter_=iter_)
        self.push(coeff=coeff)

    def local_final(self):
        y = self.data.variables.iloc[:, 1]

        thresholds = np.linspace(1.0, 0.0, num=2 ** 7 + 1)  # odd otherwise no half_idx
        yhats = np.array([self.predict(threshold=thr) for thr in thresholds])
        fn, fp, tn, tp = compute_classification_results(y, yhats)
        half_idx = np.where(thresholds == 0.5)[0][0]

        self.push_and_add(y_sum=np.sum(y))
        self.push_and_add(true_positives=tp)
        self.push_and_add(true_negatives=tn)
        self.push_and_add(false_positives=fp)
        self.push_and_add(false_negatives=fn)
        self.push_and_agree(half_idx=half_idx)

    def global_final(self):
        x_names = self.load('x_names')
        coeff = self.load('coeff')
        ll = self.load('ll')
        hess = self.load('hess')
        n_obs = self.load('n_obs')
        n_cols = self.load('n_cols')
        y_sum = self.fetch('y_sum')
        tp = self.fetch('true_positives')
        tn = self.fetch('true_negatives')
        fp = self.fetch('false_positives')
        fn = self.fetch('false_negatives')
        half_idx = self.fetch('half_idx')

        smr = compute_summary(n_obs, n_cols, coeff, ll, hess, y_sum)
        cm_smr = compute_confusion_matrix(tp[half_idx], tn[half_idx], fp[half_idx],
                                          fn[half_idx])
        roc_curve, auc, gini = compute_roc(tp, tn, fp, fn)

        p_values = [
            str(pv)
            if pv >= P_VALUE_CUTOFF
            else P_VALUE_CUTOFF_STR
            for pv in smr.p_values
        ]

        # Collect output data
        out_data = {
            'Coefficients'               : list(coeff),
            'Names'                      : list(x_names),
            'Std.Err'                    : list(smr.stderr),
            'z score'                    : list(smr.z_scores),
            'p value'                    : list(p_values),
            'Lower C.I.'                 : list(smr.low_ci),
            'Upper C.I.'                 : list(smr.high_ci),
            'Model degrees of freedom'   : smr.df_mod,
            'Residual degrees of freedom': smr.df_resid,
            'Log-likelihood'             : ll,
            'Null model log-likelihood'  : smr.ll0,
            'AIC'                        : smr.aic,
            'BIC'                        : smr.bic,
            'McFadden pseudo-R^2'        : smr.r2_mcf,
            'Cox-Snell pseudo-R^2'       : smr.r2_cs,
            'Confusion matrix'           : cm_smr.confusion_mat,
            'Accuracy'                   : cm_smr.accuracy,
            'Precision'                  : cm_smr.precision,
            'Recall'                     : cm_smr.recall,
            'F1 score'                   : cm_smr.f1,
            'AUC'                        : auc,
            'Gini coefficient'           : gini,
        }

        table_1 = TabularDataResource(
            fields=["variable", "coefficient", "std.err.", "z-score", "p-value",
                    "lower c.i.", "upper c.i."],
            data=list(zip(x_names, coeff, smr.stderr, smr.z_scores,
                          p_values, smr.low_ci, smr.high_ci)),
            title='Logistic Regression Coefficients'
        )

        table_2 = TabularDataResource(
            fields=["model degrees of freedom", "residual degrees of freedom",
                    "log-likelihood", "null model log-likelihood", "AIC", "BIC",
                    "McFadden pseudo-R^2", "Cox-Snell pseudo-R^2"],
            data=[(smr.df_mod, smr.df_resid, ll, smr.ll0, smr.aic, smr.bic,
                   smr.r2_mcf, smr.r2_cs)],
            title='Logistic Regression Summary'
        )

        hc_confmat = ConfusionMatrix(
            title='Confusion Matrix',
            confusion_matrix=cm_smr.confusion_mat
        )

        hc_roc = ROC(
            title='ROC Curve', roc_curve=roc_curve, auc=auc,
            gini=gini
        )

        self.result = AlgorithmResult(
            raw_data=out_data, tables=[table_1, table_2],
            highcharts=[hc_confmat, hc_roc]
        )

    def predict(self, x=None, coeff=None, threshold=0.5):
        if x is None:
            x = self.data.covariables
        if coeff is None:
            coeff = self.load('coeff')
        return np.array(
            [1 if prob >= threshold else 0 for prob in expit(np.dot(x, coeff))]
        )


def init_model(n_cols, n_obs):
    ll = - 2 * n_obs * np.log(2)
    coeff = np.zeros(n_cols)
    iter_ = 0
    return coeff, iter_, ll


def update_local_model_parameters(X, y, coeff):
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
    # Stable computation of (Y - s) / d
    y_ratio = (y - s) / d
    y_ratio[(y == 0) & (s == 0)] = -1
    y_ratio[(y == 1) & (s == 1)] = 1
    y_ratio = y_ratio.clip(-1e6, 1e6)  # clip inf's to avoid nan's in grad
    # Gradient
    grad = np.dot(
        np.transpose(X),
        np.dot(
            D,
            z + y_ratio
        )
    )
    # Log-likelihood
    ll = np.sum(xlogy(y, s) + xlogy(1 - y, 1 - s))
    return grad, hess, ll


def update_coefficients(grad, hess):
    if np.isinf(hess).any():
        hess = hess.clip(-1e6, 1e6)
    if np.isinf(grad).any():
        grad = grad.clip(-1e6, 1e6)
    # If inverse fails try Moore-Penrose pseudo-inverse
    try:
        inv_hess = np.linalg.inv(hess)
        if np.isnan(inv_hess).any():
            raise np.linalg.LinAlgError
    except np.linalg.LinAlgError:
        inv_hess = np.linalg.pinv(hess)
    if np.isclose(grad, 0.).any() and np.isinf(inv_hess).any():
        inv_hess = inv_hess.clip(-1e6, 1e6)
    coeff = np.dot(
        inv_hess,
        grad
    )
    return coeff


def compute_classification_results(y, yhats):
    true_positives = np.array([
        sum(1 if yi == yhi == 1 else 0 for yi, yhi in zip(y, yhat))
        for yhat in yhats
    ])
    true_negatives = np.array([
        sum(1 if yi == yhi == 0 else 0 for yi, yhi in zip(y, yhat))
        for yhat in yhats
    ])
    false_positives = np.array([
        sum(1 if yi == 0 and yhi == 1 else 0 for yi, yhi in zip(y, yhat))
        for yhat in yhats
    ])
    false_negatives = np.array([
        sum(1 if yi == 1 and yhi == 0 else 0 for yi, yhi in zip(y, yhat))
        for yhat in yhats
    ])
    return false_negatives, false_positives, true_negatives, true_positives


def compute_summary(n_obs, n_cols, coeff, ll, hess, y_sum):
    # Stats
    try:
        inv_hess = np.linalg.inv(hess)
        if np.isnan(inv_hess).any():
            raise np.linalg.LinAlgError
    except np.linalg.LinAlgError:
        inv_hess = np.linalg.pinv(hess)
    stderr = np.sqrt(np.diag(inv_hess))
    z_scores = np.divide(coeff, stderr)
    p_values = (lambda z: st.norm.sf(abs(z)) * 2)(z_scores)
    # Confidence intervals
    low_ci = np.array([
        st.norm.ppf((1. - CONFIDENCE) / 2., loc=c, scale=s)
        for c, s in zip(coeff, stderr)
    ])
    high_ci = np.array([
        st.norm.ppf((1. + CONFIDENCE) / 2., loc=c, scale=s)
        for c, s in zip(coeff, stderr)
    ])
    # Degrees of freedom
    df_mod = n_cols - 1
    df_resid = n_obs - df_mod - 1
    # Null model log-likelihood
    y_mean = y_sum / n_obs
    ll0 = xlogy(y_sum, y_mean) + xlogy(n_obs - y_sum, 1.0 - y_mean)
    # AIC
    aic = 2 * n_cols - 2 * ll
    # BIC
    bic = np.log(n_obs) * n_cols - 2 * ll
    # pseudo-R^2 McFadden and Cox-Snell
    if np.isclose(ll, 0.0) and np.isclose(ll0, 0.0):
        r2_mcf = 1
    else:
        r2_mcf = 1 - ll / ll0
    r2_cs = 1 - np.exp(2 * (ll0 - ll) / n_obs)

    summary = LogisticRegressionSummary(aic=aic, bic=bic, df_mod=df_mod,
                                        df_resid=df_resid, high_ci=high_ci,
                                        ll0=ll0, low_ci=low_ci,
                                        p_values=p_values, r2_cs=r2_cs,
                                        r2_mcf=r2_mcf,
                                        stderr=stderr, z_scores=z_scores)
    return summary


def compute_confusion_matrix(tp, tn, fp, fn):
    confusion_mat = {'True Positives' : tp, 'True Negatives': tn,
                     'False Positives': fp, 'False Negatives': fn}
    accuracy = (tp + tn) / (tp + tn + fp + fn)
    try:
        precision = tp / (tp + fp)
    except ZeroDivisionError:
        precision = 1
    try:
        recall = tp / (tp + fn)
    except ZeroDivisionError:
        recall = 1
    try:
        f1 = 2 * (precision * recall) / (precision + recall)
    except ZeroDivisionError:
        f1 = 2
    return ConfusionMatrixSummary(accuracy, precision, recall, confusion_mat, f1)


def compute_roc(true_positives, true_negatives, false_positives, false_negatives):
    fp_rate = [fp / (fp + tn)
               if fp != 0 or tn != 0
               else 1
               for fp, tn in zip(false_positives, true_negatives)]
    tp_rate = [tp / (tp + fn)
               if tp != 0 or fn != 0
               else 1
               for tp, fn in zip(true_positives, false_negatives)]
    roc_curve = list(zip(fp_rate, tp_rate))
    auc = np.trapz(tp_rate, fp_rate)
    gini = 2 * auc - 1
    return roc_curve, auc, gini


LogisticRegressionSummary = namedtuple('LogisticRegressionSummary',
                                       ['aic', 'bic', 'df_mod', 'df_resid',
                                        'high_ci', 'll0', 'low_ci', 'p_values',
                                        'r2_cs', 'r2_mcf', 'stderr', 'z_scores', ])

ConfusionMatrixSummary = namedtuple('ConfusionMatrixSummary',
                                    'accuracy precision recall confusion_mat f1')

if __name__ == '__main__':
    import time

    algorithm_args = [
        '-x',
        'lefthippocampus',
        '-y', 'alzheimerbroadcategory',
        '-pathology', 'dementia',
        '-dataset', 'adni',
        '-filter', """
        {"condition": "OR", "rules": [{"id": "alzheimerbroadcategory", "field": 
        "alzheimerbroadcategory", "type": "string", "input": "text", "operator": 
        "equal", "value": "AD"}, {"id": "alzheimerbroadcategory", "field": "alzheimerbroadcategory", "type": "string", "input": "text", "operator": "equal", "value": "CN"}], "valid": true}
        """,
        '-formula', '',
    ]
    runner = create_runner(for_class='LogisticRegression',
                           found_in='LOGISTIC_REGRESSION/logistic_regression',
                           alg_type='iterative', num_workers=1,
                           algorithm_args=algorithm_args)
    start = time.time()
    runner.run()
    end = time.time()
    print('Completed in ', end - start)
