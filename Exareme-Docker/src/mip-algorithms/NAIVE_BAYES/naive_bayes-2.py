from __future__ import print_function
from __future__ import division
from __future__ import unicode_literals

from collections import Counter
import warnings

import numpy as np
import scipy
import sklearn.metrics
from sklearn.naive_bayes import GaussianNB
from sklearn.naive_bayes import BaseDiscreteNB

from mipframework import Algorithm
from mipframework import AlgorithmResult


class NaiveBayes(Algorithm):
    def __init__(self, cli_args):
        super(NaiveBayes, self).__init__(__file__, cli_args, intercept=False)

    def local_init(self):
        data = self.data.full
        y, X = data[self.parameters.y], data[self.parameters.x]
        categ_names = [k for k, v in self.metadata.is_categorical.items() if v == 1]
        categ_names.remove(self.parameters.y[0])
        numer_names = [k for k, v in self.metadata.is_categorical.items() if v == 0]
        X_cat = np.array(X[categ_names]) if categ_names else None
        X_num = np.array(X[numer_names]) if numer_names else None
        y = np.array(y)
        # n_splits = int(self.parameters.k)
        nb_model = MixedAdditiveNB(float(self.parameters.alpha))
        nb_model.fit(y, X_num, X_cat)

        self.store(X_cat=X_cat)
        self.store(X_num=X_num)
        self.store(y=y)
        self.push_and_add(nb_model=nb_model)

    def global_init(self):
        nb_model = self.fetch("nb_model")

        self.push(nb_model=nb_model)

    def local_final(self):
        y = self.load("y")
        X_num = self.load("X_num")
        X_cat = self.load("X_cat")
        nb_model = self.fetch("nb_model")

        y_pred = nb_model.predict(X_num, X_cat)
        n_hits = sum(y_pred == np.array(y).flatten())
        n_miss = len(y) - n_hits

        self.push_and_add(n_hits=n_hits)
        self.push_and_add(n_miss=n_miss)

    def global_final(self):
        n_hits = self.fetch("n_hits")
        n_miss = self.fetch("n_miss")

        self.result = AlgorithmResult(
            raw_data={"precision": n_hits / (n_hits + n_miss)}
        )


class MixedAdditiveNB(object):
    def __init__(self, alpha=1.0):
        self.alpha = alpha
        self.gnb = None
        self.cnb = None

    def fit(self, y, X_num=None, X_cat=None):
        if X_num is not None:
            self.gnb = AdditiveGaussianNB()
            self.gnb.fit(X_num, y)
        if X_cat is not None:
            self.cnb = AdditiveCategoricalNB(alpha=self.alpha)
            self.cnb.fit(X_cat, y)

    def predict(self, X_num, X_cat):
        if X_num is not None and X_cat is not None:
            jll = (
                self.gnb.predict_log_proba(X_num)
                + self.cnb.predict_log_proba(X_cat)
                - self.gnb.class_log_prior_
            )
            return np.array([self.gnb.classes_[i] for i in jll.argmax(axis=1)])
        elif X_num is not None:
            return self.gnb.predict(X_num)
        elif X_cat is not None:
            return self.cnb.predict(X_cat)

    def __add__(self, other):
        result = MixedAdditiveNB()
        if self.gnb and other.gnb:
            result.gnb = self.gnb + other.gnb
        if self.cnb and other.cnb:
            result.alpha = self.alpha
            result.cnb = self.cnb + other.cnb
        return result


