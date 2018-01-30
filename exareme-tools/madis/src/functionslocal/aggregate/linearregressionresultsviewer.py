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

class linearregressionresultsviewer:

    registered = True #Value to define db operator

    def __init__(self):
        self.n = 0
        self.mydata = dict()
        self.variablenames = []

    def step(self, *args):
        # if self.n == 0:
        #     print args, len(args)
            # self.noofvariables = args[4]
            # self.noofclusters = args[5]
        try:
            self.variablenames.append(str(args[0]))
            self.mydata[(args[0])] =float(args[1]),float(args[2]),float(args[3]),float(args[4])
            self.n += 1
            # if self.n <= self.noofvariables :
            #     self.variablenames.append(str(args[1]))
        except (ValueError, TypeError):
            raise

    def final(self):
        yield ('linearregressionresult',)

        myresult='''
        resources: [
        {
            name: 'linear-regression',
            profile: 'tabular-data-resource',
            data: [
            ['variable', 'coefficient', 'standard_error', 't-value', 'p-value'],
            '''

        for i in xrange(len(self.variablenames)):
            row=[]
            for j in xrange(4):
                row.append(self.mydata[(self.variablenames[i])][j])
            myresult+= str(row)
            if i< len(self.variablenames)-1:
                myresult+=''','''

        myresult+='''
            ],
                schema:  {
                    fields: [
                        {name: 'variable', type: 'string'},
                        {name: 'coefficient', type: 'number'},
                        {name: 'standard_error', type: 'number'},
                        {name: 't-value', type: 'number'},
                        {name: 'p-value', type: 'string'}
                    ]
                }
            }
        ]'''


        yield (myresult,)

