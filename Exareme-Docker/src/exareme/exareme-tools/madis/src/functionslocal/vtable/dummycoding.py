import setpath
import functions
import json
registered=True

def convert(data):
    if isinstance(data, basestring):
        return str(data)
    elif isinstance(data, collections.Mapping):
        return dict(map(convert, data.iteritems()))
    elif isinstance(data, collections.Iterable):
        return type(data)(map(convert, data))
    else:
        return data


class dummycoding(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'metadata' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No metadata ")
        metadata = json.loads(dictargs['metadata'])

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        no = 0
        for myrow in c:
            first_tuple = []
            schema1 = []
            for item in xrange(len(schema)):
                if schema[item][0] in metadata:
                    vals = metadata[schema[item][0]].split(',')
                    vals.sort()
                    for v in vals:
                        newv = str(schema[item][0]) + '(' + str(v) + ')'

                        schema1.append(newv)
                        if myrow[item] == v:
                            first_tuple.append(1)
                        else :
                            first_tuple.append(0)
                else:
                    # print 'no', schema[item][0]
                    newv = str(schema[item][0])
                    schema1.append(newv)
                    first_tuple.append(myrow[item])


            if no == 0:
                # print tuple((x,) for x in schema1)
                yield tuple((x,) for x in schema1)
            no =no+1

            # print str(first_tuple)
            yield tuple(first_tuple,)

def Source():
    return functions.vtable.vtbase.VTGenerator(dummycoding)


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