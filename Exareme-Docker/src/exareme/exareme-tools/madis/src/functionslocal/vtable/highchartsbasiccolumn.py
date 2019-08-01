import setpath
import functions
import json
import re
registered=True

'''
Highcharts.chart('container', { chart: { type: 'column' },
    title: { text: 'Monthly Average Rainfall'},
    xAxis: { categories: [ 'Jan', 'Feb', 'Mar', 'Apr', 'May','Jun','Jul', 'Aug','Sep', 'Oct', 'Nov', 'Dec'],
        crosshair: true },
    yAxis: { min: 0, title: { text: 'Rainfall (mm)' } },
    tooltip: { headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
        pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
            '<td style="padding:0"><b>{point.y:.1f} mm</b></td></tr>',
        footerFormat: '</table>',
        shared: true,
        useHTML: true
    },
    plotOptions: { column: { pointPadding: 0.2, borderWidth: 0}},
    series: [
    {
        name: 'Tokyo',
        data: [49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4]

    }, {
        name: 'New York',
        data: [83.6, 78.8, 98.5, 93.4, 106.0, 84.5, 105.0, 104.3, 91.2, 83.5, 106.6, 92.3]
    }]
});

'''
'''
[1|AD[2|0[3|null[4|0.34445[5|0.7579[6|0
[1|AD[2|1[3|null[4|0.7579[5|1.17135[6|21
[1|AD[2|2[3|null[4|1.17135[5|1.5848[6|186
[1|AD[2|3[3|null[4|1.5848[5|1.99825[6|66
[1|AD[2|4[3|null[4|1.99825[5|2.4117[6|1
[1|CN[2|0[3|null[4|0.34445[5|0.7579[6|0
[1|CN[2|1[3|null[4|0.7579[5|1.17135[6|0
[1|CN[2|2[3|null[4|1.17135[5|1.5848[6|89
[1|CN[2|3[3|null[4|1.5848[5|1.99825[6|199
[1|CN[2|4[3|null[4|1.99825[5|2.4117[6|10
[1|Other[2|0[3|null[4|0.34445[5|0.7579[6|1
[1|Other[2|1[3|null[4|0.7579[5|1.17135[6|7
[1|Other[2|2[3|null[4|1.17135[5|1.5848[6|80
[1|Other[2|3[3|null[4|1.5848[5|1.99825[6|51
[1|Other[2|4[3|null[4|1.99825[5|2.4117[6|7
--- [0|Column names ---
[1|grouping [2|id [3|val [4|minval [5|maxval [6|totalsum



'''

class highchartsbasiccolumn(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'enumerations' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No enumerations argument ")
        enumerations = re.split(',',str(dictargs['enumerations']))
        enumerations = [x for x in enumerations if x] # remove nulls elements of the list

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        mydata = [x for x in c]
        categoriesList = []

        for c in mydata:
            if c[2] is not None and int(c[1])==len(categoriesList):
                categoriesList.append(str(c[2]))
            elif c[2] is  None and int(c[1])==len(categoriesList):
                categoriesList.append(str(c[4]) + '-' + str(c[3]))

        mydatajson = []

        if len(enumerations)==1 and enumerations[0] =='None':
            mydata2 =[]
            for i in xrange(len(categoriesList)):
                for c in mydata:
                    if  int(c[1])==i:
                        mydata2.append(int(c[5]))

            mydatajson = [{
                "name" : "All",
                "data" : mydata2
            }]

        else:
            for name in enumerations: #AD,Other
                mydata2 =[]
                for i in xrange(len(categoriesList)):
                    for c in mydata:
                        if str(c[0])== name and int(c[1])==i:
                            mydata2.append(int(c[5]))
                mydatajson.append({
                    "name" : name,
                    "data" : mydata2
                })
        a = '<span style="font-size:10px">{point.key}</span><table>'
        b = '<tr><td style="color:{series.color};padding:0">{series.name}: </td><td style="padding:0"><b>{point.y:.1f} mm</b></td></tr>'
        myresult =  {
            "type" : "application/vnd.highcharts+json",
            "data" : [{ "chart" : { "type": "column" },
                       "title" : { "text": str(dictargs['title']) },
                       "xAxis" : { "categories": categoriesList, "crosshair": True },
                       "yAxis":  { "min": 0, "title": { "text": str(dictargs['ytitle']) }},

                       "tooltip": { "headerFormat": a,
                                    "pointFormat":  b},

                       "plotOptions": { "column": { "pointPadding": 0.2, "borderWidth": 0} },
                       "series": mydatajson
            }]
        }

        myjsonresult = json.dumps(myresult)
        yield [('highchartresult',)]
        yield (myjsonresult,)

def Source():
    return functions.vtable.vtbase.VTGenerator(highchartsbasiccolumn)


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