

import math
import random
from fractions import Fraction
import sys
from array import *
import json


class heatmaphistogram:

    registered = True #Value to define db operator

    def __init__(self):
        self.n = 0
        self.heatmap = {}
        self.noofargs = 0;

    def step(self, *args):

        if self.n == 0:
            # print args, len(args)
            self.colnames = [None]* 2
            self.curvalues = [None]* 2
            self.index = [None]* 2
            # self.noofargs = len(args)
            if args[5] == null:
                self.flag = false
            else:
                self.flag = true

        try:
            # self.noofargs = len(args)

            if self.n == 0:
                self.colnames[0] = (args[0])
                self.minvalue = float(args[2])
                self.maxvalue = float(args[3])
                self.nobuckets = int(args[4])
                self.step = (self.maxvalue -self.minvalue)/ self.nobuckets
                # print self.step
            self.curvalues[0] = float(args[1])

            if self.flag == true:

                if self.n == 0:
                    self.colnames[1] = (args[5])
                    self.distinctvalues = json.loads(args[7])
                self.curvalues[1] = (args[6])

            self.n += 1

            # if self.n < 3:
            #   print args
            #   print self.distinctvalues[0],self.distinctvalues[1]
            #   print self.heatmap

        except (ValueError, TypeError):
            raise

        self.index[0] = int((self.curvalues[0] - self.minvalue) / self.step)
        if self.flag == true:
            self.index[1] = self.distinctvalues.index(self.curvalues[1])
        else:
            self.index[1] = 0

        # print self.index
        if tuple(self.index) in self.heatmap.keys():
            self.heatmap[tuple(self.index)] += 1
        else:
            self.heatmap[tuple(self.index)] = 1

        # print self.heatmap




    def final(self):

        if self.flag == true:
            yield ('colname0','id0', 'minvalue0', 'maxvalue0',  'colname1', 'val', 'num')
        else:
            yield ('colname0','id0', 'minvalue0', 'maxvalue0','num')

        if self.n > 0:
            for item in self.heatmap:
                result = []
                result.append(self.colnames[0])
                result.append(item[0])
                result.append(self.minvalue + item[0] * self.step)
                result.append(self.minvalue + (item[0]+1) * self.step)
                if self.noofargs  > 5:
                    result.append(self.colnames[1])
                    result.append(self.distinctvalues[item[1]])
                result.append(self.heatmap[item])
                yield result


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
