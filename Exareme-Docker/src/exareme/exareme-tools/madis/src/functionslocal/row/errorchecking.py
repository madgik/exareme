import functions


def privacychecking(*args):
    minNumberOfData = 10
    if int(args[0]) < 10 :
        raise functions.OperatorError("PrivacyError","")
    else:
        return "OK"

privacychecking.registered = True


def kmeans_inputerrorchecking(centers,k):
    if (centers == '' and k == '') or (centers != '' and k != ''):
        raise functions.OperatorError("ExaremeError", "Only one of the following two parameters should be empty/have value: Centers or k")
    else:
        return "OK"

kmeans_inputerrorchecking.registered = True


def histograms_inputerrorchecking(xisCategorical,bins):
    if (xisCategorical == 1 and bins != '') or ( xisCategorical == 0 and bins == '') :
        raise functions.OperatorError("ExaremeError", "Bins parameter should be empty when x is categorical")
    else:
        return "OK"

histograms_inputerrorchecking.registered = True


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
