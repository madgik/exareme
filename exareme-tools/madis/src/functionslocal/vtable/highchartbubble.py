import setpath
import functions
import json
registered=True

'''
Highcharts.chart('container', {
    chart: { type: 'bubble', plotBorderWidth: 1, zoomType: 'xy' },
    title: { text: 'Highcharts bubbles with radial gradient fill' },
    xAxis: {  gridLineWidth: 1 },
    yAxis: { startOnTick: false, endOnTick: false },
    series: [{
        data: [
            [9, 81, 63],
            [44, 83, 22]
        ]
    }]

});

'''
class highchartbubble(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        if len(schema)>3:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Too many columns ")
        # print schema
        myresult =  "{\"chart\":{\"type\": \"bubble\",\"plotBorderWidth\": 1,\"zoomType\": \"xy\"},"
        if 'title' in dictargs:
            myresult +=  "\"title\": { \"text\": \" " + dictargs['title'] +"  \" },"
        myresult+= "\"xAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"x: " + schema[0][0] + "\",\"align\": \"middle\"}}," \
                   "\"yAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"y: " + schema[1][0]+ "\",\"align\": \"middle\"}}," \
                    "\"series\":[{\"name\": \"clusters\",\"data\": ["
        for myrow in c:
            myresult +=str(list(myrow))+','
        myresult = myresult[:-1]+ "]}]}"
        # print "myresult", myresult
        yield [('highchartresult',)]
        yield (myresult,)

def Source():
    return functions.vtable.vtbase.VTGenerator(highchartbubble)


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
        doctest.tesdoctest.tes