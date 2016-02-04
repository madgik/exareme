import functions

try:
    from collections import OrderedDict
except ImportError:
    # Python 2.6
    from lib.collections26 import OrderedDict


__docformat__ = 'reStructuredText en'



class fmult:
    """
    .. function:: fsum(X) -> json

    Computes the sum using fractional computation. It return the result in json format

    Examples:

    >>> table1('''
    ... 1
    ... 2
    ... 2
    ... 10
    ... ''')

    >>> sql("select fmult(a) from table1")
    fmult(a)
    -------
    [15]


    """

    registered = True

    def __init__(self):
        self.init = True
        self.x = 1.0

    def step(self, *args):
        if self.init:
            self.init = False
            if not args:
                raise functions.OperatorError("fmult","No arguments")

        try:
            x = float(args[0])
        except KeyboardInterrupt:
            raise
        except:
            return

        self.x *= x

    def final(self):
        return self.x











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