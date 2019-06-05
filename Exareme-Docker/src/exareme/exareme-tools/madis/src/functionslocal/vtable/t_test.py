import setpath
import functions
import math
import json
import re
from scipy import stats
registered=True



class t_test(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']
        testvalue = 0
        effectsize = 0
        ci = 0
        meandiff = 0

        if 'testvalue' in dictargs:
            testvalue = dictargs['testvalue']

        if 'effectsize'  in dictargs:
            effectsize = int(dictargs['effectsize'])

        if 'ci' in dictargs: #confidence interval
            ci = int(dictargs['ci'])

        if 'meandiff'  in dictargs:
            meandiff = int(dictargs['ci'])

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        if len(schema)==0:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Empty table")

        outputschema = [['colname'],['statistics'],['df'],['p']]
        outputschemaString='p'
        init = True
        for myrow in c:

            colname = str(myrow[0])
            mean = float(myrow[1])
            std = float(myrow[2])
            N = int(myrow[3])
            std_error = std / (math.sqrt(N))
            t_value =  (mean - float(testvalue)) / std_error
            df = N - 1
            p_value =  2 * stats.t.cdf(-abs(t_value), df)

            result = [colname,t_value,df,p_value]

            if effectsize == 1:
                cohen_value = (mean - float(testvalue))  / (math.sqrt((std*std)))
                if init ==True:
                    outputschema.append(['Cohens_d'])
                    outputschemaString+=',Cohens_d'
                result.append(cohen_value)

            if ci == 1:
                confidence = 0.95
                h = std_error * stats.t.ppf((1- confidence) / 2, df)
                LowerConfidence = min((mean - float(testvalue)) - h,(mean - float(testvalue)) + h)
                UpperConfidence = max((mean - float(testvalue)) - h,(mean - float(testvalue)) + h)
                if init ==True:
                    outputschema.append(['Lower'])
                    outputschema.append(['Upper'])
                    outputschemaString+=',Lower'
                    outputschemaString+=',Upper'
                result.append(LowerConfidence)
                result.append(UpperConfidence)

            if meandiff == 1:
                meandiff_value =  mean - float(testvalue)
                if init ==True:
                    outputschema.append(["Meandifference"])
                    outputschemaString+=',Meandifference'
                result.append(meandiff_value)

            if init == True:
                outputschema.append(["outputschema"])
                yield outputschema
            result.append(outputschemaString)
            yield result
            init = False



def Source():
    return functions.vtable.vtbase.VTGenerator(t_test)


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