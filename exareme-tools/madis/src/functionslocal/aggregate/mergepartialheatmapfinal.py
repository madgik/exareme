import math
import json
from array import *

class mergepartialheatmapsfinal:

    registered = True #Value to define db operator

    def __init__(self):

        self.heatmap2D = []
        self.noofvariables = 5
        self.colnames = [None] * 2
        self.numberofbuckets = [None] * 2
        self.globalminvalues  = [None] * 2
        self.globalmaxvalues  = [None] * 2
        self.step = [None] *  2

        self.noofrows = 0


    def step(self, *args):
        try:
            if self.noofrows == 0:
                attrscotts = json.loads(args[7])
                for i in xrange(0,2):
                    self.colnames[i] = attrscotts[i][0]
                    self.globalminvalues[i] = attrscotts[i][1]
                    self.globalmaxvalues[i] = attrscotts[i][2]
                    self.step[i] = attrscotts[i][3]

                self.numberofbuckets[0] = int(math.ceil((self.globalmaxvalues[0]-self.globalminvalues[0]) / self.step[0]))+1
                self.numberofbuckets[1] = int(math.ceil((self.globalmaxvalues[1]-self.globalminvalues[1]) / self.step[1]))+1
                self.heatmap2D = [array('d', [0 for i in xrange(0, (self.numberofbuckets[1]))]) for x in xrange(0,self.numberofbuckets[0])]

                # print self.colnames, self.globalminvalues,self.globalmaxvalues,self.step, self.numberofbuckets

            #ta buckets sto global heatmap einai 0,1,2,3,4,5 dhadh akeraioi arithmoi
            if self.colnames[0] == args[0]:
                xmin = (args[1]-self.globalminvalues[0])/self.step[0]
                xmax = (args[2]-self.globalminvalues[0])/self.step[0]
                ymin = (args[4]-self.globalminvalues[1])/self.step[1]
                ymax = (args[5]-self.globalminvalues[1])/self.step[1]
            elif self.colnames[0] == args[3]:
                xmin = (args[4]-self.globalminvalues[0])/self.step[0]
                xmax = (args[5]-self.globalminvalues[0])/self.step[0]
                ymin = (args[1]-self.globalminvalues[1])/self.step[1]
                ymax = (args[2]-self.globalminvalues[1])/self.step[1]
            else:
                raise

            # print  "xmin xmax", [xmin,xmax],[ymin,ymax], math.ceil(xmax),math.ceil(ymax)
            sump = 0
            for i in xrange(int(math.floor(xmin)), int(math.ceil(xmax))+1):
                if xmin <= i and xmax <= i:
                    pi = 0
                elif xmin <= i and xmax <= i+1:
                    pi = xmax - i
                elif xmin <= i and xmax >= i+1:
                    pi  = 1
                elif xmin >= i and xmax <= i+1:
                    pi = xmax-xmin
                elif  xmin >= i and xmax >= i+1:
                    pi = i + 1 -xmin
                elif xmin > i +1 :
                    pi=0

                for j in xrange(int(math.floor(ymin)), int(math.ceil(ymax))+1):
                    if ymin <= j  and ymax <= j:
                        pj = 0
                    elif ymin <= j and ymax <= j+1:
                        pj = ymax - j
                    elif ymin <= j and ymax >= j+1:
                        pj  = 1
                    elif ymin >= j and ymax <= j+1:
                        pj = ymax-ymin
                    elif  ymin >= j and ymax >= j+1:
                        pj = j + 1 -ymin
                    elif ymin > j+1 :
                        pj = 0

                    sump +=  pi*pj
                    # print i,j,pi,pj,a
                    self.heatmap2D[i][j] += pi*pj*float(args[6])

            # print "sump", sump
            self.noofrows += 1

        except (ValueError, TypeError):
            raise

    def final(self):

        print "2D Heatmap"
        for i in xrange(0,self.numberofbuckets[0]):
            print self.heatmap2D[i]

        print "results"
        yield ('colname0','minvalue0','maxvalue0','colname1','minvalue1','maxvalue1','num')
        for x in xrange(0, self.numberofbuckets[0]):
            for y in xrange(0, self.numberofbuckets[1]):
                yield self.colnames[0], \
                      self.globalminvalues[0] + x * self.step[0], \
                      self.globalminvalues[0] + (x+1) * self.step[0], \
                      self.colnames[1], \
                      self.globalminvalues[1] + y * self.step[1] , \
                      self.globalminvalues[1] + (y+1) * self.step[1] , \
                      self.heatmap2D[x][y]


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
