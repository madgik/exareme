from array import *

import functions

try:
    from collections import OrderedDict
except ImportError:
    # Python 2.6
    from lib.collections26 import OrderedDict


__docformat__ = 'reStructuredText en'




class gramian:

    registered = True #Value to define db operator

    def __init__(self):
        self.init = True
        self.idcurrent = None
        self.datagroup = dict()
        self.countrecords = 0
        self.result = None
        self.varcount = None

    def initargs(self, args):
        self.init = False
        self.idcurrent = args[0]

        if not args:
            raise functions.OperatorError("covariance","No arguments")

    def step(self, *args):
        if self.init:
            self.initargs(args)

        if self.idcurrent != args[0]:
            if self.result is None:
                self.varcount = len(self.datagroup)
                vals = array('d', [self.datagroup[x] for x in sorted(self.datagroup.keys())])
                self.result = array('d', [0] * int(self.varcount*(self.varcount+1)/2))
            else:
                vals = array('d', [self.datagroup[x] for x in sorted(self.datagroup.keys())])

            l = 0

            lvarcount = self.varcount
            lres = self.result
            for i in xrange(0, lvarcount):
                for j in xrange(i, lvarcount):
                    lres[l] += vals[i] * vals[j]
                    l += 1
            self.countrecords += 1

        self.idcurrent = args[0]
        self.datagroup[args[1]] = float(args[2])

    def final(self):
        skeys = sorted(self.datagroup.keys())

        if self.result is None:
            self.varcount = len(self.datagroup)
            vals = [0] * int(self.varcount)
            self.result = array('d', [0] * int(self.varcount*(self.varcount+1)/2))
        else:
            vals = array('d', [self.datagroup[x] for x in skeys])

        self.countrecords += 1

        yield ('attr1', 'attr2', 'val', 'reccount')

        l = 0
        lvarcount = self.varcount
        lres = self.result
        lreccount = self.countrecords
        for i in xrange(0,lvarcount):
            for j in xrange(i,lvarcount):
                yield skeys[i], skeys[j], lres[l] + vals[i] * vals[j], lreccount
                l += 1


