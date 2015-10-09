"""
.. function:: whilevt([from:0,[to:10, step:1]], query)

Returns a range of integer numbers while a query's result is true.

:Returned table schema:
    - *value* int
        Number in range.

:from:
    Number to begin from. Default is 0
:to:
    Number to reach. Default is 10. The *to* number is not returned
:step:
    Step to augment the returned numbers. Default is 1

Examples::

    >>> sql("select * from range()")
    C1
    --
    0
    1
    2
    3
    4
    5
    6
    7
    8
    9
    
    >>> sql("select * from range('from:1','to:11')")
    C1
    --
    1
    2
    3
    4
    5
    6
    7
    8
    9
    10
    
    >>> sql("select * from range('from:2','to:15','step:3')")
    C1
    --
    2
    5
    8
    11
    14
    
    >>> sql("select * from range(1,10,2)")
    C1
    --
    1
    3
    5
    7
    9

    >>> sql("select * from range(5)")
    C1
    --
    1
    2
    3
    4
    5

"""

import setpath
import functions
import vtbase
registered = True
external_query = True

class WhileVT(vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)
        fromv = 0
        tov = None
        stepv = 1
        checkfirst = True
        query = 'select 1'
        con = None

        if 'from' in dictargs:
            fromv = int(dictargs['from'])
        if 'to' in dictargs:
            tov = int(dictargs['to'])
        if 'step' in dictargs:
            stepv = int(dictargs['step'])
        if 'checkfirst' in dictargs and dictargs['checkfirst'] in ('f', 'F', '0'):
            checkfirst = False
        if len(largs) >= 1:
            fromv = int(largs[0])
        if len(largs) >= 2:
            tov = int(largs[1])
        if len(largs) >= 3:
            stepv = int(largs[2])
        if len(largs) == 1:
            fromv = 1
            tov = int(largs[0])+1

        if functions.variables.execdb is None:
            con = functions.Connection('')
        else:
            con = functions.Connection(functions.variables.execdb)
        functions.register(con)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Needs a query")
        else:
            query = dictargs['query']
        yield [('C1', 'int')]

        if checkfirst:
            cur = con.cursor()
            res = list(cur.execute(query))
            cur.close()
            if len(res) == 0 or len(res[0]) == 0 or res[0][0] != 1:
                return

        yield (fromv,)

        while True:
            cur = con.cursor()
            res = list(cur.execute(query))
            cur.close()
            if len(res) == 0 or len(res[0]) == 0 or res[0][0] != 1:
                return
            fromv += 1
            if tov is not None and fromv >= tov:
                return
            yield (fromv, )

def Source():
    return vtbase.VTGenerator(WhileVT)

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