import setpath
import functions
import math
import numpy as np
from numpy.linalg import inv
from lib import iso8601
import lib.jopts as jopts
import re
import datetime
import json
from fractions import Fraction
import lib.jopts as jopts
from array import *

import itertools

class kmeansresultsviewer:

    registered = True #Value to define db operator

    def __init__(self):
        self.n = 0
        self.mydata = dict()
        self.variablenames = []

    def step(self, *args):

        if self.n == 0:
            # print args, len(args)
            self.noofvariables = args[4]
            self.noofclusters = args[5]
        try:
            self.mydata[(int(args[0]),str(args[1]))] = float(args[2]),int(args[3])
            self.n += 1
            if self.n <= self.noofvariables :
                self.variablenames.append(str(args[1]))
        except (ValueError, TypeError):
            raise

    def final(self):
        import itertools
        # print self.mydata
        # print "variablenames" , self.variablenames
        # print tuple(itertools.chain.from_iterable((tuple(itertools.chain.from_iterable([(self.variablenames[i],) for i in xrange(self.noofvariables)])),['noofpoints'])))
        yield ('highchartresult',)
        if self.noofvariables == 2:
            #yield tuple(itertools.chain.from_iterable((tuple(itertools.chain.from_iterable([(self.variablenames[i],) for i in xrange(self.noofvariables)])),['noofpoints'])))
            myvariables = tuple(itertools.chain.from_iterable([(self.variablenames[i],) for i in xrange(self.noofvariables)]))

            myresult =  "{chart: {type: 'bubble',plotBorderWidth: 1,zoomType: 'xy'},    " \
                        "  title: { text: 'Kmeans Result of " + str(myvariables[0]) +" "+ str(myvariables[1]) +"'}," \
                        "  xAxis: {gridLineWidth: 1},  " \
                        "  yAxis: {startOnTick: false,endOnTick: false}," \
                        "series: [{ data: ["

            for i in xrange(self.noofclusters):
                # print i
                row = []
                for j in xrange(self.noofvariables):
                    row.append(self.mydata[(i,self.variablenames[j])][0])
                row.append(self.mydata[(i,self.variablenames[self.noofvariables-1])][1])
                #print row
                myresult+=str(row)

                if i<self.noofclusters-1:
                    myresult+=','
#               yield row
            myresult+= "]}]}"
            #print myresult
            yield (myresult,)


