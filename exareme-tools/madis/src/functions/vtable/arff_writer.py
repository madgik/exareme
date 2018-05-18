"""

.. function:: rowidvt(query:None)

Returns the query input result adding rowid number of the result row.

:Returned table schema:
    Same as input query schema with addition of rowid column.

    - *rowid* int
        Input *query* result rowid.    

Examples::

    >>> table1('''
    ... James   10	2
    ... Mark    7	3
    ... Lila    74	1
    ... ''')
    >>> sql("rowidvt select * from table1")
    rowid | a     | b  | c
    ----------------------
    1     | James | 10 | 2
    2     | Mark  | 7  | 3
    3     | Lila  | 74 | 1
    >>> sql("rowidvt select * from table1 order by c")
    rowid | a     | b  | c
    ----------------------
    1     | Lila  | 74 | 1
    2     | James | 10 | 2
    3     | Mark  | 7  | 3

    Note the difference with rowid table column.

    >>> sql("select rowid,* from table1 order by c")
    rowid | a     | b  | c
    ----------------------
    3     | Lila  | 74 | 1
    1     | James | 10 | 2
    2     | Mark  | 7  | 3
"""
import setpath
import vtbase
import functions

### Classic stream iterator
registered = True


def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        pass

    try:
        import unicodedata
        unicodedata.numeric(s)
        return True
    except (TypeError, ValueError):
        pass

    return False


class arff_writer(vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        import arff
        largs, dictargs = self.full_parse(parsedArgs)

        self.nonames = True
        self.names = []
        self.types = []
        data = {}

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")
        query = dictargs['query']

        cur = envars['db'].cursor()
        c = cur.execute(query)

        first_row = c.next()
        schema = cur.getdescriptionsafe()

        updated_schema = []
        for i,val in enumerate(first_row):
            t = (schema[i][0],"STRING")
            if is_number(val):
                t = (schema[i][0],"NUMERIC")
            updated_schema.append(t)

        data[u'attributes'] = updated_schema

        raw = []
        raw.append(first_row)
        for row in c:
            raw.append(row)

        data[u'data'] = raw
        data[u'description'] = u''
        data[u'relation'] = u'hour-weka.filters.unsupervised.attribute.Remove-R1-2'


        f = open('input.arff','w')
        f.write(arff.dumps(data))

        yield(('result',),)
        yield (1,)





def Source():
    return vtbase.VTGenerator(arff_writer)


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


