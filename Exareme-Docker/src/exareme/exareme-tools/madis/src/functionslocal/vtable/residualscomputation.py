import setpath
import functions
import json
import re
import itertools
registered=True


class residualscomputation(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'coefficients' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No coefficients ")
        coef = json.loads(dictargs['coefficients'])
        coefficients = dict()

        for i in xrange(len(coef)):
            # coefficients
            for key in coef[i]:
                if str(key)=='estimate':
                    val = float(coef[i][key])
                if str(key)=='attr1':
                    newkey = str(coef[i][key])
            coefficients[newkey]=val

        if 'y' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No y argument ")
        y = dictargs['y']

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schemaold = cur.getdescriptionsafe()
        schemaold= schemaold[0][0]
        schemaold = re.split(',',str(schemaold))

        yindex = schemaold.index(str(y))
        no = 0
        yield (['val'], )
        for myrow in c:
            no = no + 1
            rowelements = re.split(",",str(myrow[0]))
            rowelements = [float(x) for x in rowelements]
            # print "Myrow",rowelements
            residual= rowelements[yindex] #-grandmean
            # print "y",residual
            for key in coefficients:

                ind = schemaold.index(str(key))
                # print "-",rowelements[ind],coefficients[key]
                residual -= rowelements[ind]*coefficients[key]
            yield (residual,)







def Source():
    return functions.vtable.vtbase.VTGenerator(residualscomputation)


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
