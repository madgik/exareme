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
        if frequency < 1:
            frequency = 1

        if 'granularity' not in dictargs:
            granularity = 60                   #window slides every 1 minute by default
        else:
            granularity = int(dictargs['granularity'])
        if granularity < 1:
            granularity = 1

        if 'equivalence' not in dictargs:
            equivalence = "std"                   #window slides every 1 minute by default
        else:
            equivalence = dictargs['equivalence']

        if 'parts' not in dictargs:
            parts = 1                   #window slides every 1 minute by default
        else:
            parts = int(dictargs['parts'])
            if parts < 2:
                parts = 2

            if 'minepochtime' not in dictargs:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "No minepochtime argument ")
            else:
                minwid = int(float(long(dictargs['minepochtime']))/float(frequency * granularity))

            if 'maxepochtime' not in dictargs:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "No maxepochtime argument ")
            else:
                maxwid = int(float(long(dictargs['maxepochtime']))/float(frequency * granularity))

            # modwid = math.ceil(float((maxwid - minwid) + 1) / float(parts))
            modwid = float((maxwid - minwid) + 1) / float(parts)

        # print 'maxwid', maxwid, 'minwid', minwid
        cur = envars['db'].cursor()

        c = cur.execute(query, parse=False)

        try:
            if parts == 1:
                yield [('wid', 'integer'), ('abox', 'integer')] + list(cur.getdescriptionsafe())
            else:
                yield [('part', 'int'), ('wid', 'integer'), ('abox', 'integer')] + list(cur.getdescriptionsafe())
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
        wid = int(float(r[timecolumn])/float(frequency * granularity))
        windowendtime = (wid + 1) * (frequency*granularity)
        windowstartaboxtime = windowendtime - windowaboxes*granularity + 1
        tupletime = long(r[timecolumn])
        abox = aboxfunc(tupletime, granularity, windowstartaboxtime)

        if tupletime >= (windowendtime - winlen):
            window[abox].append(r)
            if parts == 1:
                yield (wid, abox,) + r
            else:
                yield (int(float(wid - minwid) / float(modwid)), wid, abox,) + r

        while True:
            try:
                r = c.next()
            except StopIteration:
                emptywindow = False
                while not emptywindow:
                    numberofslidingwindows = 1
                    numberofslides = numberofslidingwindows * frequency

                    if numberofslides > windowaboxes:
                        numberofslides = windowaboxes

                    for _ in range(0, numberofslides):
                        window.append([])

                    windowstartaboxtime += numberofslidingwindows*frequency*granularity
                    windowendtime += numberofslidingwindows*frequency*granularity
                    wid += numberofslidingwindows # *frequency # Maybe must uncomment

                    emptywindow = True
                    for i, l in enumerate(window):
                        for t in l:
                            emptywindow = False
                            if (windowendtime - winlen) <= t[timecolumn] <= windowendtime:
                                if parts == 1:
                                    yield (wid, i,) + t
                                else:
                                    yield (int(float(wid - minwid) / float(modwid)), wid, i,) + t

                raise StopIteration

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
                wid += numberofslidingwindows # *frequency # Maybe must uncomment

                for i, l in enumerate(window):
                    for t in l:
                        if (windowendtime - winlen) <= t[timecolumn] <= windowendtime:
                            if parts == 1:
                                yield (wid, i,) + t
                            else:
                                yield (int(float(wid - minwid) / float(modwid)), wid, i,) + t

            if tupletime < (windowendtime - winlen):
                continue

            abox = aboxfunc(tupletime, granularity, windowstartaboxtime)
            window[abox].append(r)
            if (windowendtime - winlen) <= r[timecolumn] <= windowendtime:
                if parts == 1:
                    yield (wid, abox,) + r
                else:
                    yield (int(float(wid - minwid) / float(modwid)), wid, abox,) + r

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
