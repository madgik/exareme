import setpath
import functions
import math
import json
import re
from scipy import stats
registered=True



class ttest_independent(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        hypothesis ='different'
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

        if 'ylevels' in dictargs:
            ylevels = str(dictargs['ylevels']).split(',')


        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        if len(schema)==0:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Empty table")

        outputschema = [['colname'],['statistics'],['df']]
        init = True
        mydata = [myrow for myrow in c]

        for mycolname in colnames:
            print "a",mycolname
            for myrow in mydata:
                if str(myrow[0]) == mycolname:
                    if str(myrow[1]) == str(ylevels[0]):
                        colnameA = str(myrow[0])
                        meanA = float(myrow[2])
                        stdA = float(myrow[3])
                        NA = int(myrow[4])
                        sseA = float(myrow[5])
                    elif str(myrow[1]) == str(ylevels[1]):
                        colnameB = str(myrow[0])
                        meanB = float(myrow[2])
                        stdB = float(myrow[3])
                        NB = int(myrow[4])
                        sseB = float(myrow[5])

            df = NA + NB - 2
            std_error = (sseA+sseB) / df
            t_value =  (meanA - meanB) / math.sqrt(std_error/NA + std_error/NB)
            result = [colnameA,t_value,df]

            if init == True:
                outputschema.append(["Hypothesis"])
                outputschemaString = 'Hypothesis'
            if hypothesis == 'different': result.append(2.0 * (1.0-stats.t.cdf(abs(t_value), df)))
            elif hypothesis == 'twoGreater': result.append(stats.t.cdf(t_value, df))
            elif hypothesis == 'oneGreater': result.append(1.0- stats.t.cdf(t_value, df))

            if meandiff == 1:
                if init ==True:
                    outputschema.append(["Meandifference"])
                    outputschemaString+=',Meandifference'
                    outputschema.append(["SSEdifference"])
                    outputschemaString+=',SSEdifference'
                result.append(meanA-meanB)
                result.append(math.sqrt(std_error*(1.0/NA+1.0/NB)))

            if ci == 1:
                if hypothesis == 'different':
                    LowerConfidence, UpperConfidence = stats.t.interval(0.95, df, meanA - meanB, math.sqrt(std_error/NA + std_error/NB) )
                elif hypothesis == 'twoGreater':
                    _, UpperConfidence = stats.t.interval(0.90, df, meanA - meanB, math.sqrt(std_error/NA + std_error/NB) )
                    LowerConfidence = '-Inf'
                elif hypothesis == 'oneGreater':
                    UpperConfidence = 'Inf'
                    LowerConfidence, _ = stats.t.interval(0.90, df, meanA - meanB, math.sqrt(std_error/NA + std_error/NB) )
                if init == True:
                    outputschema.append(['Lower'])
                    outputschema.append(['Upper'])
                    outputschemaString += ',Lower,Upper'
                result.append(LowerConfidence)
                result.append(UpperConfidence)

            if effectsize == 1:
                cohen_value = (meanA - meanB)  / (math.sqrt((sseA+sseB)/(NA+NB-2.0)))
                if init ==True:
                    outputschema.append(['Cohens_d'])
                    outputschemaString+=',Cohens_d'
                result.append(cohen_value)

            if init == True:
                 outputschema.append(['outputschema'])
                 yield outputschema
            result.append(outputschemaString)
            init = False
            yield result



def Source():
    return functions.vtable.vtbase.VTGenerator(ttest_independent)


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