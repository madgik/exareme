# coding: utf-8
import setpath
import functions
import os



def vartype(*args):
    if args[0] == "0":
        raise functions.OperatorError("TYPE","Provide type Integer,Float or Real")
    else:
        return 1

vartype.registered=True

def vartypecolumns(*args):
    if args[0] == "0":
        raise functions.OperatorError("TYPECOLUMNS","Provide type Integer,Float or Real")
    else:
        return 1

vartypecolumns.registered=True

def vartypeshistogram(*args):
    if args[0] == "0":
        raise functions.OperatorError("TYPEHISTOGRAM","Provide type Integer,Float or Real")
    else:
        return 1

vartypeshistogram.registered=True

def vartypebucket(*args):
    if args[0] == "0":
        raise functions.OperatorError("TYPEBUCKET","Provide type Integer")
    else:
        return 1

vartypebucket.registered=True

def vartypey(*args):
    if args[0] == "0":
        raise functions.OperatorError("TYPEY","Provide type Integer,Float or Real")
    else:
        return 1

vartypey.registered=True

def vartypek(*args):
    if args[0] == "0":
        raise functions.OperatorError("TYPEK","Provide type Integer,Float or Real")
    else:
        return 1

vartypek.registered=True
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