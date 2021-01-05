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