class AdditiveCategoricalNB(BaseDiscreteNB):
    def __init__(self, alpha=1.0):
        self.alpha = alpha
        self._class_log_prior_ = np.array([])
        self._feature_log_prob_ = []

    def fit(self, X, y):
        self.n_obs_, self.n_features_ = X.shape
        self.classes_, self.class_count_ = np.unique(y, return_counts=True)
        self.n_classes_ = len(self.classes_)
        self.categories_, self.category_per_feat_count_ = list(
            zip(*[np.unique(col, return_counts=True) for col in X.T])
        )
        self.n_categories_ = np.array([len(c) for c in self.categories_])
        self.category_count_ = [
            np.empty((self.n_classes_, self.n_categories_[f]))
            for f in xrange(self.n_features_)
        ]
        for ci, c in enumerate(self.classes_):
            X_where_x = X[np.where(y == c)[0]]
            for fi, feature in enumerate(X_where_x.T):
                counter = Counter(feature)
                self.category_count_[fi][ci, :] = np.array(
                    [counter[cat] for cat in self.categories_[fi]]
                )

    def __add__(self, other):
        def sum_elementwise(x, y):
            return [xi + yi for xi, yi in zip(x, y)]

        if self.alpha != other.alpha:
            raise ValueError("alphas do not agree")
        result = AdditiveCategoricalNB(alpha=self.alpha)

        result.n_obs_ = self.n_obs_ + other.n_obs_

        if self.n_features_ != other.n_features_:
            raise ValueError("n_features_ do not agree")
        result.n_features_ = self.n_features_

        if (self.classes_ != other.classes_).all():
            raise ValueError("classes_ do not agree")
        result.classes_ = self.classes_

        result.class_count_ = self.class_count_ + other.class_count_

        if self.n_classes_ != other.n_classes_:
            raise ValueError("n_classes_ do not agree")
        result.n_classes_ = self.n_classes_

        result.category_per_feat_count_ = sum_elementwise(
            self.category_per_feat_count_, other.category_per_feat_count_
        )

        if not all(
            [(c1 == c2).all() for c1, c2 in zip(self.categories_, other.categories_)]
        ):
            raise ValueError("catefories_ do not agree")
        result.categories_ = self.categories_

        result.n_categories_ = sum_elementwise(self.n_categories_, other.n_categories_)

        result.category_count_ = sum_elementwise(
            self.category_count_, other.category_count_
        )

        return result

    @property
    def class_log_prior_(self):
        if not self._class_log_prior_.any():
            with warnings.catch_warnings():
                # silence the warning when count is 0 because class was not yet
                # observed
                warnings.simplefilter("ignore", RuntimeWarning)
                log_class_count = np.log(self.class_count_)
            self._class_log_prior_ = log_class_count - np.log(self.class_count_.sum())
        return self._class_log_prior_

    @property
    def feature_log_prob_(self):
        if not self._feature_log_prob_:
            feature_log_prob = []
            for i in range(self.n_features_):
                smoothed_cat_count = self.category_count_[i] + self.alpha
                smoothed_class_count = smoothed_cat_count.sum(axis=1)
                feature_log_prob.append(
                    np.log(smoothed_cat_count)
                    - np.log(smoothed_class_count.reshape(-1, 1))
                )
            self._feature_log_prob_ = feature_log_prob
        return self._feature_log_prob_

    def _joint_log_likelihood(self, X):
        if not X.shape[1] == self.n_features_:
            raise ValueError(
                "Expected input with %d features, got %d instead"
                % (self.n_features_, X.shape[1])
            )
        jll = np.zeros((X.shape[0], self.class_count_.shape[0]))
        for i in range(self.n_features_):
            categories = X[:, i]
            indices = [np.where(self.categories_[i] == cat)[0][0] for cat in categories]
            jll += self.feature_log_prob_[i][:, indices].T
        total_ll = jll + self.class_log_prior_
        return total_ll

    def __eq__(self, other):
        raise NotImplementedError


class AdditiveGaussianNB(GaussianNB):
    def __init__(self, priors=None, var_smoothing=1e-9):
        self._class_log_prior_ = np.array([])
        super(AdditiveGaussianNB, self).__init__(priors, var_smoothing)

    def fit(self, X, y):
        self.n_obs_, self.n_feats_ = X.shape
        super(AdditiveGaussianNB, self).fit(X, y)

    @property
    def class_log_prior_(self):
        if not self._class_log_prior_.any():
            with warnings.catch_warnings():
                # silence the warning when count is 0 because class was not yet
                # observed
                warnings.simplefilter("ignore", RuntimeWarning)
                log_class_count = np.log(self.class_count_)
            self._class_log_prior_ = log_class_count - np.log(self.class_count_.sum())
        return self._class_log_prior_

    def __add__(self, other):
        if self.var_smoothing != other.var_smoothing:
            raise ValueError("var_smoothing values do not agree")
        if self.priors != other.priors:
            raise ValueError("priors do not agree")
        if (self.classes_ != other.classes_).all():
            raise ValueError("classes_ do not agree")

        class_count_1 = self.class_count_[:, np.newaxis]
        class_count_2 = other.class_count_[:, np.newaxis]
        n_obs_total = self.n_obs_ + other.n_obs_
        class_count_total = class_count_1 + class_count_2

        theta_total = (
            class_count_1 * self.theta_ + class_count_2 * other.theta_
        ) / class_count_total

        self.sigma_[:, :] -= self.epsilon_
        other.sigma_[:, :] -= other.epsilon_
        epsilon_total = max(self.epsilon_, other.epsilon_)
        ssd_1 = class_count_1 * self.sigma_
        ssd_2 = class_count_2 * other.sigma_
        total_ssd = (
            ssd_1
            + ssd_2
            + (class_count_1 * class_count_2 / class_count_total)
            * (self.theta_ - other.theta_) ** 2
        )
        sigma_total = total_ssd / class_count_total
        sigma_total += epsilon_total

        result = AdditiveGaussianNB(self.priors, self.var_smoothing)
        result.n_obs_ = n_obs_total
        result.classes_ = self.classes_
        result.sigma_ = sigma_total
        result.theta_ = theta_total
        result.epsilon_ = epsilon_total
        result.class_count_ = class_count_total.flatten()
        result.class_prior_ = result.class_count_ / n_obs_total
        return result

    def __eq__(self, other):
        if self.var_smoothing != other.var_smoothing:
            return False
        if self.priors != other.priors:
            return False
        if (self.classes_ != other.classes_).all():
            return False
        if not np.isclose(self.theta_, other.theta_).all():
            return False
        if not np.isclose(self.sigma_, other.sigma_).all():
            return self.sigma_, other.sigma_
        if (self.class_count_ != other.class_count_).all():
            return False
        if (self.class_prior_ != other.class_prior_).all():
            return False
        if self.n_obs_ != other.n_obs_:
            return False
        if self.n_feats_ != other.n_feats_:
            return False
        return True


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-x",
        "lefthippocampus,righthippocampus,leftaccumbensarea,gender,apoe4,agegroup",
        "-y",
        "alzheimerbroadcategory",
        "-alpha",
        "1",
        "-k",
        "1",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        "",
    ]
    runner = create_runner(NaiveBayes, algorithm_args=algorithm_args, num_workers=2,)
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
