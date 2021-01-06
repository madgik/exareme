from __future__ import print_function
from __future__ import division
from __future__ import unicode_literals

from collections import namedtuple

import numpy as np
from sklearn.model_selection import KFold


def kfold_split_design_matrices(n_splits, *matrices):
    train_sets = []
    test_sets = []
    kf = KFold(n_splits=n_splits, random_state=0)
    for train_idx, test_idx in kf.split(matrices[0]):
        train_sets.append([m[train_idx] for m in matrices])
        test_sets.append([m[test_idx] for m in matrices])
    return train_sets, test_sets


def kfold_split_design_matrix(X, n_splits):
    kf = KFold(n_splits=n_splits)
    train_sets = []
    test_sets = []
    for train_idx, test_idx in kf.split(X):
        train_sets.append(X[train_idx])
        test_sets.append(X[test_idx])
    return train_sets, test_sets


def compute_classification_results(y, yhats):
    true_positives = np.array(
        [sum(1 if yi == yhi == 1 else 0 for yi, yhi in zip(y, yhat)) for yhat in yhats]
    )
    true_negatives = np.array(
        [sum(1 if yi == yhi == 0 else 0 for yi, yhi in zip(y, yhat)) for yhat in yhats]
    )
    false_positives = np.array(
        [
            sum(1 if yi == 0 and yhi == 1 else 0 for yi, yhi in zip(y, yhat))
            for yhat in yhats
        ]
    )
    false_negatives = np.array(
        [
            sum(1 if yi == 1 and yhi == 0 else 0 for yi, yhi in zip(y, yhat))
            for yhat in yhats
        ]
    )
    return false_negatives, false_positives, true_negatives, true_positives


ConfusionMatrixSummary = namedtuple(
    "ConfusionMatrixSummary", "accuracy precision recall confusion_mat f1"
)


def compute_confusion_matrix(tp, tn, fp, fn):
    confusion_mat = {
        "True Positives": tp,
        "True Negatives": tn,
        "False Positives": fp,
        "False Negatives": fn,
    }
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
    fp_rate = [
        fp / (fp + tn) if fp != 0 or tn != 0 else 1
        for fp, tn in zip(false_positives, true_negatives)
    ]
    tp_rate = [
        tp / (tp + fn) if tp != 0 or fn != 0 else 1
        for tp, fn in zip(true_positives, false_negatives)
    ]
    roc_curve = list(zip(fp_rate, tp_rate))
    auc = np.trapz(tp_rate, fp_rate)
    gini = 2 * auc - 1
    return roc_curve, auc, gini


class AdditiveMulticlassROCCurve(object):
    def __init__(
        self,
        y_true=None,
        y_pred_proba_per_class=None,
        classes=None,
        tp=None,
        tn=None,
        fp=None,
        fn=None,
    ):
        if (tp, tn, fp, fn) == (None, None, None, None):
            if len(y_true.shape) > 1:
                y_true = y_true.flatten()
            self.tp = []
            self.tn = []
            self.fp = []
            self.fn = []
            self.classes = classes
            for ci, c in enumerate(classes):
                y_pred_proba = y_pred_proba_per_class[:, ci]
                thres = np.linspace(1.0, 0.0, num=2 ** 7 + 1)
                self.tp.append(
                    ((y_true == c) & (y_pred_proba >= thres[:, None])).sum(axis=1)
                )
                self.tn.append(
                    ((y_true != c) & (y_pred_proba < thres[:, None])).sum(axis=1)
                )
                self.fp.append(
                    ((y_true != c) & (y_pred_proba >= thres[:, None])).sum(axis=1)
                )
                self.fn.append(
                    ((y_true == c) & (y_pred_proba < thres[:, None])).sum(axis=1)
                )
        elif tp and tn and fp and fn:
            self.tp = tp
            self.tn = tn
            self.fp = fp
            self.fn = fn

    def __add__(self, other):
        result = AdditiveMulticlassROCCurve(
            tp=[tp_1 + tp_2 for tp_1, tp_2 in zip(self.tp, other.tp)],
            tn=[tn_1 + tn_2 for tn_1, tn_2 in zip(self.tn, other.tn)],
            fp=[fp_1 + fp_2 for fp_1, fp_2 in zip(self.fp, other.fp)],
            fn=[fn_1 + fn_2 for fn_1, fn_2 in zip(self.fn, other.fn)],
        )
        if (self.classes == other.classes).all():
            result.classes = self.classes
        else:
            raise ValueError("classes do not agree")
        return result

    def get_curves(self):
        curves = []
        for ci, c in enumerate(self.classes):
            tpr = self.tp[ci] / (self.tp[ci] + self.fn[ci])
            tpr[np.isnan(tpr)] = 1.0
            fpr = self.fp[ci] / (self.fp[ci] + self.tn[ci])
            fpr[np.isnan(fpr)] = 1.0
            curves.append((fpr.tolist(), tpr.tolist()))
        return curves


if __name__ == "__main__":
    classes = np.array(["AD", "CN", "Other"])
    y_true = np.array(["AD", "AD", "Other", "CN", "CN", "AD"])
    y_pred_proba_per_class = np.array(
        [
            [0.6, 0.2, 0.1],
            [0.38, 0.42, 0.2],
            [0.4, 0.1, 0.5],
            [0.3, 0.3, 0.4],
            [0.2, 0.45, 0.35],
            [0.5, 0.3, 0.2],
        ]
    )
    roc = MulticlassROCCurve(y_true, y_pred_proba_per_class, classes)
