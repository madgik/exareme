import functions
import re

def privacychecking(*args):
    minNumberOfData = 10
    if int(args[0]) < 10 :
        raise functions.OperatorError("PrivacyError","")
    else:
        return "OK"

privacychecking.registered = True


def categoricalparameter_inputerrorchecking(parameterName, parameterVal, domainVals):
    values = re.split(',',domainVals)
    if str(parameterVal) not in values:
        raise functions.OperatorError("ExaremeError", "Incorrect parameter value. " + parameterName + "'value should be one of the following: " + domainVals)
    return "OK"
categoricalparameter_inputerrorchecking.registered = True

def variabledistinctvalues_inputerrorchecking(nameofvariable, distinctvalues1, distinctvalues2): #ttestindependent
    values1 = re.split(',',distinctvalues1)
    values2 = re.split(',',distinctvalues2)
    values1.sort()
    values2.sort()

    if len(values1)!= len(values2):
        raise functions.OperatorError("ExaremeError", "Variable " + nameofvariable+ " should contain variable's distinct values")
    for i in range(len(values1)):
        if values1[i] != values2[i]:
            raise functions.OperatorError("ExaremeError", "Variable " + nameofvariable+ " should contain variable's distinct values")
    return "OK"

variabledistinctvalues_inputerrorchecking.registered = True

def variableshouldbebinary_inputerrorchecking(nameofvariable, numberdistictvalues): #ttestindependent
    if numberdistictvalues == 2 :
        return "OK"
    else:
        raise functions.OperatorError("ExaremeError", "Variable: " +nameofvariable+ " should be binary")
variableshouldbebinary_inputerrorchecking.registered = True


def holdoutvalidation_inputerrorchecking1(train_size, test_size):
    if train_size=='' and  test_size =='' :
        raise functions.OperatorError("ExaremeError", "Train_size and test_size should not be both empty")
    else:
        return "OK"

holdoutvalidation_inputerrorchecking1.registered = True


def holdoutvalidation_inputerrorchecking2(train_size, test_size):
    if train_size + test_size > 1:
        raise functions.OperatorError("ExaremeError", "Train_size + test_size should be less or equal to 1.0 ")
    else:
        return "OK"

holdoutvalidation_inputerrorchecking2.registered = True

# def maxnumberofiterations_errorhandling(maxnumberofiterations,no): # For most of the iterative algorithms
#     if maxnumberofiterations< no:
#         raise functions.OperatorError("ExaremeError", "The algorithm could not complete in the max number of iterations given. Please increase the iterations_max_number and try again.")
#     else:
#         return "OK"
#
# maxnumberofiterations_errorhandling.registered = True



def kmeans_inputerrorchecking(centersisempty,k):
    if (int(centersisempty) == 1 and k == '') or (int(centersisempty)== 0 and k != ''):
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
