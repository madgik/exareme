# coding: utf-8
import setpath
import functions
import os

def vars(*args):
    """
    *args is what the query: "var 'valExists' from select case when (select exists (select colname from
    tempinputlocaltbl1 where colname='%{variable}'))=0 then 0 else 1 end;" returns
    """

    if args[0] == "0":
        raise functions.OperatorError("VARIABLE","Variable does not exist")
    else:
        return 1
vars.registered=True



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