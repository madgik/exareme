import math
# import numpy
from array import *

class dbscan: #minx,miny,stepx, stepy, count

    registered = True #Value to define db operator

    def __init__(self):

        self.heatmap1D = []
        self.noofrows = 0
        self.noofvariables = 0

        self.step = []
        self.numberofbuckets = []
        self.mask  = []
        self.sizemaskx = 0
        self.sizemasky = 0
        self.heatmap2D = []
        self.density = []
        self.maxclusterid = 0
        self.clusters = []

    def step(self, *args):

        if self.noofvariables == 0:

            self.noofvariables = len(args) - 2  # Afairw 2 sthles (dhladh ta eps, plithos)

            self.globalminvalues  = [None] *  int((self.noofvariables -1)/2.0 ) # self.noofvariables einai panta zugos arithmos
            self.globalmaxvalues  = [None] *  int((self.noofvariables -1)/2.0 )

            try:
                self.eps = args[self.noofvariables ]
                self.plithos = args[self.noofvariables + 1]

                for i in xrange(0, int((self.noofvariables -1)/2.0 )):
                    self.globalminvalues[i] = args[ i*2 ]
                    self.globalmaxvalues[i] = args[ i*2 + 1]

            except (ValueError, TypeError):
                 return

        try:
            self.heatmap1D.append( array('d', [args[i] for i in xrange(0, self.noofvariables)]))
            for i in xrange(0, int((self.noofvariables -1)/2.0 )):
                if args[i*2] < self.globalminvalues[i]:
                    self.globalminvalues[i] = args[i*2]
                if args[i*2 + 1] > self.globalmaxvalues[i]:
                    self.globalmaxvalues[i] = args[i*2 + 1]
            self.noofrows += 1

        except (ValueError, TypeError):
            return


    def final(self):

        self.step  = [0.0] *  int((self.noofvariables -1)/2.0 )
        self.step[0] = self.heatmap1D[0][1]-self.heatmap1D[0][0]
        self.step[1] = self.heatmap1D[0][3]-self.heatmap1D[0][2]

        self.numberofbuckets  = [0.0] *  int((self.noofvariables -1)/2.0 )
        print "numberofbuckets:", self.numberofbuckets

        self.numberofbuckets[0] = int(math.ceil((self.globalmaxvalues[0]-self.globalminvalues[0]) / self.step[0]))
        self.numberofbuckets[1] = int(math.ceil((self.globalmaxvalues[1]-self.globalminvalues[1]) / self.step[1]))

        #--------------------------------------------------------------------------------------------------------------
        #1. Kataskeuh self.heatmap2D
        self.heatmap2D = [array('d', [0 for i in xrange(0, (self.numberofbuckets[1]))]) for x in xrange(0,self.numberofbuckets[0])]

        for d in self.heatmap1D:
            print d
            i = (d[0]-self.globalminvalues[0])/self.step[0]
            j = (d[2]-self.globalminvalues[1])/self.step[1]
            self.heatmap2D[int(i)][int(j)] += d[4]

        print "2D Heatmap"
        for i in xrange(0,self.numberofbuckets[0]):
            print self.heatmap2D[i]

        #--------------------------------------------------------------------------------------------------------------
        #2. Compute mask
        print self.eps, self.step

        r0f = self.eps/self.step[0]
        r1f = self.eps/self.step[1]

        self.sizemaskx = 2 * int(math.ceil(r0f)) + 1
        self.sizemasky = 2 * int(math.ceil(r1f)) + 1

        self.mask = [array('d', [1 for i in xrange(0, (self.sizemasky))]) for x in xrange(0,self.sizemaskx)]

        print "r0f, r1f" ,r0f, r1f
        print self.sizemaskx,self.sizemasky

        for i in xrange(self.sizemaskx):
            ii = i-((self.sizemaskx-1)/2)
            for j in xrange(self.sizemasky):
                jj = j-((self.sizemasky-1)/2)
                # print i,j,ii,jj
                if abs(ii) > math.floor(r0f + 0.5) -1:
                    self.mask[i][j] = self.mask[i][j] * (r0f + 0.5 - math.floor(r0f + 0.5))
                if abs(jj) > math.floor(r1f + 0.5) -1:
                    self.mask[i][j] = self.mask[i][j] * (r1f + 0.5 - math.floor(r1f + 0.5))

        print "Mask"
        for i in xrange(0,len(self.mask)):
             print self.mask[i]

        #--------------------------------------------------------------------------------------------------------------
        #3. Compute density and set 1 if it is a core point and 2 if it is a border point

        self.density = [array('d', [0 for i in xrange(0, (self.numberofbuckets[1]))]) for x in xrange(0,self.numberofbuckets[0])]
        for i in xrange(self.numberofbuckets[0]):
            for j in xrange(self.numberofbuckets[1]):
                # compute density
                conv = self.convolution(i,j)
                # set labels
                if  conv >= self.plithos:
                    self.density[i][j] = 1
                elif conv > 0 and conv <self.plithos:
                    self.density[i][j] = 2
                else:
                    self.density[i][j] = 0

        print "Density"
        for i in xrange(0,self.numberofbuckets[0]):
             print self.density[i]

        #--------------------------------------------------------------------------------------------------------------
        #4. Find Core Points: Put labels
        self.clusters = [array('d', [0 for i in xrange(0, (self.numberofbuckets[1]))]) for x in xrange(0,self.numberofbuckets[0])]
        for i in xrange(self.numberofbuckets[0]):
            for j in xrange(self.numberofbuckets[1]):
                if self.density[i][j] ==1:
                    self.clusters[i][j]=self.density[i][j]

        self.maxclusterid = 3
        for i in xrange(self.numberofbuckets[0]):
            for j in xrange(self.numberofbuckets[1]):
                if  self.clusters[i][j] == 1: # core point
                    queue =[]
                    queue.append([i,j])
                    self.clusters[i][j] = self.maxclusterid
                    self.loopconnectedcomponents(queue)
                    self.maxclusterid += 1

        print "Clusters Core Points"
        for i in xrange(0,self.numberofbuckets[0]):
             print self.clusters[i]

        #Return core points
        yield ('x','y','corepoint','pointsnumber','clusterid', 'percentage')
        for x in xrange(self.numberofbuckets[0]):
            for y in xrange(self.numberofbuckets[1]):
                if self.density[x][y]==1.0:
                    yield x,y,1,self.heatmap2D[x][y],self.clusters[x][y], 100.0
        #--------------------------------------------------------------------------------------------------------------
        #5. Find density-reachable points
        for x in xrange(self.numberofbuckets[0]):
             for y in xrange(self.numberofbuckets[1]):
                 if self.heatmap2D[x][y]>0 and self.density[x][y]==2.0:
                    cl = [0.0] *(self.maxclusterid + 1)
                    for i in xrange(0,self.sizemaskx): # apply filter
                        for j in xrange(0,self.sizemasky):
                            a = i-(self.sizemaskx-1)/2
                            b = j-(self.sizemasky-1)/2
                            if x+a>=0 and y+b>=0 and x+a<self.numberofbuckets[0] and y+b<self.numberofbuckets[1]:
                                cl[int(self.clusters[x+a][y+b])] += self.mask[i][j]*self.heatmap2D[x+a][y+b]

                    cltotalvalue = 0
                   # clmaxvalue = 0
                    for nocluster in xrange(3,self.maxclusterid + 1):
                        cltotalvalue+=cl[nocluster]
                       # clmaxvalue=max(clmaxvalue, cl[nocluster])

                    for nocluster in xrange(3,self.maxclusterid + 1):
                        if cl[nocluster]>0:
                            yield x,y,0,self.heatmap2D[x][y],nocluster,100*cl[nocluster]/cltotalvalue


        return

    def convolution(self,x,y):
        result = 0
        for i in xrange(0,self.sizemaskx):
            for j in xrange(0,self.sizemasky):
                a = i-(self.sizemaskx-1)/2
                b = j-(self.sizemasky-1)/2
                if x+a>=0 and y+b>=0 and x+a<self.numberofbuckets[0] and y+b<self.numberofbuckets[1]:
                    result += self.mask[i][j]*self.heatmap2D[x+a][y+b]
        return result


    def loopconnectedcomponents(self,queue):
        if len(queue) == 0:
            return
        [i,j] = queue.pop(0)
        for ii in xrange(-1,2):
            for jj in xrange(-1,2):
                 if i+ii>=0 and i+ii < self.numberofbuckets[0] and j+jj>=0 and j+jj < self.numberofbuckets[1] \
                         and self.clusters[i+ii][j+jj] == 1:
                    queue.append([i+ii,j+jj])
                    self.clusters[i+ii][j+jj] = self.maxclusterid
        self.loopconnectedcomponents(queue)
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
