import functions
import heapq

import vtbase

### Classic stream iterator
registered = True


class MergeUnion(vtbase.VT):
    def BestIndex(self, constraints, orderbys):
        return (None, 0, None, True, 1000)

    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if len(largs) < 1:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Not defined union tables ")
        streams = str(largs[0]).split(",")
        if len(streams) < 2:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Union tables must be more than one ")

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
                    raise functions.OperatorError(__name__.rsplit('.')[-1], "Column name does not exists ")
        else:
            cols = 0

        if cols >= len(cursors[0].getdescriptionsafe()):
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Column position does not exists ")

        for x in range(0, len(streams)):
            execs[x] = ((v[cols], v) for v in execs[x])

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

        for _, v in heapq.merge(*execs):
            yield v


def Source():
    return vtbase.VTGenerator(MergeUnion)


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    from functions import *

    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest

        doctest.testmod()
