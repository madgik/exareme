import math
from array import array
import cPickle

class convertcovariancetabletoarray:

    registered = True #Value to define db operator

    def __init__(self):
        self.n = 0
        self.data = {}
        self.mydata1D = []
        self.headers = []
        self.flag = True

    def step(self, *args):

        try:
            if self.n > 0  and self.headers[0] != args[0]:
                self.flag = False

            if  self.flag == True:
                self.headers.append(str(args[1]))

            self.mydata1D.append([args[0],args[1],float(args[2])])
            self.n += 1

        except (ValueError, TypeError):
            raise

    def final(self):
        yield ('header','covvalues')
        if bool(self.mydata1D):
            noofvariables = int(math.sqrt(self.n))
            self.mydata2D = [[0 for i in xrange(0, noofvariables)] for x in xrange(0,noofvariables)]

            for d in self.mydata1D:
                self.mydata2D[self.headers.index(d[0])][self.headers.index(d[1])] += d[2]


            yield (str(self.headers), str(self.mydata2D))

