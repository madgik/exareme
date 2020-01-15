import setpath
import functions
import math
import json
import re
from scipy import stats

registered = True


class ttest_onesample(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")
        query = dictargs['query']
        testvalue = 0
        effectsize = 0
        ci = 0
        meandiff = 0
        sediff = 0
        hypothesis = 'different'

        if 'testvalue' in dictargs:
            testvalue = dictargs['testvalue']

        if 'effectsize' in dictargs:
            effectsize = int(dictargs['effectsize'])

        if 'ci' in dictargs:  # confidence interval
            ci = int(dictargs['ci'])

        if 'meandiff' in dictargs:
            meandiff = int(dictargs['meandiff'])

        if 'sediff' in dictargs:
            sediff = int(dictargs['sediff'])

        if 'hypothesis' in dictargs:
            hypothesis = str(dictargs['hypothesis'])

            print hypothesis

        cur = envars['db'].cursor()
        c = cur.execute(query)
        schema = cur.getdescriptionsafe()

        if len(schema) == 0:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Empty table")

        outputschema = [['colname'], ['t-value'], ['df']]
        init = True
        for myrow in c:
            colname = str(myrow[0])
            mean = float(myrow[1])
            std = float(myrow[2])
            N = int(myrow[3])

            std_error = std / (math.sqrt(N))
            t_value = (mean - float(testvalue)) / std_error
            df = N - 1
            result = [colname, t_value, df]

            if init == True:
                outputschema.append(["p-value"])
                outputschemaString = 'p-value'
            if hypothesis == 'different': result.append(2.0 * (1.0-stats.t.cdf(abs(t_value), df)))
            elif hypothesis == 'lessthan': result.append(stats.t.cdf(t_value, df))
            elif hypothesis == 'greaterthan': result.append(1.0- stats.t.cdf(t_value, df))

            if meandiff == 1:
                meandiff_value = mean - float(testvalue)
                if init == True:
                    outputschema.append(["Meandifference"])
                    outputschemaString += ',Meandifference'
                result.append(meandiff_value)

            if sediff == 1:
                if init == True:
                    outputschema.append(["SEdifference"])
                    outputschemaString+=',SEdifference'
                result.append(std_error)

            if ci == 1:
                if hypothesis == 'different':
                    LowerConfidence, UpperConfidence = stats.t.interval(0.95, df, mean - float(testvalue), std_error)
                elif hypothesis == 'lessthan':
                    _, UpperConfidence = stats.t.interval(0.90, df, mean - float(testvalue), std_error)
                    LowerConfidence = '-Inf'
                elif hypothesis == 'greaterthan':
                    UpperConfidence = 'Inf'
                    LowerConfidence, _ = stats.t.interval(0.90, df, mean - float(testvalue), std_error)
                if init == True:
                    outputschema.append(['Lower'])
                    outputschema.append(['Upper'])
                    outputschemaString += ',Lower,Upper'
                result.append(LowerConfidence)
                result.append(UpperConfidence)

            if effectsize == 1:
                cohen_value = (mean - float(testvalue)) / (math.sqrt((std * std)))
                if init == True:
                    outputschema.append(['Cohens_d'])
                    outputschemaString += ',Cohens_d'
                result.append(cohen_value)

            if init == True:
                outputschema.append(["outputschema"])
                print outputschema
                yield outputschema
            result.append(outputschemaString)
            yield result
            init = False


def Source():
    return functions.vtable.vtbase.VTGenerator(ttest_onesample)


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

        doctest.tes
