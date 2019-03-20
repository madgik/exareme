import setpath
import functions
import json
registered=True

# It receives a tree (no,colname,val,nextnode) like this:
# [1|1[2|tear-prod-rate[3|normal[4|2
# [1|1[2|tear-prod-rate[3|reduced[4|none
# [1|2[2|astigmatism[3|no[4|3
# [1|2[2|astigmatism[3|yes[4|?
# [1|3[2|age[3|pre-presbyopic[4|soft
# [1|3[2|age[3|presbyopic[4|?
# [1|3[2|age[3|young[4|soft


#it returns the path that contains '?'
# [1|2[2|astigmatism[3|yes[4|?
# [1|1[2|tear-prod-rate[3|normal[4|2

def newTreeRecord(mytable,no):
    for myrow in mytable:
        if str(myrow[3]) == no:
            return [str(myrow[0]),myrow]

class pathtree(functions.vtable.vtbase.VT):

    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'value' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No node value ")
        nodeValue = dictargs['value']

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()
        schemainput = []

        for i in xrange(len(schema)):
             schemainput.append(str(schema[i][0]))
        yield tuple((x,) for x in schemainput)

        mytable=[]
        mytable = list(c)
        print mytable

        if nodeValue in [x[3] for x in mytable]:
            nextnode = nodeValue
            while nextnode!='1':
                nextnode,row = newTreeRecord(mytable,nextnode)
                yield row





def Source():
    return functions.vtable.vtbase.VTGenerator(pathtree)



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
        doctest.tes