"""
.. function:: execnselect(query:None, [path:None, variables])

This function expecting the query results to be target queries for execution (similar to exec).
Base on the parameters executes the target queries with the appropriate execution environment
and returns the results of the last target query.

*path*       : set up the current working directory for the target queries execution.
*variables*  : key type variables references to the current environment variable value
               and key:value type variables are set up in the target queries execution environment.

***Notice also that forwards the connecntions to the target environment.
"""

import os, sys, re, apsw
import functions

comment_line = re.compile(r'/\*.*?\*/(.*)$')
registered = True

def filterlinecomment(s):
    if re.match(r'\s*--', s, re.DOTALL|re.UNICODE):
        return ''
    else:
        return s

# TODO Fix bug, break queries same line
def breakquery(q):
    if len(q) > 1:
        raise functions.OperatorError(__name__.rsplit('.')[-1], "Ambiguous query column, result has more than one columns")
    st = ''
    for row in q[0].splitlines():
        strow = filterlinecomment(row)
        if strow == '':
            continue
        if st != '':
            st += '\n'+strow
        else:
            st += strow
        if apsw.complete(st):
            yield st
            st = ''

    if len(st) > 0 and not re.match(r'\s+$', st, re.DOTALL| re.UNICODE):
        if len(st) > 35:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Incomplete statement found : %s ... %s" % (st[:15], st[-15:]))
        else:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Incomplete statement found : %s" % (st,))

class ExecNSelectVT(functions.vtable.vtbase.VT):

    def VTiter(self, *parsedArgs, **envars):

        # default  parsing
        largs, dictargs = self.full_parse(parsedArgs)

        # get default connection
        connection = envars['db']
        if functions.variables.execdb is None:
            functions.variables.execdb = connection.filename
        tconnection = functions.Connection(functions.variables.execdb)
        functions.register(tconnection)

        # get query
        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument.")
        else :
            query = dictargs['query']

        # set up variables
        oldvars = functions.variables
        newvars = lambda x: x
        newpath = None
        path = os.getcwd()
        if 'path' in dictargs:
            newpath = os.path.abspath(dictargs['path'])
            del dictargs['path']
            os.chdir(newpath)
        newvars.execdb = functions.variables.execdb
        newvars.flowname = 'notset'

        for key in largs:
            if hasattr(functions.variables, key):
                setattr(newvars, key, getattr(functions.variables, key))
            else :
                raise functions.OperatorError(__name__.rsplit('.')[-1], "Variable %s doesn't exist" % (key,))
        for key, value in dictargs.items():
            setattr(newvars, key, value)
        functions.variables = newvars

        # execute target queries
        try:
            counter = -1
            cursor = connection.cursor()
            tcursor = tconnection.cursor()
            tqlast = ''
            databases = cursor.execute("PRAGMA database_list")
            for database in databases:
                dbname = database[1]
                dbfile = database[2]
                if dbname != "main" and dbname != "mem" and dbname != "temp" and dbfile != '':
                    list(tcursor.execute("attach database '{0}' as {1};".format(dbfile, dbname)))

            counter = 0
            results = cursor.execute(query, parse = False)
            for result in results:
                for tquery in breakquery(result):
                    # print "tquery", tquery
                    # print "tqlast", tqlast
                    if tqlast != '':
                        list(tcursor.execute(tqlast))
                        counter+=1
                    tqlast = tquery
            # print "tqlast", tqlast
            tresults = tcursor.execute(tqlast)
            counter+=1
            yield tcursor.getdescriptionsafe()
            for tresult in tresults:
                yield tresult

            tcursor.close()
            tconnection.close()
            cursor.close()
        except Exception as ex:
            import traceback
            traceback.print_exc()
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Error in query no. %d - %s" % (counter, str(ex)))

        # restore env
        functions.variables = oldvars
        if newpath:
            os.chdir(path)

def Source():
    return functions.vtable.vtbase.VTGenerator(ExecNSelectVT)

if not ('.' in __name__):
    """
    This is needed to be able to test the function,
    put it at the end of every new function you create.
    """
    import sys
    from functions import *
    testfunction()

    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest
        doctest.testmod()
