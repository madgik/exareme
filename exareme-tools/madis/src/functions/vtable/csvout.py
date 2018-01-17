import setpath
import vtbase
import functions
from lib.dsv import writer
import sys
import csv
### Classic stream iterator
registered=True
import StringIO

class csvout(vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        self.nonames=True
        self.names=[]
        self.types=[]

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query=dictargs['query']

        cur = envars['db'].cursor()
        c=cur.execute(query)

        yield [('c1',)]

        output = StringIO.StringIO()
        writer = csv.writer(output)
        for r in c:
            writer.writerow(r)
        ll = output.getvalue().splitlines()
        for row in ll:
            yield (row,)



def Source():
    return vtbase.VTGenerator(csvout)

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
        doctest.testmod()

