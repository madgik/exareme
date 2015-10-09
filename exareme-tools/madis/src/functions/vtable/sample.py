"""

.. function:: sample(sample_size  query) -> samples rows from input

Returns a random sample_size set of rows.

:Returned table schema:
    Same as input query schema.

Options:

:size:

    Sample size

Examples::

    >>> table1('''
    ... James   10	2
    ... Mark    7	3
    ... Lila    74	1
    ... ''')
    >>> sql("sample '10' select * from table1")
    a     | b  | c
    --------------
    James | 10 | 2
    Mark  | 7  | 3
    Lila  | 74 | 1
    
    >>> sql("sample size:1 select * from table1") # doctest:+ELLIPSIS +NORMALIZE_WHITESPACE
    a     | b  | c
    ...

    >>> sql("sample size:0 select * from table1")

"""
import setpath
import vtbase
import functions

registered = True
       
class SampleVT(vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query=dictargs['query']

        samplesize = 1

        if len(largs) > 0:
            samplesize = int(largs[0])


        if 'size' in dictargs:
            samplesize = int(dictargs['size'])

        try:
            samplesize = int(samplesize)
        except ValueError:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Sample size should be integer")

        cur = envars['db'].cursor()
        c = cur.execute(query, parse = False)

        try:
            yield list(cur.getdescriptionsafe())
        except StopIteration:
            try:
                raise
            finally:
                try:
                    c.close()
                except:
                    pass

        from itertools import islice
        samplelist = list(islice(c, samplesize))
        index = len(samplelist)

        from random import randint
        for i, row in enumerate(c, index):
            r = randint(0, i)
            if r < samplesize:
                samplelist[r] = row

        for r in samplelist:
            yield r

def Source():
    return vtbase.VTGenerator(SampleVT)

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


