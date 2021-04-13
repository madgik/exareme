import sys
from threec import ThreeC


def main(args):
    ThreeC(args[1:]).local_pure()


if __name__ == "__main__":
    ThreeC(sys.argv[1:]).local_pure()
