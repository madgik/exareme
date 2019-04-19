class variableprofileresultsviewer:
    # type code categories header gval

    registered = True  # Value to define db operator

    def __init__(self):
        self.n1 = 0
        self.n2 = 0
        self.n3 = 0
        self.mydataSummaryStatistics = dict()
        self.mydataDatasetStatistics1 = dict()
        self.mydataDatasetStatistics2 = dict()
        # self.variablename = []
        # self.init = False

    def step(self, *args):

        if self.n1 == 0:
            # self.init = True
            self.variablename = args[1]
        try:
            if str(args[0]) == 'SummaryStatistics':
                if str(args[3]) == 'count':
                    self.mydataSummaryStatistics[str(args[3])] = int(args[4])
                else:
                    self.mydataSummaryStatistics[str(args[3])] = float(args[4])
                self.n1 += 1
            elif str(args[0]) == 'DatasetStatistics1':
                self.mydataDatasetStatistics1[str(args[3])] = int(args[4])
                self.n2 += 1
            elif str(args[0]) == 'DatasetStatistics2':
                if self.mydataDatasetStatistics2.has_key(str(args[3])):
                    d = self.mydataDatasetStatistics2[str(args[3])]
                    d[str(args[2])] = int(args[4])
                    self.mydataDatasetStatistics2[str(args[3])] = d

                else:
                    d = {}
                    d[str(args[2])] = int(args[4])
                    self.mydataDatasetStatistics2[str(args[3])] = d
                self.n3 += 1
        except (ValueError, TypeError):
            raise

    def final(self):
        yield ('result',)
        print self.n1, self.n2, self.n3
        myresult = "{\"data\": { \"schema\": {\"field\": [\
        {\"name\": \"index\",\"type\": \"string\" },\
        {\"name\": \"count\",\"type\": \"object\"},\
        {\"name\": \"average\",\"type\": \"number\"},\
        {\"name\": \"std\",\"type\": \"number\"},\
        {\"name\": \"min\",\"type\": \"number\"},\
        {\"name\": \"max\",\"type\": \"number\" } ] },\
        \"data\": [ "

        if self.n1 > 0:
            first = True
            myresult += "{ \"index\": \"" + self.variablename + "\""
            for x in self.mydataSummaryStatistics:
                myresult += ", \"" + str(x) + "\" :\"" + str(self.mydataSummaryStatistics[x]) + "\""
            myresult += "}"
            if self.n2 > 0 or self.n3 > 0:
                myresult += ","
            else:
                myresult += " ]}}"
        if self.n2 > 0:
            first = True
            myresult += "{\"count\": { "
            for x in self.mydataDatasetStatistics1:
                if first == True:
                    myresult += "\"" + str(x) + "\" :\"" + str(self.mydataDatasetStatistics1[x]) + "\""
                else:
                    myresult += ", \"" + str(x) + "\" :\"" + str(self.mydataDatasetStatistics1[x]) + "\""
                first = False
            myresult += "}}"
            if self.n3 > 0:
                myresult += ","
            else:
                myresult += " ]}}"
        if self.n3 > 0:
            first1 = True
            myresult += "{\"count\": { "
            for x in self.mydataDatasetStatistics2:
                if first1 == True:
                    myresult += "\"" + str(x) + "\" : {"
                else:
                    myresult += ",\"" + str(x) + "\" : { "
                first1 = False
                first3 = True
                for y in self.mydataDatasetStatistics2[x]:
                    if first3 is True:
                        myresult += "\"" + str(y) + "\" :\"" + str(self.mydataDatasetStatistics2[x][y]) + "\""
                    if first3 is False:
                        myresult += ", \"" + str(y) + "\" :\"" + str(self.mydataDatasetStatistics2[x][y]) + "\""
                    first3 = False
                myresult += "}"
            myresult += "}}]}}"
        yield (myresult,)


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
