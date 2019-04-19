class heatmapfinal:
    registered = True  # Value to define db operator

    def __init__(self):
        self.n = 0
        #  self.mydata = []
        self.heatmap = {}

    def step(self, *args):

        if self.n == 0:
            # print args, len(args)

            self.nomydatacolumns = int(len(args) / 4.0)
            self.colnames = [None] * self.nomydatacolumns
            self.curvalues = [None] * self.nomydatacolumns
            self.minvalues = [0.0] * self.nomydatacolumns
            self.step = [0.0] * self.nomydatacolumns
            self.index = [0.0] * self.nomydatacolumns
            # print self.nomydatacolumns, self.colnames ,self.curvalues , self.minvalues ,self.step ,self.index

        try:
            for i in xrange(self.nomydatacolumns):
                self.curvalues[i] = float(args[i * 4 + 1])
                if self.n == 0:
                    self.minvalues[i] = float(args[i * 4 + 2])
                    self.colnames[i] = (args[i * 4])
                    self.step[i] = float(args[i * 4 + 3])
            self.n += 1

        except (ValueError, TypeError):
            raise

        # print  self.step #["A", self.curvalues, self.colnames, self.minvalues, self.step]

        for i in xrange(self.nomydatacolumns):
            # print self.step[i]
            self.index[i] = max(int(round((self.curvalues[i] - self.minvalues[i]) / self.step[i])), 0)

        if tuple(self.index) in self.heatmap.keys():
            self.heatmap[tuple(self.index)] += 1
        else:
            self.heatmap[tuple(self.index)] = 1

    def final(self):
        import itertools
        yield tuple(itertools.chain.from_iterable((tuple(itertools.chain.from_iterable(
            [("colname" + str(i), "id" + str(i), "minvalue" + str(i), "maxvalue" + str(i)) for i in
             xrange(self.nomydatacolumns)])), ['num'])))
        # yield ('colname0', 'minvalue0', 'maxvalue1', 'colname1', 'minvalue1', 'maxvalue1')

        if self.n == 0:
            result = []
            for i in xrange(self.nomydatacolumns):
                result.append("None")
                yield result
        else:
            for item in self.heatmap:
                result = []
                for i in xrange(self.nomydatacolumns):
                    result.append(self.colnames[i])
                    result.append(item[i])
                    result.append(self.minvalues[i] + item[i] * self.step[i])
                    result.append(self.minvalues[i] + (item[i] + 1) * self.step[i])
                result.append(self.heatmap[item])
                yield result


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
