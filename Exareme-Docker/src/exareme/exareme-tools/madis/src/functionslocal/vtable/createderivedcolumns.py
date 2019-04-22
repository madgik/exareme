import setpath
import functions
import json
import re
registered=True


class createderivedcolumns(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'derivedcolumns' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No derivedcolumns ")
        derivedcolumns = str(dictargs['derivedcolumns'])
        derivedcolumns = re.split(',',derivedcolumns)
        derivedcolumns1 =[]

        for d in xrange(len(derivedcolumns)):
            if ":" in derivedcolumns[d]:
                element = re.split(":",derivedcolumns[d])
            else:
                element = [derivedcolumns[d]]

            item=[]
            for e in xrange(len(element)):
                # print element[e]
                if "(" in str(element[e]):
                    A=re.split("\(",element[e])
                    colname = A[0]
                    colval = A[1][:-1]
                    # print colname,colval
                else:
                    # print "aaa"
                    colname = element[0]
                    colval = None
                item.append([colname,colval])
            # print "item",item
            derivedcolumns1.append(item)

        # print "derivedcolumns1", derivedcolumns1
        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        schemaold =[]
        for i in xrange(len(schema)):
            schemaold.append(str(schema[i][0]))
        print "schemaold" ,schemaold


        print "newschema", derivedcolumns
        newschema =""
        for i in xrange(len(derivedcolumns)):
            newschema += derivedcolumns[i]+","
        newschema=newschema[:-1]
        yield ([newschema],)
        for myrow in c:
            # print myrow
            myrowresult = ""
            for d in xrange(len(derivedcolumns1)):
                newcolnameval = derivedcolumns1[d]
                # print "A",newcolnameval
                result = 1
                for i in xrange(len(newcolnameval)):
                     colname = newcolnameval[i][0]
                     colval = newcolnameval[i][1]
                     # print "bb",colname,colval
                     if str(colname) in schemaold:
                        myindex = schemaold.index(str(colname))
                        if colval is not None and str(myrow[myindex]) != str(colval):
                            result = 0
                        elif colval is None:
                            result = myrow[myindex]
                     elif str(colname)=='intercept':
                         result = 1
                myrowresult+= str(result) +","


            yield (myrowresult[:-1],)






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



