import sys

from LOGISTIC_EXPERIMENTAL.logistic_regression import LogisticRegression

if __name__ == '__main__':
    LogisticRegression(sys.argv[1:]).local_final()
