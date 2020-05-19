import sys
from KAPLAN_MEIER import KaplanMeier

if __name__ == "__main__":
    KaplanMeier(sys.argv[1:]).local_()
