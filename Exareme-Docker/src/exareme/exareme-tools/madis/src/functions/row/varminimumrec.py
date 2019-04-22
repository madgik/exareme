import functions


def varminimumrec(*args):
    if args[0] == "0":
        raise functions.OperatorError("MINIMUMREC", "Provide type Integer,Float or Real")
    else:
        return 1


varminimumrec.registered = True

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
