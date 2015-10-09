"""

.. function:: toeav(query) -> Entity Attribute Value table

Transforms the query input results to an Entity Attribute Value model table.

:Returned table schema:
    ID, Attribute, Value

Examples::

    >>> table1('''
    ... James   10	2
    ... Mark    7	3
    ... Lila    74	1
    ... ''')
    >>> sql("toeav select * from table1")
    rid   | colname | val
    ---------------------
    James | b       | 10
    James | c       | 2
    Mark  | b       | 7
    Mark  | c       | 3
    Lila  | b       | 74
    Lila  | c       | 1
"""
import setpath
import vtbase
import functions
import gc

### Classic stream iterator
registered=True
       
class toEAV(vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        cur = envars['db'].cursor()
        c = cur.execute(query, parse=False)
        schema = []

        try:
            schema = [x[0] for x in cur.getdescriptionsafe()]
        except StopIteration:
            try:
                raise
            finally:
                try:
                    c.close()
                except:
                    pass

        yield [('rid',), ('colname',), ('val',)]
        lr = len(schema)
        while True:
            l = c.next()
            rid = l[0]
            for i in xrange(1, lr):
                yield (rid, schema[i], l[i])

def Source():
    return vtbase.VTGenerator(toEAV)

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


