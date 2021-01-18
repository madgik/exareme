import sys
from anova import Anova


def main(args):
    Anova(args[1:]).local_()


if __name__ == "__main__":
    Anova(sys.argv[1:]).local_()
