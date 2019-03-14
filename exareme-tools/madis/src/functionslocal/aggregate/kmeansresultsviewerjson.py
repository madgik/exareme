class kmeansresultsviewerjson:
    # input clid,colname,val,noofpoints, noofvariables,noofclusters,columns
    registered = True  # Value to define db operator

    def __init__(self):
        self.n = 0
        self.mydata = dict()
        self.variablenames = []
        self.init = False

    def step(self, *args):
        if self.n == 0:
            self.init = True
            self.noofvariables = args[4]
            self.noofclusters = args[5]
            self.clusterids = []

        try:
            self.mydata[(int(args[0]), str(args[1]))] = float(args[2]), int(args[3])
            self.n += 1
            if self.n <= self.noofvariables:
                self.variablenames.append(str(args[1]))
            if int(args[0]) not in self.clusterids:
                self.clusterids.append(int(args[0]))

        except (ValueError, TypeError):
            raise

    def final(self):
        import itertools
        # print self.clusterids
        # print self.mydata
        # print "variablenames" , self.variablenames
        # print tuple(itertools.chain.from_iterable((tuple(itertools.chain.from_iterable([(self.variablenames[i],) for i in xrange(self.noofvariables)])),['noofpoints'])))
        yield ('highchartresult',)

        if self.init == True:
            if self.noofvariables == 2:
                # yield tuple(itertools.chain.from_iterable((tuple(itertools.chain.from_iterable([(self.variablenames[i],) for i in xrange(self.noofvariables)])),['noofpoints'])))
                myvariables = tuple(
                    itertools.chain.from_iterable([(self.variablenames[i],) for i in xrange(self.noofvariables)]))

                myresult = "{\"chart\":{\"type\": \"bubble\",\"plotBorderWidth\": 1,\"zoomType\": \"xy\"}," \
                           "\"title\": { \"text\": \"Cluster Centers Computed by K-means\" }," \
                           "\"xAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"x: " + str(
                    myvariables[0]) + "\",\"align\": \"middle\"}}," \
                                      "\"yAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"y: " + str(
                    myvariables[1]) + "\",\"align\": \"middle\"}}," \
                                      "\"series\":[{\"name\": \"clusters\",\"data\": ["
                id = 0
                for i in self.clusterids:
                    row = []
                    for j in xrange(self.noofvariables):
                        # print "A",i,self.variablenames[j],self.mydata[(i,self.variablenames[j])]
                        row.append(self.mydata[(i, self.variablenames[j])][0])
                    row.append(self.mydata[(i, self.variablenames[self.noofvariables - 1])][1])
                    myresult += str(row)
                    if id < self.noofclusters - 1:
                        myresult += ','
                    id += 1
                myresult += "]}]}"
                # print myresult
                yield (myresult,)

            elif self.noofvariables == 3:
                myvariables = tuple(
                    itertools.chain.from_iterable([(self.variablenames[i],) for i in xrange(self.noofvariables)]))
                # print myvariables

                myresult = "{\"chart\": {\"margin\": 100, \"type\": \"scatter3d\", \"options3d\": {" \
                           "\"enabled\": true, \"alpha\": 10, \"beta\": 30, \"depth\": 250," \
                           "\"viewDistance\": 5, \"fitToPlot\": false," \
                           "\"frame\": { \"bottom\": { \"size\": 1, \"color\": \"rgba(0,0,0,0.02)\" }," \
                           "\"back\": { \"size\": 1, \"color\": \"rgba(0,0,0,0.04)\" }," \
                           "\"side\": { \"size\": 1, \"color\": \"rgba(0,0,0,0.06)\" }}}}," \
                           "\"title\": { \"text\": \"Cluster Centers Computed by K-means\"}," \
                           "\"plotOptions\": { \"scatter\": { \"width\": 20, \"height\": 20, \"depth\": 20} }," \
                           "\"xAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"x: " + str(
                    myvariables[0]) + "\",\"align\": \"middle\"}}," \
                                      "\"yAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"y: " + str(
                    myvariables[1]) + "\",\"align\": \"middle\"}}," \
                                      "\"zAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"z: " + str(
                    myvariables[2]) + "\",\"align\": \"middle\"}}," \
                                      "\"legend\": { \"enabled\": false }," \
                                      "\"series\": [{" \
                                      "\"colorByPoint\": true," \
                                      "\"data\": ["
                id = 0
                for i in self.clusterids:
                    row = []
                    for j in xrange(self.noofvariables):
                        row.append(self.mydata[(i, self.variablenames[j])][0])
                    myresult += str(row)
                    if id < self.noofclusters - 1:
                        myresult += ','
                    id += 1
                myresult += "],\"marker\": {\"radius\": 5}}]}"
                yield (myresult,)

            elif self.noofvariables > 3:
                myvariables = tuple(
                    itertools.chain.from_iterable([(self.variablenames[i],) for i in xrange(self.noofvariables)]))
                myresult = "{\"resources\": [{ \"name\": \"Cluster Centers Computed by K-means\",\"profile\": \"tabular-data-resource\",\"data\": [ [\"cluster id\","
                for i in xrange(len(self.variablenames)):
                    myresult += "\"" + str(myvariables[i]) + "\","
                myresult += "\"cluster size\"],"

                id = 0
                for i in self.clusterids:
                    myresult += "[\"" + str(i) + "\""
                    for j in xrange(self.noofvariables):
                        myresult += ", \"" + str(self.mydata[(i, self.variablenames[j])][0]) + "\""
                    myresult += ", \"" + str(self.mydata[(i, self.variablenames[self.noofvariables - 1])][1]) + "\"]"
                    # row.append(self.mydata[(i,self.variablenames[j])][0])
                    # row.append(self.mydata[(i,self.variablenames[self.noofvariables-1])][1])
                    # myresult+=str(row)
                    if id < self.noofclusters - 1:
                        myresult += ","
                    id += 1
                myresult += "], \"schema\":  { \"fields\": [{\"name\": \"cluster id\", \"type\": \"number\"},"
                # print myresult
                for i in xrange(len(self.variablenames)):
                    myresult += "{\"name\": \"" + str(myvariables[i]) + "\",\"type\": \"number\"},"
                myresult += "{\"name\": \"cluster size\", \"type\": \"number\"} ]}}]}"

                # print myresult
                yield (myresult,)
        else:
            myresult = "{\"chart\": {\"type\": \"bubble\",\"plotBorderWidth\": 1,\"zoomType\": \"xy\"}," \
                       "\"title\": { \"text\": \"No Cluster Centers Computed by K-means: Initial dataset is empty\"}," \
                       "\"xAxis\": {\"gridLineWidth\": 1}," \
                       "\"yAxis\": {\"startOnTick\": false,\"endOnTick\": false}," \
                       "\"data\": []}"
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
