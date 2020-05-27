import sys
from KAPLAN_MEIER import KaplanMeier

def main(args):
    KaplanMeier(args[1:]).local_()

if __name__ == "__main__":
    KaplanMeier(sys.argv[1:]).local_()
