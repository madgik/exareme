import setpath
import functions
import json
import re

registered = True


class totabulardataresourceformat(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")
        query = dictargs['query']

        if 'title' not in dictargs:
            title = ''
        else:
            title = dictargs['title']

        if 'types' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No types argument ")
        types = dictargs['types']
        typeslist = re.split(",", types)
        typeslist = [x for x in typeslist if x]  # remove nulls elements of the list

        cur = envars['db'].cursor()
        c = cur.execute(query)
        schema = cur.getdescriptionsafe()

        if len(schema) == 0:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Empty table")
        else:
            myschema = []
            for i in xrange(len(schema)):
                myschema.append({
                    'name': schema[i][0],
                    'type': str(typeslist[i])
                })

        mydata = []
        for myrow in c:
            mydata.append(myrow)

        myresult = {
            "type": "application/vnd.dataresource+json",
            "data":
                {"name": str(title),
                 "profile": "tabular-data-resource",
                 "data": mydata,
                 "schema": {
                     "fields": myschema
                 }
                 }
        }
        myjsonresult = json.dumps(myresult)

        yield [('tabulardataresourceresult',)]
        yield (myjsonresult,)


def Source():
    return functions.vtable.vtbase.VTGenerator(totabulardataresourceformat)


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

        doctest.tesdoctest.tes
