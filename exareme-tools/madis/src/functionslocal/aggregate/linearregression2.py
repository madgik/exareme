import setpath
import functions
import math
import numpy as np
from numpy.linalg import inv
from lib import iso8601
import lib.jopts as jopts
import re
import datetime
import json
from fractions import Fraction
import lib.jopts as jopts
from array import *

import itertools

try:
    from collections import OrderedDict
except ImportError:
    # Python 2.6
    from lib.collections26 import OrderedDict


__docformat__ = 'reStructuredText en'


class invertarray:

    registered = True

    def __init__(self):
        self.init = True
        self.sizeofarray = None
        self.arrayval = None
        self.arrayattr = []
        self.i =None
        self.j =None

    def step(self, *args):
        print args
        if self.init:
            self.init = False
            self.size= args[3]
            self.arrayval =  np.empty([self.size,self.size])
            self.i = 0
            self.j = 0

        if self.i == 0:
            self.arrayattr.append(str(args[1]))
        self.arrayval[self.i,self.j] = float(args[2])

        if self.j == self.size - 1:
            self.j = 0
            self.i = self.i + 1
        else:
            self.j = self.j + 1

    def final(self):
        print "invert array"
        print self.arrayval
        print self.arrayattr
        print "stop"

        ArrayInvert = inv(self.arrayval)
        yield ('attr1', 'attr2', 'val')
        for i in range(0,self.size):
            for j in range(0,self.size):
                yield self.arrayattr[i], self.arrayattr[j], ArrayInvert[i,j]


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    import setpath
    from functions import *
    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest
        doctest.testmod()
