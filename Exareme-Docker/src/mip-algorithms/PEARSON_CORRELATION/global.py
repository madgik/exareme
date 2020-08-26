import sys
from pearson import Pearson

def main(args):
    Pearson(args[1:]).global_()

if __name__ == '__main__':
    Pearson(sys.argv[1:]).global_()
