import math
# import numpy

class heatmap:
    """
    .. function:: heatmap(column)

    Calculating the bins of an histogram

    Example:

    >>> table1('''
    ... 1
    ... 2
    ... 3
    ... 4
    ... 5
    ... ''')

    >>> sql("select heatmap(a) from table1")

    """

    registered = True #Value to define db operator

    def __init__(self):
        self.n = 0
        self.mydata = []
        self.heatmap = {}


    def step(self, *args):
        if self.n == 0:
            self.nomydatacolumns = int(len(args)/3.0) # last column contains Scotts method data
            self.curvalues = [None]* self.nomydatacolumns
            self.minvalues = [0.0]* self.nomydatacolumns
            self.step = [0.0]* self.nomydatacolumns
            self.index = [0.0]* self.nomydatacolumns

        try:
            for i in xrange(self.nomydatacolumns):
                self.curvalues[i] = float(args[i])
            self.n += 1
        except (ValueError, TypeError):
            return

        if self.n == 1: # read Scotts method data
            for i in xrange(self.nomydatacolumns):
                self.minvalues[i] = float( args[self.nomydatacolumns + i*2])
                self.step[i] = float(args[self.nomydatacolumns + i*2 + 1 ])
            print [self.minvalues, self.step]


        for i in xrange(self.nomydatacolumns):
            self.index[i] = max(int(math.floor((self.curvalues[i] - self.minvalues[i]) / self.step[i])), 0)

        if tuple(self.index) in self.heatmap.keys():
            self.heatmap[tuple(self.index)] += 1
        else:
            self.heatmap[tuple(self.index)] = 1

    def final(self):
        import itertools
        yield tuple(itertools.chain.from_iterable((tuple(itertools.chain.from_iterable([("minvalue"+str(i), "maxvalue" + str(i)) for i in xrange(self.nomydatacolumns)])),['num'])))

        if self.n == 0:
            result = []
            for i in xrange(self.nomydatacolumns):
                result.append( "None")
                yield [result]
            return

        for item in self.heatmap:
            result = []
            for i in xrange(self.nomydatacolumns):
                result.append(self.minvalues[i] + item[i] * self.step[i])
                result.append(self.minvalues[i] + (item[i]+1) * self.step[i])
            result.append(self.heatmap[item])

            yield result
        return


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
