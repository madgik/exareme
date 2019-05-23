# coding: utf-8
import functions

def raiserror(*args):

    raise functions.OperatorError("ExaremeError", "error ypu have somthg wrong")


raiserror.registered = True

def privacyerror(*args):

    raise functions.OperatorError("PrivacyError","")


privacyerror.registered = True

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
