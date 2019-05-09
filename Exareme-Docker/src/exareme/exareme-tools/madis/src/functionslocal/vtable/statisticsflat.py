import setpath
import functions
import json
import re
import itertools
registered=True


class statisticsflat(functions.vtable.vtbase.VT):
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

        print schemaold
        result =[]
        for i in xrange(len(schemaold)):
            result.append(0)
        no = 0
        for myrow in c:
            no = no + 1
            elements = re.split(",",str(myrow[0]))
            elements = [float(x) for x in elements]
            for e in xrange(len(elements)):
                result[e]+=elements[e]

        yield (['colname'], ['S1'], ['N'])
        for i in xrange(len(schemaold)):
            yield schemaold[i],result[i],no


def Source():
    return functions.vtable.vtbase.VTGenerator(statisticsflat)


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