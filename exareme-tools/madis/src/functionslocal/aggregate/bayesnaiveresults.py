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




'''    series: [{
        type: 'column',
        name: 'Run 1',
        data: [0.25, -0.5]
    }, {
        type: 'column',
        name: 'Run 2',
        data: [0.6,-0.25]
    }, {
        type: 'column',
        name: 'Run 3',
        data: [0.6,0.28]
    }, {
        type: 'spline',
        name: 'Average',
        data: [0.48,-0.15],
        marker: {
            lineWidth: 2,
            lineColor: Highcharts.getOptions().colors[3],
            fillColor: 'white'
        }
    }]
});
'''




class bayesnaiveresults:  #iterationnumber [2|actualclass [3|predictedclass [4|val

    registered = True #Value to define db operator

    def __init__(self):

        self.myresult ="\"title\": { \"text\": \"Combination chart\"}," \
        "\"xAxis\": { \"categories\": [" \
        "\"Accuracy\"," \
        "\"AccuracyLower\"," \
        "\"AccuracyUpper\"," \
        "\"AccuracyNull\"," \
        "\"AccuracyPValue\"," \
        \
        "\"Kappa\"," \
        "\"McnemarPValue\"," \
        \
        "\"Sensitivity\"," \
        "\"Specificity\", " \
        "\"Pos Pred Value\"," \
        "\"Neg Pred Value\", " \
        "\"Prevalence\", " \
        "\"Detection Rate\"," \
        "\"Detection Prevalence\"," \
        "\"Balanced Accuracy\"," \
        \
        "\"Precision\"," \
        "\"Recall\"," \
        "\"F1\" ]},\"series\": ["

        self.init = 1
        self.mydata= dict()

    def step(self, *args): #[1|iterationnumber [2|typecolname [3|statscolname [4|val

        try:
            self.iterationnumber_now = int(args[0])
            self.col1_now = str(args[1])  #typecolname
            self.col2 = str(args[2])
            self.val = str(args[3])
            print "val", str(self.val)=='None'
            if str(self.val)=='None':
                self.val='null'
            print "myrow: ", str(args[0]), str(args[1]), str(args[2]), str(args[2])

            if self.init == 1:
                self.iterationnumber = self.iterationnumber_now
                self.init = 0
                self.col1 = self.col1_now
            print self.col1

            if self.col1 == "statistics" or self.col1 == "avg_statistics":
                if self.iterationnumber != self.iterationnumber_now:
                    if self.col1 == "statistics":
                        self.myresult+= "{\"type\": \"column\",\"name\": \"Run " + str(self.iterationnumber) + "\", \"data\": ["
                    if self.col1 == "avg_statistics":
                        self.myresult+= "{\"type\":\"spline\", \"name\": \"Average\",\"data\": ["

                    self.myresult+=self.mydata["Accuracy"]+","
                    self.myresult+=self.mydata["AccuracyLower"]+","
                    self.myresult+=self.mydata["AccuracyUpper"]+","
                    self.myresult+=self.mydata["AccuracyNull"]+","
                    self.myresult+=self.mydata["AccuracyPValue"]+","

                    self.myresult+=self.mydata["Kappa"]+","
                    self.myresult+=self.mydata["McnemarPValue"]

                    #self.myresult+=self.mydata["Sensitivity"]+","
                    #self.myresult+=self.mydata["Specificity"]+","

                    #self.myresult+=self.mydata["Pos Pred Value"]+","
                    #self.myresult+=self.mydata["Neg Pred Value"]+","
                    #self.myresult+=self.mydata["Prevalence"]+","
                    #self.myresult+=self.mydata["Detection Rate"]+","
                    #self.myresult+=self.mydata[" Detection Prevalence"]+","
                    #self.myresult+=self.mydata["Balanced Accuracy"]+","

                    #self.myresult+=self.mydata["Precision"]+","
                    #self.myresult+=self.mydata[" Recall"]+","
                    #self.myresult+=self.mydata["F1"]
                    #self.myresult+=self.mydata["Positive Class"]

                    if  self.col1 ==  "statistics":
                        self.myresult+=  "]},"
                    if  self.col1 == "averages":
                        self.myresult+= "], \"marker\": {\"lineWidth\": 2,\"lineColor\": \"Highcharts.getOptions().colors[3]\", \"fillColor\": \"white\"}},"
                    self.mydata.clear()

            self.iterationnumber = self.iterationnumber_now
            self.col1= self.col1_now
            self.mydata[self.col2] = self.val

        except (ValueError, TypeError):
            print"error"
            raise

    def final(self):


        if len(self.mydata) >0:
            if self.col1 == "statistics" or self.col1 == "avg_statistics":

                if self.col1 == "statistics":
                    self.myresult+= "{\"type\": \"column\",\"name\": \"Run " + str(self.iterationnumber) + "\", \"data\": ["
                if self.col1 == "avg_statistics":
                    self.myresult+= "{\"type\":\"spline\", \"name\": \"Average\",\"data\": ["

                self.myresult+=self.mydata["Accuracy"]+","
                self.myresult+=self.mydata["AccuracyLower"]+","
                self.myresult+=self.mydata["AccuracyUpper"]+","
                self.myresult+=self.mydata["AccuracyNull"]+","
                self.myresult+=self.mydata["AccuracyPValue"]+","

                self.myresult+=self.mydata["Kappa"]+","
                self.myresult+=self.mydata["McnemarPValue"]

                #self.myresult+=self.mydata["Sensitivity"]+","
                #self.myresult+=self.mydata["Specificity"]+","

                #self.myresult+=self.mydata["Pos Pred Value"]+","
                #self.myresult+=self.mydata["Neg Pred Value"]+","
                #self.myresult+=self.mydata["Prevalence"]+","
                #self.myresult+=self.mydata["Detection Rate"]+","
                #self.myresult+=self.mydata[" Detection Prevalence"]+","
                #self.myresult+=self.mydata["Balanced Accuracy"]+","

                #self.myresult+=self.mydata["Precision"]+","
                #self.myresult+=self.mydata[" Recall"]+","
                #self.myresult+=self.mydata["F1"]

               #self.myresult+=self.mydata["Positive Class"]

                if  self.col1 ==  "statistics":
                    self.myresult+=  "]},"
                if  self.col1 == "avg_statistics":
                    self.myresult+= "], \"marker\": {\"lineWidth\": 2, \"lineColor\": \"Highcharts.getOptions().colors[3]\", \"fillColor\": \"red\"}}]"
                self.mydata.clear()
        else:
           self.myresult+="]"

        yield ('bayesnaiveresults',)
        yield (self.myresult,)





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