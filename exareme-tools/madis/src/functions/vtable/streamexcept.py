from copy import deepcopy
import setpath
import vtbase
import functions
import heapq

### Classic stream iterator
registered=True

class StreamExcept(vtbase.VT):
    def BestIndex(self, constraints, orderbys):
        return (None, 0, None, True, 1000)

    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if len(largs) < 1:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Not defined union tables ")
        streams = str(largs[0]).split(",")
        if len(streams) < 2:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Union tables must be more than one ")

        cursors = []
        execs = []
        for stream in streams:
            cursors.append(envars['db'].cursor())
            execs.append(cursors[-1].execute("select * from " + str(stream) + ";"))

        comparedcursor = str(cursors[0].getdescriptionsafe())
        # for cursor in cursors:
        #     if str(cursor.getdescriptionsafe()) != comparedcursor:
        #         raise functions.OperatorError(__name__.rsplit('.')[-1],"Union tables with different schemas ")

        if 'cols' in dictargs:
            try:
                cols = int(dictargs['cols'])
            except ValueError:
                try:
                    cols = [y[0] for y in cursors[0].getdescriptionsafe()].index(dictargs['cols'])
                except ValueError:
                    raise functions.OperatorError(__name__.rsplit('.')[-1],"Column name does not exists ")
        else:
            cols=0

        if cols >= len(cursors[0].getdescriptionsafe()):
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Column position does not exists ")

        for x in range(0, len(streams)):
            if x is 0:
                execs[0] = ((v[cols], (0,) + v) for v in execs[0])
            elif x is 1:
                execs[1] = ((v[cols], (1,) + v) for v in execs[1])
            elif x is 2:
                execs[2] = ((v[cols], (2,) + v) for v in execs[2])
            elif x is 3:
                execs[3] = ((v[cols], (3,) + v) for v in execs[3])
            elif x is 4:
                execs[4] = ((v[cols], (4,) + v) for v in execs[4])

        try:
            yield list(cursors[0].getdescriptionsafe())
        except StopIteration:
            try:
                raise
            finally:
                try:
                    for cur in cursors:
                        cur.close()
                except:
                    pass

        currentgroup = None
        lists = [[]] * len(streams)
        for k, v in heapq.merge(*execs):
            if currentgroup is None or currentgroup != k:
                unionset = set().union(*lists[1:])
                for t in (set(lists[0]) - unionset):
                    yield t

                lists = [[]] * len(streams)

            lists[v[0]] = lists[v[0]] + [tuple(v[1:])]
            currentgroup = k

        unionset = set().union(*lists[1:])
        for t in list(set(lists[0]) - unionset):
            yield t

def Source():
    return vtbase.VTGenerator(StreamExcept)

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


