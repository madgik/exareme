import setpath
import functions
import json
import re
import itertools
registered=True


class gramianflat(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        # print "derivedcolumns1", derivedcolumns1
        cur = envars['db'].cursor()
        c=cur.execute(query)
        schemaold = cur.getdescriptionsafe()
        schemaold= schemaold[0][0]
        schemaold = re.split(',',str(schemaold))

        gramianresult = dict()
        schemanew=[list(x) for x in list(itertools.product(schemaold,schemaold))]

        for i in xrange(len(schemanew)):
            gramianresult[str(schemanew[i][0]), str(schemanew[i][1])] = 0.0
        no = 0
        for myrow in c:
            no = no + 1
            elements = re.split(",",str(myrow[0]))
            elements = [float(x) for x in elements]
            a =[list(x) for x in list(itertools.product(elements,elements))]

            for i in xrange(len(schemanew)):
                gramianresult[str(schemanew[i][0]), str(schemanew[i][1])] += a[i][0]*a[i][1]
        # print gramianresult
        yield (['attr1'], ['attr2'], ['val'], ['reccount'])
        for key in gramianresult:
            yield key[0],key[1],gramianresult[key],no



def Source():
    return functions.vtable.vtbase.VTGenerator(gramianflat)


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
