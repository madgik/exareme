__docformat__ = 'reStructuredText en'

import json

try:
    from collections import OrderedDict
except ImportError:
    # Python 2.6
    from lib.collections26 import OrderedDict


class tabletojson:
    """
    .. function:: tabletojson

    Example:

    """
    registered = True  # Value to define db operator

    def __init__(self):
        self.d = {}
        self.dlist = []
        self.i = 0

    def step(self, *args):
        if self.i == 0:
            self.colnames = str(args[-1]).split(',')

        for j in xrange(len(args) - 1):
            # print self.colnames[j],args[j]
            self.d[self.colnames[j]] = args[j]
        self.dlist.append(self.d.copy())
        self.i = self.i + 1

    def final(self):
        return json.dumps(self.dlist)


class jsontotable:
    registered = True  # Value to define db operator

    def __init__(self):
        self.d = {}
        self.dlist = []
        self.i = 0

    def step(self, *args):
        self.jsondata = json.loads(args[0])

    def final(self):
        init = True
        for x in self.jsondata:
            if init is True:
                colnames = []
                vals = []
                for key, value in x.iteritems():
                    colnames.append(str(key))
                    vals.append(value)
                yield tuple(colnames)
                yield tuple(vals)
                init = False
            else:
                vals = []
                for z in xrange(len(colnames)):
                    vals.append(x[colnames[z]])
                yield tuple(vals)
