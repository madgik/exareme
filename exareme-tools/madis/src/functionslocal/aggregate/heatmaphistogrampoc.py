class heatmaphistogrampoc:
    """
    .. function:: heatmaphistogram(colname1,val1,minval1,maxval1,buckets1[,colname2,val2,distinctvalues2]).
    Output: colname1 id1 and val is null in the case of 1D histogram

    Examples:

    >>> table1('''
    ... AGE	76.3	10	PTGENDER	Female	'["Female","Male"]'	55	91.4
    ... AGE	73.9	10	PTGENDER	Female	'["Female","Male"]'	55	91.4
    ... AGE	74.2	10	PTGENDER	Male	'["Female","Male"]'	55	91.4
    ... AGE	77.8	10	PTGENDER	Male	'["Female","Male"]'	55	91.4
    ... AGE	58.7	10	PTGENDER	Male	'["Female","Male"]'	55	91.4
    ... AGE	78.3	10	PTGENDER	Female	'["Female","Male"]'	55	91.4
    ... AGE	68.6	10	PTGENDER	Male	'["Female","Male"]'	55	91.4
    ... AGE	87.3	10	PTGENDER	Male	'["Female","Male"]'	55	91.4
    ... AGE	75.1	10	PTGENDER	Male	'["Female","Male"]'	55	91.4
    ... AGE	87.2	10	PTGENDER	Male	'["Female","Male"]'	55	91.4
    ... AGE	65.6	10	PTGENDER	Male	'["Female","Male"]'	55	91.4
    ... AGE	82.6	10	PTGENDER	Female	'["Female","Male"]'	55	91.4
    ... ''')

    >>> sql("select heatmaphistogram(a,b,g,h,c,d,e,f) from table1")
    colname0 | id0 | minvalue0 | maxvalue0 | colname1 | id1 |  val    | num
    ----------------------------------------------------------------
    AGE      | 7   | 80.48     | 84.12     | PTGENDER | 1   | Female | 1
    AGE      | 8   | 84.12     | 87.76     | PTGENDER | 0   | Male   | 2
    AGE      | 6   | 76.84     | 80.48     | PTGENDER | 0   | Male   | 1
    AGE      | 3   | 65.92     | 69.56     | PTGENDER | 0   | Male   | 1
    AGE      | 6   | 76.84     | 80.48     | PTGENDER | 1   | Female | 1
    AGE      | 2   | 62.28     | 65.92     | PTGENDER | 0   | Male   | 1
    AGE      | 5   | 73.2      | 76.84     | PTGENDER | 1   | Female | 2
    AGE      | 5   | 73.2      | 76.84     | PTGENDER | 0   | Male   | 2
    AGE      | 1   | 58.64     | 62.28     | PTGENDER | 0   | Male   | 1
    .... (+ zero bins)
    """
    registered = True  # Value to define db operator

    def __init__(self):

        self.n = 0
        self.heatmap = {}
        self.noofargs = 0;
        self.flag = False  # 1D or 2D histogram

    def step(self, *args):
        import json
        if self.n == 0:
            self.colnames = [None] * 2
            self.curvalues = [None] * 2
            self.index = [None] * 2
            # print args[5]
            if args[5] != None:
                self.flag = True

        try:
            # self.noofargs = len(args)

            if self.n == 0:
                self.colnames[0] = (args[0])
                self.minvalue = float(args[2])
                self.maxvalue = float(args[3])
                self.nobuckets = int(args[4])
                self.step = (self.maxvalue + 0.01 - self.minvalue) / self.nobuckets
                # print self.step, self.minvalue, self.maxvalue
                if self.flag == True:
                    self.colnames[1] = (args[5])
                    try:
                        self.distinctvalues = json.loads(args[7])
                    except ValueError:
                        self.distinctvalues = (args[7])
                    for i in xrange(self.nobuckets):
                        for j in xrange(len(self.distinctvalues)):
                            self.index[0] = i
                            self.index[1] = j
                            self.heatmap[tuple(self.index)] = 0
                if self.flag == False:
                    for i in xrange(self.nobuckets):
                        self.index[0] = i
                        self.index[1] = 0
                        self.heatmap[tuple(self.index)] = 0
                        # print self.heatmap

            self.curvalues[0] = float(args[1])
            if self.flag == True:
                self.curvalues[1] = (args[6])
            self.n += 1

        except (ValueError, TypeError):
            raise

        self.index[0] = int((self.curvalues[0] - self.minvalue) / self.step)
        if self.index[0] >= self.nobuckets:
            self.index[0] = self.nobuckets - 1
        if self.index[0] < 0:
            self.index[0] = 0

        if self.flag == True:
            self.index[1] = self.distinctvalues.index(self.curvalues[1])
        else:
            self.index[1] = 0

        if tuple(self.index) in self.heatmap.keys():
            self.heatmap[tuple(self.index)] += 1
        else:
            self.heatmap[tuple(self.index)] = 1

    def final(self):
        # print self.heatmap
        # print self.n
        if self.flag == True:
            yield ('colname0', 'id0', 'minvalue0', 'maxvalue0', 'colname1', 'id1', 'val', 'num')
        else:
            yield ('colname0', 'id0', 'minvalue0', 'maxvalue0', 'colname1', 'id1', 'val', 'num')

        if self.n > 0:
            for item in self.heatmap:
                result = []
                result.append(self.colnames[0])
                result.append(item[0])
                result.append(self.minvalue + item[0] * self.step)
                result.append(self.minvalue + (item[0] + 1) * self.step)
                if self.flag == True:
                    result.append(self.colnames[1])
                    result.append(item[1])
                    result.append(self.distinctvalues[item[1]])
                else:
                    result.append(None)
                    result.append(None)
                    result.append(None)
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
