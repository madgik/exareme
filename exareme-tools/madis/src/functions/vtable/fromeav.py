"""

.. function:: fromeav(query) -> Relational table

Transforms the query input results to a relational table from an Entity-Attribute-Value (EAV) model.

Examples::

    >>> table1('''
    ... 1    name    James
    ... 1    city    Chicago
    ... 1    job    Programmer
    ... 1    age    35
    ... 2    name    Mark
    ... 2    city    London
    ... 2    job    Pilot
    ... 2    age    43
    ... 5    name    Lila
    ... 5    city    'New York'
    ... 5    job    Teacher
    ... 5    age    29
    ... ''')

    >>> sql("fromeav select * from table1 where a = 1")
    rid | row_id | name  | city    | job        | age
    -------------------------------------------------
    0   | 1      | James | Chicago | Programmer | 35

    >>> sql("fromeav select * from table1 where a in (1,5)")
    rid | row_id | name  | city     | job        | age
    --------------------------------------------------
    0   | 1      | James | Chicago  | Programmer | 35
    1   | 5      | Lila  | New York | Teacher    | 29

    >>> sql("fromeav select * from table1")
    rid | row_id | name  | city     | job        | age
    --------------------------------------------------
    0   | 1      | James | Chicago  | Programmer | 35
    1   | 2      | Mark  | London   | Pilot      | 43
    2   | 5      | Lila  | New York | Teacher    | 29

    >>> sql("fromeav select * from table1 where b in ('city', 'job')")
    rid | row_id | city     | job
    ------------------------------------
    0   | 1      | Chicago  | Programmer
    1   | 2      | London   | Pilot
    2   | 5      | New York | Teacher
"""
import setpath
import vtbase
import functions
import gc

### Classic stream iterator
registered=True

class fromEAV(vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument")
        query = dictargs['query']

        cur = envars['db'].cursor()
        c = cur.execute(query, parse=False)
        schema = [('rid',), ('row_id',)]
        schema_order = {}

        try:
            l = prev_l = c.next()
        except:
            yield [("c1",)]
            return
        rid = 0
        record = [rid, l[0]]
        while l[0] == prev_l[0]:
            schema.append((str(l[1]),))
            schema_order[l[1]] = len(record)
            record.append(l[2])
            prev_l = l
            try:
                l = c.next()
            except:
                break
        yield schema
        yield record

        rid += 1
        record[0] = rid
        record[1] = l[0]
        record[2] = l[2]
        for i in xrange(3, len(schema)):
            l = c.next()
            record[schema_order[l[1]]] = l[2]
        yield record

        lr = len(schema) - 2
        while True:
            for i in xrange(lr):
                l = c.next()
                record[schema_order[l[1]]] = l[2]
            rid += 1
            record[0] = rid
            record[1] = l[0]
            yield record

def Source():
    return vtbase.VTGenerator(fromEAV)

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
