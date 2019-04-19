import datetime
import functions
import time
from lib import iso8601

import vtbase

registered = True


class StreamdataVT(vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)
        quantum = None
        output = False
        nextproducetuple = 0  # The next tuple that must appear

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument ")
        query = dictargs['query']

        if 'ratio' in dictargs:
            self.ratio = float(dictargs['ratio'])
            if self.ratio >= 1:
                self.ratio = int(self.ratio)
            elif self.ratio <= 0:
                self.ratio = 1
            else:
                if (float(float(1) / float(self.ratio)) - int(float(1) / float(self.ratio))) != 0:
                    raise functions.OperatorError(__name__.rsplit('.')[-1], "1/Ratio must be a not decimal number ")
        else:
            self.ratio = 1

        self.quantum = None
        if 'quantum' in dictargs:
            self.quantum = int(dictargs['quantum'])
            if self.quantum <= 0:
                self.quantum = 1

        if 'output' in dictargs:
            if str(dictargs['output']).lower() == 'same':
                output = True

        if 'starttimestamp' in dictargs:
            dt = iso8601.parse_date(dictargs['starttimestamp'])
            nextproducetupletime = long(time.mktime(dt.utctimetuple()) - time.timezone)
        else:
            nextproducetupletime = long(time.time())

        lines = []
        cur = envars['db'].cursor()
        q = cur.execute(query, parse=False)

        schema = list(cur.getdescriptionsafe())
        for x in range(len(schema)):
            if str(schema[x][0]).lower() == 'timestamp':
                schema[x] = ('timestamp1', 'text')

        try:
            yield [('timestamp', 'text')] + schema
        except StopIteration:
            try:
                raise
            finally:
                try:
                    cur.close()
                except:
                    pass

        if not output:
            numoflines = sum(1 for x in self.getDataGen(q)) - 1
            nextproducetuple = int(nextproducetupletime) % int(float(numoflines))

        # For ever
        simtuple = []
        while True:
            try:
                q = cur.execute(query, parse=False)
                dataGen = self.getDataGen(q)
                for x in range(nextproducetuple):
                    dataGen.next()

                for secstuples in dataGen:
                    try:
                        time.sleep(float(int(nextproducetupletime) - float(time.time().real)))
                    except IOError:
                        pass

                    # For every tuple in second
                    for line in secstuples:
                        simtuple[:] = [datetime.datetime.utcfromtimestamp(nextproducetupletime).strftime(
                            '%Y-%m-%dT%H:%M:%S+00:00')]
                        for value in line:
                            simtuple.append(value)

                        yield simtuple

                    nextproducetupletime += 1

                nextproducetuple = 0
            except KeyboardInterrupt:
                break

    def getDataGen(self, q):
        if self.quantum is not None:
            try:
                t = q.next()
                ttime = t[0]
                tuples = [t]
                for t in q:
                    sec = int(t[0] / self.quantum) - int(ttime / self.quantum)
                    if sec == 0:
                        tuples.append(t)
                    elif sec < 0:
                        raise functions.OperatorError(__name__.rsplit('.')[-1], "Time not in order ")
                    else:
                        yield tuples
                        for x in range(sec - 1):
                            yield []
                        tuples = [t]

                    ttime = t[0]
            except:
                raise functions.OperatorError(__name__.rsplit('.')[-1],
                                              "The first column must be a long (timestamp in epoch time) ")
        else:
            if self.ratio < 1.000:
                for t in q:
                    for x in range(int(1.0 / float(self.ratio)) - 1):
                        yield []
                    yield [t]
            else:
                try:
                    while True:
                        tuples = []
                        for x in range(int(self.ratio)):
                            tuples.append(q.next())

                        yield tuples
                except StopIteration:
                    pass


def Source():
    return vtbase.VTGenerator(StreamdataVT)


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    from functions import *

    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest

        doctest.testmod()
