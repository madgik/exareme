import sys

from LOGISTIC_REGRESSION.logistic_regression import LogisticRegression

def main(args):
    LogisticRegression(args[1:]).global_final()

if __name__ == "__main__":
    LogisticRegression(sys.argv[1:]).global_final()
