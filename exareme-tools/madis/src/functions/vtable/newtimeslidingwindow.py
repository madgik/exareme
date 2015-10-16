import setpath
import vtbase
import functions
from collections import deque
import time
import math

### Classic stream iterator
registered = True


class NewTimeSlidingWindow(vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")
        query = dictargs['query']

        if 'timewindow' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No TimeWindow argument ")
        else:
            winlen = int(dictargs['timewindow'])

        if 'timecolumn' not in dictargs:
            timecolumn = 0
        else:
            timecolumn = int(dictargs['timecolumn'])

        if 'frequency' not in dictargs:
            frequency = 1                   #window slides every 1 minute by default
        else:
            frequency = int(dictargs['frequency'])

        if 'granularity' not in dictargs:
            granularity = 60                   #window slides every 1 minute by default
        else:
            granularity = int(dictargs['granularity'])

        if 'equivalence' not in dictargs:
            equivalence = "std"                   #window slides every 1 minute by default
        else:
            equivalence = dictargs['equivalence']

        cur = envars['db'].cursor()

        c = cur.execute(query, parse=False)

        try:
            yield [('wid', 'integer')] + [('abox', 'integer')] + list(cur.getdescriptionsafe())
        except StopIteration:
            try:
                raise
            finally:
                try:
                    c.close()
                except:
                    pass

        if equivalence == 'floor':
            windowaboxes = int(math.ceil(float(winlen)/float(granularity))) + 1
            aboxfunc = GetFloorAbox
        elif equivalence == 'ceil':
            windowaboxes = int(math.floor(float(winlen)/float(granularity))) + 1
            aboxfunc = GetCeilAbox
        else:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Standard equivalence are not supported yet ")

        window = deque([[] for _ in xrange(windowaboxes)], windowaboxes)

        r = c.next()
        wid = 0
        abox = windowaboxes - 1
        windowendtime = r[timecolumn]
        # wid = int(float(r[timecolumn])/float(frequency * granularity))
        # windowendtime = wid * (frequency*granularity)
        windowstartaboxtime = windowendtime - windowaboxes*granularity + 1
        window[abox].append(r)
        yield (wid, abox,) + r

        tupletime = -1
        while True:
            r = c.next()

            if tupletime > long(r[timecolumn]):
                raise functions.OperatorError(__name__.rsplit('.')[-1], "Rows are not in time order ")

            tupletime = long(r[timecolumn])

            # slides the window
            while tupletime > windowendtime:
                numberofslidingwindows = 1
                numberofslides = numberofslidingwindows * frequency

                if numberofslides > windowaboxes:
                    numberofslides = windowaboxes

                for _ in range(0, numberofslides):
                    window.append([])

                windowstartaboxtime += numberofslidingwindows*frequency*granularity
                windowendtime += numberofslidingwindows*frequency*granularity
                wid += numberofslidingwindows*frequency

                for i, l in enumerate(window):
                    for t in l:
                        if (windowendtime - winlen) <= t[timecolumn] <= windowendtime:
                            yield (wid, i,) + t

            if tupletime < (windowendtime - winlen):
                continue

            abox = aboxfunc(tupletime, granularity, windowstartaboxtime)
            window[abox].append(r)
            if (windowendtime - winlen) <= r[timecolumn] <= windowendtime:
                yield (wid, abox,) + r

def GetFloorAbox(tupletime, granularity, windowstartaboxtime):
    return int(float(tupletime - (granularity - 1) - windowstartaboxtime) / float(granularity))

def GetCeilAbox(tupletime, granularity, windowstartaboxtime):
    return int(float(tupletime - windowstartaboxtime) / float(granularity))

def Source():
    return vtbase.VTGenerator(NewTimeSlidingWindow)

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
