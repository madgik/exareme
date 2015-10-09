import vtbase
import time
import datetime
from lib import iso8601
import functions
import math
from random import randint
registered=True

class StreamdataVT(vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)
        quantum = None
        output = False
        nextproducetuple = 0  # The next tuple that must appear

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")
        query=dictargs['query']

        if 'ratio' in dictargs:
            ratio=float(dictargs['ratio'])
            if ratio > 100:
                ratio=100
            elif ratio >= 1:
                ratio=int(ratio)
            elif ratio <= 0:
                ratio = 1
            else:
                if (float(float(1)/float(ratio)) - int(float(1)/float(ratio))) != 0:
                    raise functions.OperatorError(__name__.rsplit('.')[-1], "1/Ratio must be a not decimal number ")

        if 'quantum' in dictargs:
            quantum=int(dictargs['quantum'])
            if quantum <= 0:
                quantum = 1

        if 'output' in dictargs:
            if str(dictargs['output']).lower() == 'same':
                output = True

        if 'starttimestamp' in dictargs:
            dt=iso8601.parse_date(dictargs['starttimestamp'])
            nextproducetupletime=time.mktime(dt.utctimetuple()) - time.timezone
        else:
            nextproducetupletime=time.time()

        lines = []
        cur = envars['db'].cursor()
        q = cur.execute(query, parse=False)

        try:
            yield [('timestamp', 'text')] + list(cur.getdescriptionsafe())
        except StopIteration:
            try:
                raise
            finally:
                try:
                    cur.close()
                except:
                    pass

        if quantum is not None:
            t = q.next()
            ttime = t[0]
            lines.append([t])
            for t in q:
                sec = int(t[0]/quantum) - int(ttime/quantum)
                if sec == 0:
                    lines[-1].append(t)
                elif sec < 0:
                    raise functions.OperatorError(__name__.rsplit('.')[-1], "Time not in order ")
                else:
                    for x in range(sec - 1):
                        lines.append([])
                    lines.append([t])

                ttime = t[0]
        else:
            if ratio < 1.000:
                for t in q:
                    for x in range(int(1.0/float(ratio)) - 1):
                        lines.append([])
                    lines.append([t])
            else:
                try:
                    while True:
                        lines.append([])
                        for x in range(int(ratio)):
                            lines[-1].append(q.next())
                except StopIteration:
                    pass

        numoflines=len(lines) - 1
        if not output:
            nextproducetuple = int(nextproducetupletime) % int(float(numoflines))

        # For ever
        simtuple = []
        while True:
            for secstuples in lines[nextproducetuple:]:
                try:
                    time.sleep(float(nextproducetupletime-(time.time())))
                except IOError:
                    pass

                # For every tuple in second
                for line in secstuples:
                    simtuple[:] = [datetime.datetime.utcfromtimestamp(nextproducetupletime).strftime('%Y-%m-%dT%H:%M:%S+00:00')]
                    for value in line:
                        simtuple.append(value)

                    yield simtuple

                nextproducetupletime += 1

            nextproducetuple=0

def Source():
    return vtbase.VTGenerator(StreamdataVT)

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
