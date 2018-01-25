# coding: utf-8
import setpath
import functions
import os


def null_table(*args):
    if args[0] == "0":
        raise functions.OperatorError("NULLTABLE","Variable does not exist")
    else:
        return 1

null_table.registered=True

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