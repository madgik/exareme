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
        self.addtype = False

    def step(self, *args):
        if self.i == 0:
            self.colnames = str(args[-2]).split(',')
            self.addtype = int(args[-1])

        for j in xrange(len(args) - 2):
            # print self.colnames[j],args[j]
            self.d[self.colnames[j]] = args[j]
        self.dlist.append(self.d.copy())
        self.i = self.i + 1

    def final(self):
        if self.addtype == 1:
            dlist = { "type": "application/json", "data": self.dlist }
        else:
            dlist = self.dlist
        json_result = json.dumps(dlist)
        json_result= json_result.replace(' ','')
        return json_result


class jsontotable:
    registered = True  # Value to define db operator

    def __init__(self):
        self.d = {}
        self.dlist = []
        self.i = 0


    def step(self, *args):

        self.jsondata = json.loads(args[0])
        if len(args)==2:
            self.schema = str(args[1]).split(',')

    def final(self):
        init = True
        if len(self.jsondata)==0:
            yield tuple(self.schema,)
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
