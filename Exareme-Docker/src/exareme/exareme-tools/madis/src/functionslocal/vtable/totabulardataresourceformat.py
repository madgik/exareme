import setpath
import functions
import json
import re
registered=True



class totabulardataresourceformat(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'title' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No title argument ")
        title = dictargs['title']

        if 'types' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No types argument ")
        types = dictargs['types']
        typeslist = re.split(",",types)
        typeslist = [x for x in typeslist if x] # remove nulls elements of the list

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        if len(schema)==0:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Empty table")
        # print schema

        myresult= "{\"data\": [{ \"name\": \""+str(title)+"\",\"profile\": \"tabular-data-resource\",\"data\": [ ["

        for i in xrange(len(schema)):
            myresult += "\"" + str(schema[i][0]) +"\","
        myresult = myresult[:-1] +" ],"

        for myrow in c:
            myresult += "["
            for i in xrange(len(myrow)):
                # print str(typeslist[i])
                if str(typeslist[i]) != 'text':
                    # if myrow[i] is not None: --LR MODIFICATION
                    myresult +=str(myrow[i])+','
                else:
                    # if myrow[i] is not None: --LR MODIFICATION
                    myresult +="\"" + str(myrow[i])+"\""+','
            myresult = myresult[:-1]+ "],"
        myresult = myresult[:-1]+ "], \"schema\":  { \"fields\": ["

        for i in xrange(len(schema)):
             myresult += "{\"name\": \"" + str(schema[i][0]) +"\",\"type\": \""+str(typeslist[i])+"\"},"
        myresult =myresult[:-1] +" ]}}],\"type\":\"application/vnd.dataresource+json\"}"

        # print "myresult", myresult
        yield [('tabulardataresourceresult',)]
        yield (myresult,)

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