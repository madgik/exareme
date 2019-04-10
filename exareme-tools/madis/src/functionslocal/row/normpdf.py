# coding: utf-8
import math


def normpdf(*args):
    """
    function:: normpdf(mean, sigma) -> float

    Returns the probability density function for the normal distribution

    """

    try:
        x = args[0]
        mean = args[1]
        sd = args[2]
        var = float(sd) ** 2
        pi = 3.1415926
        denom = (2 * pi * var) ** .5
        num = math.exp(-(float(x) - float(mean)) ** 2 / (2 * var))
        ret = num / denom
    except ValueError:
        return None

    return ret


normpdf.registered = True

if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    from functions import *

    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest

        doctest.testmod()
