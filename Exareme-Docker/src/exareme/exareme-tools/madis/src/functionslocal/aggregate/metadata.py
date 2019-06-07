__docformat__ = 'reStructuredText en'

import json
import re

try:
    from collections import OrderedDict
except ImportError:
    # Python 2.6
    from lib.collections26 import OrderedDict


class sqltypestotext:

    registered = True  # Value to define db operator

    def __init__(self):
        self.data = {}
        self.colnames = None

    def step(self, *args):
        self.data[str(args[0])]=str(args[1])
        self.colnames  = str(args[2])

    def final(self):
        self.colnames = re.split(',',self.colnames)
        self.colnames = [x for x in self.colnames if x] # remove nulls elements of the list
        result =""
        for name in self.colnames:
            result += str(self.data[name])+","
        return result[:-1]





