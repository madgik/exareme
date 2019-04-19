import setpath
import functions
import json
registered=True

# Filters the input table using the filters argument
# For example
# lala table :
# [1|1[2|tear-prod-rate[3|normal[4|?
# --- [0|Column names ---
# [1|no [2|colname [3|val [4|nextnode
# var 'filters' from select tabletojson(colname,val, "colname,val") from lala;
# or
# var 'filters' '[{"colname": "tear-prod-rate", "val": "normal"}]'
# filtertable filters:%{filters} select * from mytable;


class filtertable(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'filters' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No filters ")
        # print dictargs['filters']
        filters = json.loads(dictargs['filters'])

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        schemaold = []
        schemanew = []
        for i in xrange(len(schema)):
            schemaold.append(str(schema[i][0]))
            schemanew.append(str(schema[i][0]))
        for filter in filters:
            schemanew.remove(str(filter['colname']))
        # print schemaold
        # print schemanew

        yield tuple((x,) for x in schemanew)

        for myrow in c:
            passfilter = 1
            for filter in filters:
                colid = schemaold.index(str(filter['colname']))
                if myrow[colid]!= str(filter['val']):
                    passfilter = 0
            if passfilter ==1:
                newrow=[]
                for i in xrange(len(schemanew)):
                    # print schemanew[i],i
                    if schemanew[i] in schemaold:
                        colid2 = schemaold.index(schemanew[i])
                        newrow.append(myrow[colid2])
                yield  tuple(newrow,)



  # no = 0
  #       for myrow in c:
  #           first_tuple = []
  #           schema1 = []
  #           for item in xrange(len(schema)):
  #               if schema[item][0] in metadata:
  #                   vals = metadata[schema[item][0]].split(',')
  #                   vals.sort()
  #                   for v in vals:
  #                       newv = str(schema[item][0]) + '(' + str(v) + ')'
  #
  #                       schema1.append(newv)
  #                       if myrow[item] == v:
  #                           first_tuple.append(1)
  #                       else :
  #                           first_tuple.append(0)
  #               else:
  #                   # print 'no', schema[item][0]
  #                   newv = str(schema[item][0])
  #                   schema1.append(newv)
  #                   first_tuple.append(myrow[item])
  #
  #
  #           if no == 0:
  #               # print tuple((x,) for x in schema1)
  #               yield tuple((x,) for x in schema1)
  #           no =no+1
  #
  #           # print str(first_tuple)
  #           yield tuple(first_tuple,)

def Source():
    return functions.vtable.vtbase.VTGenerator(filtertable)


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