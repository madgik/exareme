import setpath
import functions
import json
registered=True

#It returns the columns names of the table (either on a string or on a table)
#  getschema outputformat=1 select * from table; -->retrun string col1,col2,col3
#  getschema outputformat=0 select * from table; -->return table
#  getschema select * from table; --> return table

class getschema(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        outputformat = 0
        if 'outputformat' in dictargs:
            outputformat = int(dictargs['outputformat'])

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        schema1= []
        schema2= ''
        for item in xrange(len(schema)):
            schema1.append(str(schema[item][0]))
            schema2=schema2 + str(schema[item][0])+','
        schema2=schema2[:-1]
        # print schema1
        # print schema2

        # print schema1
        yield [('schema',)]
        if outputformat == 0:
            for x in schema1:
                yield tuple((x,))
        if outputformat == 1:
            yield (schema2,)


def Source():
    return functions.vtable.vtbase.VTGenerator(getschema)


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