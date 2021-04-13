import sys
from anova import Anova


def main(args):
    Anova(args[1:]).global_()


if __name__ == "__main__":
    Anova(sys.argv[1:]).global_()
