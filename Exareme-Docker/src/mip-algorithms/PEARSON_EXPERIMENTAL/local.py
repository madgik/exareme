import sys
from pearson import Pearson

if __name__ == '__main__':
    alg = Pearson(sys.argv[1:])
    alg.local_()
