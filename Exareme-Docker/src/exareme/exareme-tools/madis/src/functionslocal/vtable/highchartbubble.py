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

        if 'title' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No title argument ")

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        mydata = []
        for myrow in c:
            mydata.append(list(myrow))
        print schema[2]
        print schema[2][0]
        if len(schema)!=3 or (len(schema)==3 and str(schema[2][0])!='noofpoints'):
            myresult =  {
                    "type" : "application/vnd.highcharts+json",
                    "data" : { "chart" : { "type": "bubble",  "plotBorderWidth": 1, "zoomType": "xy"},
                                "title" : { "text": str(dictargs['title']) },
                                "subtitle":{"text":"The plot is empty as there are not two variables "}
                    }
                }
        elif len(mydata)==0:
            myresult =  {
                    "type" : "application/vnd.highcharts+json",
                    "data" : { "chart" : { "type": "bubble",  "plotBorderWidth": 1, "zoomType": "xy"},
                                "title" : { "text": str(dictargs['title']) },
                                "subtitle":{"text":"The plot is empty as there are not data points"}
                    }
                }
        else:
            myresult =  {
                "type" : "application/vnd.highcharts+json",
                "data" : { "chart" : { "type": "bubble",  "plotBorderWidth": 1, "zoomType": "xy"},
                            "title" : { "text": str(dictargs['title']) },
                            "xAxis" : { "gridLineWidth": 1, "title": {"text": str(schema[0][0]), "align":"middle" }},
                            "yAxis":  { "gridLineWidth": 1, "title": {"text": str(schema[1][0]), "align":"middle" }},
                            "series": [{ "data": mydata}]
                }
            }

        myjsonresult = json.dumps(myresult)
        yield [('highchartresult',)]
        yield (myjsonresult,)


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
