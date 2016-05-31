"""
.. function:: demopfavt(query)

A minimal example of using pfa scoring engine.
Takes as input a single number x and returns x + 10.

Examples:

    >>> sql("select * from (demopfavt select * from range(10))")    # doctest:+ELLIPSIS
    C1
    ----
    11.0
    12.0
    13.0
    14.0
    15.0
    16.0
    17.0
    18.0
    19.0
    20.0

"""
from functions.vtable import vtbase

import json
import titus.genpy

registered=True

class demopfavt(vtbase.VT):

    def VTiter(self, *parsedArgs, **envars):

        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")

        try:
            engine, = titus.genpy.PFAEngine.fromJson('''
                                {"input": "double",
                                 "output": "double",
                                 "action": {"+": ["input", 10]}}
                                 ''')

            query = dictargs['query']
            c = envars['db'].cursor()
            cexec = c.execute(query)

            yield [('C1', 'float')]
            for r in cexec:
                yield [engine.action(r[0])]

        except Exception, e:
            raise functions.OperatorError(__name__.rsplit('.')[-1], e)

def Source():
    return vtbase.VTGenerator(demopfavt)

if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    import functions.vtable.setpath
    from functions import *
    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest
        doctest.testmod()
