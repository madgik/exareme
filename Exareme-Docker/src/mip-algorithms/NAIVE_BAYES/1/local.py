import sys
from NAIVE_BAYES import NaiveBayes


def main(args):
    NaiveBayes(args[1:]).local_init()


if __name__ == "__main__":
    NaiveBayes(sys.argv[1:]).local_init()
