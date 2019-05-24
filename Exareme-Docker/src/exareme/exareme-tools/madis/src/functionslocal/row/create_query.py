import setpath
import functions
import random
# coding: utf-8
import re
import json



# def create_query(*args):
#
#     query = args[0]
#     for i,arg in enumerate(args[1:]):
#         print i,arg
#         if i%2:
#             query = re.subn("(\w*\(*)\?(\)*)", r"\1"+arg+r"\2", query, 1, flags=re.UNICODE)[0]
#             # print query
#             repl = arg.split(',')
#             # print repl
#             str = args[1:][i - 1].join([r"\1" + x + r"\2" for x in repl])
#             # print str
#             query = re.sub("(\w*\(*)" + arg + "(\)*)", str, query, flags=re.UNICODE)
#             # print query
#
#     yield tuple(('query',),)
#     yield (query,)
#
# create_query.registered = True



# def create_complex_query1(*args):
#
# #init,step,delimeter,final,columnsnames
#
#     # init = args[0]
#     step = args[0]
#     delimeter =  args[1]
#     # final = args[3]
#     jsoncolumnames = json.loads(args[4])
#
#     for x in jsoncolumnames:
#         for symbol,tovalue in x.iteritems():
#             print "a", symbol, tovalue
#             step = step.replace(symbol, tovalue)
#
#     query =  step + ' ' + delimeter
#
#     # query = init
#     # i = 1
#     # for col in mycolumns:
#     #     istep=step.replace("?", col)
#     #     if i < lencolumns:
#     #         query=query +' ' + istep + ' ' + delimeter
#     #     else:
#     #         query=query +' ' + istep
#     #     i = i+1
#     #
#     # query = query + ' ' + final
#     #
#     # yield tuple(('query',),)
#     # yield (query,)
#
#
# create_complex_query1.registered = True


def create_complex_query(*args):

#init,step,delimeter,final,columnsnames

    init = args[0]
    step = args[1]
    delimeter =  args[2]
    final = args[3]


    columnnames = args[4]
    if columnnames =='':
        columnnames = "null"

    mycolumns = columnnames.split(",");

    lencolumns =  len(mycolumns);
    # print init
    # print columnnames

    query = init
    i = 1
    for col in mycolumns:
        istep=step.replace("?", col)
        if i < lencolumns:
            query=query +' ' + istep + ' ' + delimeter;
        else:
            query=query +' ' + istep
        i = i+1;

    query = query + ' ' + final;

    yield tuple(('query',),)
    yield (query,)


create_complex_query.registered = True



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
        doctest.testmod()