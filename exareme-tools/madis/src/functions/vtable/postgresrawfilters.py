"""
.. function:: postgres(host, port, user, passwd, db, query:None)

Connects to an PostgreSQL DB and returns the results of query.

Examples:

    >>> sql("select * from (postgres h:127.0.0.1 port:5432 u:root p:rootpw db:testdb select 5 as num, 'test' as text);")
    num | text
    -----------
    5   | test

"""

import src.functions.setpath
import vtbase
import functions
import re
import json
from types import NoneType

registered = True
external_query = True

class PostgresRawFilters(vtbase.VT):


    def VTiter(self, *parsedArgs, **envars):
        # DBAPI.warn = lambda x,stacklevel:x

        typetrans = {
            16: 'INT',
            17: 'INT',
            19: 'TEXT',  # name type
            20: 'INT',
            21: 'INT',
            23: 'INT',
            25: 'TEXT',  # TEXT type
            26: 'INT',  # oid type
            142: 'TEXT',  # XML
            194: 'TEXT',  # "string representing an internal node tree"
            700: 'REAL',
            701: 'REAL',
            705: 'TEXT',
            829: 'TEXT',  # MACADDR type
            1000: 'INT',  # BOOL[]
            1003: 'TEXT',  # NAME[]
            1005: 'INT',  # INT2[]
            1007: 'INT',  # INT4[]
            1009: 'TEXT',  # TEXT[]
            1014: 'TEXT',  # CHAR[]
            1015: 'TEXT',  # VARCHAR[]
            1016: 'INT',  # INT8[]
            1021: 'REAL',  # FLOAT4[]
            1022: 'REAL',  # FLOAT8[]
            1042: 'TEXT',  # CHAR type
            1043: 'TEXT',  # VARCHAR type
            1082: 'TEXT',
            1083: 'TEXT',
            1114: 'TEXT',
            1184: 'TEXT',  # timestamp w/ tz
            1186: '',
            1231: '',  # NUMERIC[]
            1263: 'TEXT',  # cstring[]
            1700: '',
            2275: 'TEXT',  # cstring
        }

        largs, dictargs = self.full_parse(parsedArgs)
        # if 'query' not in dictargs:
        #   raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        try:
            largs = dictargs['query']
        except KeyError:
            largs = None


        try:
            myfilter =dictargs['filter']
            filterforquery = myfilter
            myfilter =  myfilter.replace('&','(')
            myfilter =  myfilter.replace('|','(')
            myfilter =  myfilter.replace(')','(')
            myfilter =  myfilter.split('(')

        except KeyError:
            myfilter = None
            filterforquery = myfilter



        # get site properties
        data = json.load(open("/root/mip-algorithms/properties.json"))
        #data = json.load(open("home/sofiakar/Desktop/sofia/Exareme-Docker/files/root/properties.json"))
        for d in data["local_engine_default"]["parameters"]:
            dictargs[d["name"]] = d["value"]

        query = data["local_engine_default"]["query"]
        host = str(dictargs.get('host', dictargs.get('h', '127.0.0.1')))
        port = int(dictargs.get('port', 5432))
        user = str(dictargs.get('username', dictargs.get('u', '')))
        passwd = str(dictargs.get('password', dictargs.get('p', '')))
        db = str(dictargs.get('db', ''))
        mytable= query.split("from")[1]

        import psycopg2
        try:
            conn = psycopg2.connect(user=user, host=host, port=port, database=db, password=passwd)

            cur = conn.cursor()
            larg =()
            if largs is None:
                query = query.replace('%', '%%')
                if myfilter is not None:
                    query = query + "where ("
            else:
                larg = tuple([largs.split(",")[i].strip() for i in xrange(len(largs.split(",")))])
                query = query.replace('%', '%%') + " where colname in %s "
                if myfilter is not None:
                    query = query + "and ("

            mylist = []
            mylist.append(larg)
            if myfilter is not None:
                conditions  =  ['<=','>=','<','=','>']
                n = 0
                if ";" not in filterforquery:
                    for myitem in myfilter:
                        split = False

                        for mycondition in conditions:
                            if mycondition in myitem and split == False:
                                colname = myitem.split(mycondition)[0]
                                val = myitem.split(mycondition)[1]
                                n += 1
                                newitem= "rid in (select rid from " + mytable + " where colname=%s and " #val" + mycondition + "%s) "  #,colname, val
                                val=val.encode("utf8")
                                if is_number(val):
                                    newitem+= "val::numeric" + mycondition + "%s) "
                                else:
                                    newitem+= "val" + mycondition + "%s) "
                                mylist.append(colname)
                                mylist.append(val)
                                split =True
                                filterforquery = filterforquery.replace(myitem,newitem)

                if n>0:
                    filterforquery = filterforquery.replace("&"," and ")
                    filterforquery = filterforquery.replace("|"," or ")
                    query += filterforquery +  " );"
                else:
                    query += ";"

            if  len(mylist) == 0:
                cur.execute(query)
            else:
                cur.execute(query,mylist)

            yield [(c[0], typetrans.get(c[1], '')) for c in cur.description]

            for i in cur:
                yield [unicode(c) if type(c) not in (long, int, float, str, unicode, NoneType, bool) else c for c in i]

            cur.close()
        except Exception, e:
            raise functions.OperatorError(__name__.rsplit('.')[-1], ' '.join(str(t) for t in e))


def Source():
    return vtbase.VTGenerator(PostgresRawFilters)


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

if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    import src.functions.setpath
    from functions import *

    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest

        doctest.testmod()