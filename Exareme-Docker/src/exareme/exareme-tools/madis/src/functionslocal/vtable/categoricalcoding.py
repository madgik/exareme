# pip install category_encoders
# pip install python-dateutil==2.

import setpath
import functions
import json
import re
registered=True

# Onehot (or dummy) coding for categorical features, produces one feature per category, each binary. http://contrib.scikit-learn.org/categorical-encoding/onehot.html
# https://www.statsmodels.org/dev/contrasts.html

class categoricalcoding(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'encodingcategory' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No encodingcategory argument ")
        encodingcategory = dictargs['encodingcategory']

        if 'metadata' not in dictargs:
             raise functions.OperatorError(__name__.rsplit('.')[-1],"No metadata argument ")
        metadata1 = json.loads(dictargs['metadata'])
        metadata =dict()
        for key in metadata1:
            metadata[str(key)]=str(metadata1[key])
        # print metadata

        cur = envars['db'].cursor()
        c=cur.execute(query)
        currentSchema = cur.getdescriptionsafe()

        noRow = 0
        for myrow in c:
            newrow = []
            newSchema = []
            for i in xrange(len(myrow)):
                colname = str(currentSchema[i][0])
                colval = myrow[i]

                if colname in metadata.keys():
                    print colname,colval
                    newcolvals = metadata[colname].split(',')
                    nvals =len(newcolvals)
                    print nvals
                    # newcolvals.sort()
                    for v in xrange(0,len(newcolvals)):
                        newcolval =newcolvals[v]
                        newSchema.append(str(colname) + '(' + str(newcolval) + ')')
                        if encodingcategory =='dummycoding':
                            if str(colval) == str(newcolval):
                               newrow.append(1)
                            else:
                                newrow.append(0)
                        elif encodingcategory =='sumscoding':
                            if str(colval) == str(newcolval):
                               newrow.append(1)
                            elif str(colval) == str(newcolvals[0]): # Reference point
                               newrow.append(-1)
                            else:
                                newrow.append(0)
                        elif encodingcategory =='simplecoding':
                            if str(colval) == str(newcolval):
                               newrow.append(1.0 - 1.0/nvals)
                            else:
                                newrow.append(0.0 - 1.0/nvals)


                else: # gia mh categorical columns:
                    newSchema.append(str(colname))
                    newrow.append(colval)
            print "oldrow",myrow
            print "newrow",newrow

            if noRow == 0:
                yield tuple((x,) for x in newSchema)
            noRow = noRow+1
            yield tuple(newrow,)



def Source():
    return functions.vtable.vtbase.VTGenerator(categoricalcoding)


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



