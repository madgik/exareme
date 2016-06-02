"""
.. function:: madtitus(query:none [, path:None, pfadoc:None, options])

Using Titus (https://github.com/opendatagroup/hadrian),
the implementation of the PFA (http://dmg.org/pfa/index.html)
to execute the provided scoring engine document using the query results.

:Returned table schema:
    the output type of the PFA document

Options


Examples::

    >>> sql("select filetext('/tmp/demoPFA.json')") # doctest:+ELLIPSIS
    filetext('/tmp/demoPFA.json')
    --------------------------------------------------------------------------------
    { "action" : { "+" : ["input", 10] }, "input" : "double", "output" : "double" }

    >>> sql("select * from (madtitus 'path:/tmp/demoPFA.json' select * from range(10))")    # doctest:+ELLIPSIS
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

    >>> sql("select * from (madtitus 'pfa:eyAiYWN0aW9uIiA6IHsgIisiIDogWyJpbnB1dCIsIDEwXSB9LCAiaW5wdXQiIDogImRvdWJsZSIsICJvdXRwdXQiIDogImRvdWJsZSIgfQ' select * from range(10))")    # doctest:+ELLIPSIS
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
import functions
from functions.vtable import vtbase
import base64
import json
import titus.genpy

registered=True

def decode_base64(data):
    """Decode base64, padding being optional.

    :param data: Base64 data as an ASCII byte string
    :returns: The decoded byte string.

    """
    missing_padding = 4 - len(data) % 4
    if missing_padding:
        data += b'='* missing_padding
    return base64.decodestring(data)

class madtitus(vtbase.VT):

    def VTiter(self, *parsedArgs, **envars):

        largs, dictargs = self.full_parse(parsedArgs)
        query = ""
        pfa = ""
        path = ""

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")
        else:
            query = dictargs['query']

        if 'path' not in dictargs:
            if 'pfa' not in dictargs:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")
            else:
                pfa = json.loads(decode_base64(dictargs['pfa']))
        else:
            path = dictargs['path']

        try:

            if path != "" and '.json' in path:
                engine, = titus.genpy.PFAEngine.fromJson(open(path))
            elif path != "" and '.yml' in path:
                engine, = titus.genpy.PFAEngine.fromJson(open(path))
            elif path == "" and pfa != "":
                engine, = titus.genpy.PFAEngine.fromJson(pfa)
            else:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "No valid path extension.")

            query = dictargs['query']
            c = envars['db'].cursor()
            cexec = c.execute(query)
            yield c.getdescriptionsafe()
            for r in cexec:
                yield [engine.action(r[0])]

        except Exception, e:
            raise functions.OperatorError(__name__.rsplit('.')[-1], e)

def Source():
    return vtbase.VTGenerator(madtitus)

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
