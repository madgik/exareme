import setpath
import functions
import json
import scipy
import re

registered=True

#It returns the columns names of the table (either on a string or on a table)
#  getschema outputformat=1 select * from table; -->retrun string col1,col2,col3
#  getschema outputformat=0 select * from table; -->return table
#  getschema select * from table; --> return table

class histogram(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'metadata' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No metadata ")
        metadata = json.loads(dictargs['metadata'])

        if 'bins' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No bins ")
        bins = dictargs['bins']

        for key in metadata[0]:
            if str(key) == 'code': code = str(metadata[0][key])
            if str(key) == 'categorical': categorical = int(metadata[0][key])
        for key in metadata[0]:
            if categorical == 1:
                if str(key) == 'enumerations':
                    enumerations = re.split(',',str(metadata[0][key]))
                    enumerations = [x for x in enumerations if x] # remove nulls elements of the list
            elif categorical == 0:
                if str(key) == 'minval': minval = float(metadata[0][key])
                if str(key) == 'maxval': maxval = float(metadata[0][key])
                if str(key) == 'N': N = int(metadata[0][key])

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        yield (['id'], ['val'], ['minval'], ['maxval'], ['num'],)
        if categorical == 1:
            Hist = {en: 0 for en in enumerations}
            for myrow in c:
                Hist[str(myrow[0])] = Hist[str(myrow[0])] + 1
            id = 0
            for key in Hist:
                yield (id, key, None, None, Hist[key])
                id +=1
        elif categorical == 0:
            data =[x for x in c]
            hist, bin_edges = scipy.histogram(data, int(bins), (minval,maxval))
            print len(hist), len(bin_edges)
            for i in xrange(len(hist)):
                 yield (i,None, bin_edges[i],bin_edges[i+1] ,hist[i])




def Source():
    return functions.vtable.vtbase.VTGenerator(histogram)


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