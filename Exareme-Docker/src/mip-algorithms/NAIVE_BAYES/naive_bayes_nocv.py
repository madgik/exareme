from __future__ import print_function
from __future__ import division
from __future__ import unicode_literals

from collections import Counter
import warnings

import numpy as np
from sklearn.naive_bayes import GaussianNB
from sklearn.naive_bayes import BaseDiscreteNB

from mipframework import Algorithm
from mipframework import AlgorithmResult


class MixedAdditiveNB(object):
    def __init__(self, alpha=1.0):
        self.alpha = alpha

    def fit(self, X_num, X_cat, y):
        self.gnb = AdditiveGaussianNB()
        self.gnb.fit(X_num, y)
        self.cnb = AdditiveCategoricalNB(alpha=self.alpha)
        self.cnb.fit(X_cat, y)

    def predict(self, X_num, X_cat):
        jll = (
            self.gnb.predict_log_proba(X_num)
            + self.cnb.predict_log_proba(X_cat)
            - self.gnb.class_log_prior_
        )
        return np.array([self.gnb.classes_[i] for i in jll.argmax(axis=1)])


class MixedNaiveBayesTrain(Algorithm):
    def __init__(self, cli_args):
        super(MixedNaiveBayesTrain, self).__init__(__file__, cli_args, intercept=False)

    def local_(self):
        data = self.data.full
        y, X = data[self.parameters.y], data[self.parameters.x]
        X_num = np.array(X.iloc[:, :3])
        X_cat = np.array(X.iloc[:, 3:])
        y = np.array(y)
        mnb = MixedAdditiveNB()
        mnb.fit(X_num, X_cat, y)
        pass


def run_mixed():
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
    runner = create_runner(
        MixedNaiveBayesTrain, algorithm_args=algorithm_args, num_workers=1,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)


class CategoricalNaiveBayesTrain(Algorithm):
    def __init__(self, cli_args):
        super(CategoricalNaiveBayesTrain, self).__init__(
            __file__, cli_args, intercept=False
        )

    def local_(self):
        data = self.data.full
        y, X = data[self.parameters.y], data[self.parameters.x]
        y, X = np.array(y), np.array(X)
        cnb = AdditiveCategoricalNB()
        cnb.fit(X, y)
        self.push_and_add(cnb=cnb)

    def global_(self):
        cnb = self.fetch("cnb")

        self.result = AlgorithmResult(
            raw_data={"category_count": [cc.tolist() for cc in cnb.category_count_]}
        )


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


def run_categorical():
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-x",
        "gender,apoe4,agegroup",
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
    runner = create_runner(
        CategoricalNaiveBayesTrain, algorithm_args=algorithm_args, num_workers=10,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)


class GaussianNaiveBayesTrain(Algorithm):
    def __init__(self, cli_args):
        super(GaussianNaiveBayesTrain, self).__init__(
            __file__, cli_args, intercept=False
        )

    def local_(self):
        data = self.data.full
        y, X = data[self.parameters.y], data[self.parameters.x]
        y, X = np.array(y), np.array(X)
        gnb = AdditiveGaussianNB()
        gnb.fit(X, y)
        self.push_and_add(gnb=gnb)

    def global_(self):
        gnb = self.fetch("gnb")

        self.result = AlgorithmResult(
            raw_data={"theta": gnb.theta_.tolist(), "sigma": gnb.sigma_.tolist()}
        )


class AdditiveGaussianNB(GaussianNB):
    def fit(self, X, y):
        self.n_obs_, self.n_feats_ = X.shape
        self._class_log_prior_ = np.array([])
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


def run_gaussian():
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-x",
        "lefthippocampus,righthippocampus,leftaccumbensarea,"
        "leftacgganteriorcingulategyrus,leftainsanteriorinsula,leftamygdala",
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
    runner = create_runner(
        GaussianNaiveBayesTrain, algorithm_args=algorithm_args, num_workers=1,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
    runner = create_runner(
        GaussianNaiveBayesTrain, algorithm_args=algorithm_args, num_workers=1,
    )
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)


if __name__ == "__main__":
    # run_gaussian()
    # run_categorical()
    run_mixed()
