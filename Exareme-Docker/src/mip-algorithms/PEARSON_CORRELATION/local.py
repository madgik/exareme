import sys
from pearson import Pearson

def main(args):
    Pearson(args[1:]).local_()

if __name__ == '__main__':
    Pearson(sys.argv[1:]).local_()
