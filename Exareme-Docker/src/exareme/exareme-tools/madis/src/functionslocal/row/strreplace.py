import setpath
import functions

def strreplace(*args):

    try:
        mystring = str(args[0])
        result=mystring.replace(' ','')
    except ValueError:
        return None

    return result


strreplace.registered = True

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