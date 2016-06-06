import setpath
import functions
import random
# coding: utf-8
import math
import json
from fractions import Fraction

def dummycode(*args):

    # if type(args[0]) not in (str,unicode):
    #      yield args[0]

    rid = args[0]
    colname = args[1]
    val = args[2]
    values = json.loads(args[3])
    values.pop(0)

    yield ("rid","colname", "val")

    res = []
    for i in xrange(len(values)):
        if val == values[i]:
            yield (rid,colname+values[i],float(1.0))
        else:
            yield (rid,colname+values[i],float(0.0))


dummycode.registered = True



def t_distribution_cdf(*args):

    from scipy import stats

    # colname = args[0]
    number = args[0]
    degreeOfFreedom = args[1]

    # yield ("colname", "valPr")

    result = stats.t.cdf(number, degreeOfFreedom)
    return result
    # yield (colname, result)


t_distribution_cdf.registered = True



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