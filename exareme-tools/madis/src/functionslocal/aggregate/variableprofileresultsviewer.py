class variableprofileresultsviewer:
    # type code categories header gval

    import itertools
    registered = True #Value to define db operator

    def __init__(self):
        self.n1 = 0
        self.n2 = 0
        self.n3 = 0
        self.mydataSummaryStatistics = dict()
        self.mydataDatasetStatistics1 = dict()
        self.mydataDatasetStatistics2 = dict()
        self.variablenames = []
        #self.init = False

    def step(self, *args):

        if self.n1 == 0:
            #self.init = True
            self.variablename = args[1]
        try:
            if str(args[0]) == 'SummaryStatistics':
                if str(args[3]) == 'count':
                    self.mydataSummaryStatistics[str(args[3])] = int(args[4])
                else:
                    self.mydataSummaryStatistics[str(args[3])] = float(args[4])
                self.n1 +=1
            elif str(args[0]) == 'DatasetStatistics1':
                self.mydataDatasetStatistics1[str(args[3])] = int(args[4])
                self.n2 += 1
            elif str(args[0]) == 'DatasetStatistics2':
                if  self.mydataDatasetStatistics2.has_key(str(args[3])):
                    d = self.mydataDatasetStatistics2[str(args[3])]
                    d[str(args[2])]= int(args[4])
                    self.mydataDatasetStatistics2[str(args[3])] = d

                else:
                    d = {}
                    d[str(args[2])]= int(args[4])
                    self.mydataDatasetStatistics2[str(args[3])] = d
                self.n3 += 1
                print self.mydataDatasetStatistics2
        except (ValueError, TypeError):
            raise

    def final(self):
        yield ('result',)
        print self.n1, self.n2, self.n3
        myresult = "'data': { 'schema': {'field': [\
        {'name': 'index','type': 'string' },\
        {'name': 'count','type': 'object'},\
        {'name': 'mean','type': 'number'},\
        {'name': 'std','type': 'number'},\
        {'name': 'min','type': 'number'},\
        { 'name': 'max','type': 'number' } ] },\
        'data': [ "
        if self.n1 > 0:
            myresult+= str(self.mydataSummaryStatistics)
            if self.n2 > 0  or self.n3 > 0:
                myresult+=","
        if self.n2 > 0:
            myresult+=" {'count':" + str(self.mydataDatasetStatistics1) + "} "
            if self.n3 > 0:
                myresult +=","
        if self.n3 > 0:
            myresult+=" {'count':" + str(self.mydataDatasetStatistics2) +"}"
        myresult += " ]}"

        print myresult
        print "SummaryStatistics", str(self.mydataSummaryStatistics)
        print "DatasetStatistics1", self.mydataDatasetStatistics1
        print "DatasetStatistics2", self.mydataDatasetStatistics2
        yield (myresult,)


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