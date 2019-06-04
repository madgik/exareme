import setpath
import functions
import math
import json
import re
from scipy import stats
registered=True



class t_test_unpaired(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']
        testvalue = 0
        effectsize = 0
        ci = 0
        meandiff = 0

        if 'colnames' in dictargs:
            colnames = str(dictargs['colnames']).split(',')

        if 'hypothesis' in dictargs:
            hypothesis = str(dictargs['hypothesis'])

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
        init = True
        mydata = [myrow for myrow in c]


        for mycolname in colnames:
            print "a",mycolname
            rowsAtHand = []
            for myrow in mydata:
                if str(myrow[0]) == mycolname:
                    rowsAtHand.append(myrow)


            colnameA = str(rowsAtHand[0][0])
            meanA = float(rowsAtHand[0][1])
            stdA = float(rowsAtHand[0][2])
            NA = int(rowsAtHand[0][3])
            sseA = float(rowsAtHand[0][4])

            colnameB = str(rowsAtHand[1][0])
            meanB = float(rowsAtHand[1][1])
            stdB = float(rowsAtHand[1][2])
            NB = int(rowsAtHand[1][3])
            sseB = float(rowsAtHand[1][4])

            df = NA + NB - 2
            std_error = (sseA+sseB) / df
            t_value =  (meanA - meanB) / math.sqrt(std_error/NA + std_error/NB)
            if  hypothesis == 'lessthan':
                 p_value =  stats.t.cdf(-abs(t_value), df)
            elif hypothesis == 'different':
                p_value =  2 * stats.t.cdf(-abs(t_value), df)
            elif hypothesis == 'greaterthan':
                p_value =  1- stats.t.cdf(-abs(t_value), df)
            result = [colnameA,t_value,df,p_value]

            if effectsize == 1:
                cohen_value = (meanA - meanB)  / (math.sqrt((stdA+stdB)/2.0))
                if init ==True: outputschema.append(['Cohen\'s d'])
                result.append(cohen_value)

            if ci == 1:
                confidence = 0.95
                h = std_error * stats.t.ppf((1- confidence) / 2, df)
                LowerConfidence = min((meanA - meanB) - h,(meanA - meanB) + h)
                UpperConfidence = max((meanA - meanB) - h,(meanA - meanB) + h)
                if init ==True: outputschema.append(['Lower'])
                if init ==True: outputschema.append(['Upper'])
                result.append(LowerConfidence)
                result.append(UpperConfidence)

            if meandiff == 1:
                meandiff_value =  meanA - meanB
                if init ==True: outputschema.append(["Mean difference"])
                result.append(meandiff_value)

            if init == True:
                yield outputschema
            yield result
            init = False



def Source():
    return functions.vtable.vtbase.VTGenerator(t_test_unpaired)


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
        doctest.tesdoctest.tes