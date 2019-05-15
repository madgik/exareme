import setpath
import functions
import json
import re
registered=True


class createderivedcolumns(functions.vtable.vtbase.VT): #uses + and : for multiplication
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'newSchema' not in dictargs: # einai to neo sxhma pou tha exei o pinakas.
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No newSchema ")
        newSchema = str(dictargs['newSchema'])
        newSchema = re.split(',',newSchema)


        newSchema1 =""
        for i in xrange(len(newSchema)):
            newSchema1 += newSchema[i]+","
        newSchema1=newSchema1[:-1]
        yield ([newSchema1],)



        cur = envars['db'].cursor()
        c=cur.execute(query)
        currentSchema1 = cur.getdescriptionsafe()
        currentSchema =[str(x[0]) for x in currentSchema1]


        for myrow in c:
            myrowresult =""
            for d in xrange(len(newSchema)):
                colval = 1.0
                if ":" in newSchema[d]:
                    elements = re.split(":",newSchema[d])
                else:
                    elements = [newSchema[d]]
                item=[]
                for e in xrange(len(elements)):
                    colname = elements[e]

                    myindex = currentSchema.index(str(colname))
                    colval = colval * float(myrow[myindex])
                myrowresult+=str(colval)+","
            # print myrow
            # print newSchema
            # print "result", myrowresult


            yield tuple([myrowresult[0:-1]],)




def Source():
    return functions.vtable.vtbase.VTGenerator(createderivedcolumns)


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



