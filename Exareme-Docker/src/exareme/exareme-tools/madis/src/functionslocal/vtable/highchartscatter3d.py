import setpath
import functions
import json
registered=True

'''
// Set up the chart
var chart = new Highcharts.Chart(container,{
    chart: {
        margin: 100,
        type: 'scatter3d',
        animation: false,
        options3d: {
            enabled: true,
            alpha: 10,
            beta: 30,
            depth: 250,
            viewDistance: 5,
            fitToPlot: false,
            frame: {
                bottom: { size: 1, color: 'rgba(0,0,0,0.02)' },
                back: { size: 1, color: 'rgba(0,0,0,0.04)' },
                side: { size: 1, color: 'rgba(0,0,0,0.06)' }
            }
        }
    },
    title: {
        text: 'Draggable box'
    },
    subtitle: {
        text: 'Click and drag the plot area to rotate in space'
    },
    plotOptions: {
        scatter: {
            width: 10,
            height: 10,
            depth: 10
        }
    },
    yAxis: {
        min: 0,
        max: 10,
        title: null
    },
    xAxis: {
        min: 0,
        max: 10,
        gridLineWidth: 1
    },
    zAxis: {
        min: 0,
        max: 10,
        showFirstLabel: false
    },
    legend: {
        enabled: false
    },
    series: [{
        name: 'Reading',
        colorByPoint: true,
        data: [
            [1, 6, 5], [8, 7, 9], [1, 3, 4], [4, 6, 8], [5, 7, 7], [6, 9, 6],
            [7, 0, 5], [2, 3, 3], [3, 9, 8], [3, 6, 5], [4, 9, 4], [2, 3, 3],
            [5, 1, 2], [9, 9, 7], [6, 9, 9], [8, 4, 3], [4, 1, 7], [6, 2, 5],
            [0, 4, 9], [3, 5, 9], [6, 9, 1], [1, 9, 2]]
    }]
});
// Add mouse and touch events for rotation
(function (H) {
    function dragStart(eStart) {
        eStart = chart.pointer.normalize(eStart);
        var posX = eStart.chartX,
            posY = eStart.chartY,
            alpha = chart.options.chart.options3d.alpha,
            beta = chart.options.chart.options3d.beta,
            sensitivity = 5,  // lower is more sensitive
            handlers = [];
        function drag(e) {
            // Get e.chartX and e.chartY
            e = chart.pointer.normalize(e);
            chart.update({
                chart: {
                    options3d: {
                        alpha: alpha + (e.chartY - posY) / sensitivity,
                        beta: beta + (posX - e.chartX) / sensitivity
                    }
                }
            }, undefined, undefined, false);
        }
        function unbindAll() {
            handlers.forEach(function (unbind) {
                if (unbind) {
                    unbind();
                }
            });
            handlers.length = 0;
        }
        handlers.push(H.addEvent(document, 'mousemove', drag));
        handlers.push(H.addEvent(document, 'touchmove', drag));
        handlers.push(H.addEvent(document, 'mouseup', unbindAll));
        handlers.push(H.addEvent(document, 'touchend', unbindAll));
    }
    H.addEvent(chart.container, 'mousedown', dragStart);
    H.addEvent(chart.container, 'touchstart', dragStart);
}(Highcharts));
'''

class highchartscatter3d(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()
        yield [('highchartresult',)]

        mydata = ""
        for myrow in c:
            mydata +=str(list(myrow))+','

        if mydata == "":
            yield ("{}")
        else:
            if len(schema)>3:
                raise functions.OperatorError(__name__.rsplit('.')[-1],"Too many columns ")
            # print schema
            myresult = "{\"chart\": {\"margin\": 100, \"type\": \"scatter3d\", \"options3d\": {" \
                               "\"enabled\": true, \"alpha\": 10, \"beta\": 30, \"depth\": 250," \
                               "\"viewDistance\": 5, \"fitToPlot\": false," \
                               "\"frame\": { \"bottom\": { \"size\": 1, \"color\": \"rgba(0,0,0,0.02)\" }," \
                               "\"back\": { \"size\": 1, \"color\": \"rgba(0,0,0,0.04)\" }," \
                               "\"side\": { \"size\": 1, \"color\": \"rgba(0,0,0,0.06)\" }}}},"
            if 'title' in dictargs:
                myresult +=  "\"title\": { \"text\": \" " + dictargs['title'] +"  \" },"
            myresult+= "\"xAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"x: " + schema[0][0] + "\",\"align\": \"middle\"}}," \
                       "\"yAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"y: " + schema[1][0]+ "\",\"align\": \"middle\"}}," \
                        "\"zAxis\": {\"gridLineWidth\": 1 , \"title\": {\"text\": \"z: " + schema[2][0] + "\",\"align\": \"middle\"}}," \
                        "\"legend\": { \"enabled\": false }," \
                        "\"series\": [{" \
                        "\"colorByPoint\": true,"\
                        "\"data\": ["

            myresult += mydata
            myresult= myresult[:-1] + "],\"marker\": {\"radius\": 5}}]}"
            # print "myresult", myresult
            yield (myresult,)

def Source():
    return functions.vtable.vtbase.VTGenerator(highchartscatter3d)


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