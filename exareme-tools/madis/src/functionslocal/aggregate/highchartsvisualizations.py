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

try:
    from collections import OrderedDict
except ImportError:
    # Python 2.6
    from lib.collections26 import OrderedDict


__docformat__ = 'reStructuredText en'


class highchartheatmap:
    # i, j, val, title
#
# ''' chart: {  type: 'heatmap', marginTop: 40, marginBottom: 80, plotBorderWidth: 1 },
#     title: { text: 'Sales per employee per weekday' },
#     xAxis: { categories: ['Alexander', 'Marie', 'Maximilian']},
#     yAxis: { categories: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'], title: null },
#     colorAxis: { min: 0, minColor: '#FFFFFF', maxColor: Highcharts.getOptions().colors[0] },
#     legend: { align: 'right',layout: 'vertical', margin: 0, verticalAlign: 'top', y: 25, symbolHeight: 280 },
#     tooltip: { formatter: function () {
#             return '<b>(' + this.series.xAxis.categories[this.point.x] + ", "+ this.series.yAxis.categories[this.point.y]+')= '+ this.point.value + '</b>';
#         }
#     },
#     series: [{
#         borderWidth: 1,
#         data: [[0, 0, 10], [0, 1, 19], [0, 2, 8], [0, 3, 24], [0, 4, 67], [1, 0, 92], [1, 1, 58], [1, 2, 78], [1, 3, 117], [1, 4, 48], [2, 0, 35], [2, 1, 15], [2, 2, 123], [2, 3, 64], [2, 4, 52]],
#         dataLabels: {
#             enabled: true,
#             color: '#000000'
#         }
#     }]
# '''
    registered = True #Value to define db operator

    def __init__(self):
        self.n = 0
        self.xcategories = []
        self.ycategories = []
        self.mydata = []

    def step(self, *args):
        try:
            if str(args[0]) not in self.xcategories:
                self.xcategories.append(str(args[0]))
            if str(args[1]) not in self.ycategories:
                self.ycategories.append(str(args[1]))

            self.mydata.append([self.xcategories.index(str(args[0])),self.ycategories.index(str(args[1])),float(args[2])])
            self.title =str(args[3])
            self.xtitle =str(args[4])
            self.ytitle =str(args[5])
        except (ValueError, TypeError):
            raise

    def final(self):
        # print "self.xcategories", self.xcategories
        # print "self.ycategories", self.ycategories
        yield ('highchartheatmap',)
        # print  self.mydata
        myresult="chart: {  type: 'heatmap', marginTop: 40, marginBottom: 80, plotBorderWidth: 1 },"
        myresult += " title: { text: '" + self.title + "' },"
        myresult += " xAxis: { categories: " + str(self.xcategories) + "},"
        myresult += " yAxis: { categories: " + str(self.ycategories) + "},"
        myresult += " colorAxis: { min: 0, minColor: '#FFFFFF', maxColor: Highcharts.getOptions().colors[0] },"
        myresult += " legend: { align: 'right',layout: 'vertical', margin: 0, verticalAlign: 'top', y: 25, symbolHeight: 280 },"
        # myresult += " tooltip: { formatter: function () {return '<b>(' + this.series.xAxis.categories[this.point.x] + ", "+ this.series.yAxis.categories[this.point.y]+')= '+ this.point.value + '</b>';}},"
        myresult += " series: [{  borderWidth: 1, data: "
        myresult += str(self.mydata)
        myresult += ", dataLabels: { enabled: true,color: '#000000'}}]"
        yield (myresult,)




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