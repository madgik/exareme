import setpath
import functions
import json
registered=True

'''
Highcharts.chart('container',
      { "chart": {"type": "heatmap","marginTop": 40,"marginBottom": 80,"plotBorderWidth": 1},
                  "title": {"text": " confusion matrix "},
                  "xAxis": {"title": { "text": " actual values "},"categories": [ "AD","CN","Other"]},
                  "yAxis": {"title": { "text": " predicted values "},"categories": [ "AD", "CN", "Other"]},
                  "colorAxis": {"min": 0,"minColor": "#FFFFFF","maxColor": "#6699ff"},
                  "legend": {"align": "right","layout": "vertical","margin": 0,"verticalAlign": "top","y": 25,"symbolHeight": 280},
                  "series": [{ "borderWidth": 1, "data": [  [ 0, 0, 46],
                                                            [ 0, 1, 39],
                                                            [ 0, 2, 0],
                                                            [ 1,  0, 20],
                                                            [ 1, 1,76],
                                                            [ 1, 2, 0],
                                                            [2, 0, 26],
                                                            [ 2, 1,33],
                                                            [2, 2,0]],
                  "dataLabels": {"enabled": true,"color": "#000000" }}]}
);
'''
class highchartheatmap(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']
        if 'title' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No title argument ")
        if 'xtitle' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No xtitle argument ")
        if 'ytitle' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No ytitle argument ")

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        mydata = []
        xcategories = []
        ycategories = []

        for myrow in c:
            if str(myrow[0]) not in xcategories:
                xcategories.append(str(myrow[0]))
            if str(myrow[1]) not in ycategories:
                ycategories.append(str(myrow[1]))
            mydata.append([xcategories.index(str(myrow[0])), ycategories.index(str(myrow[1])), float(myrow[2])])

        myresult =  {
                "type" : "application/vnd.highcharts+json",
                "data" :{ "chart": {"type": "heatmap","marginTop": 40,"marginBottom": 80,"plotBorderWidth": 1},
                            "title": {"text": str(dictargs['title'])},
                            "xAxis": {"title": { "text":str(dictargs['xtitle'])},"categories": xcategories},
                            "yAxis": {"title": { "text":str(dictargs['ytitle'])},"categories": ycategories},
                            "colorAxis": {"min": 0,"minColor": "#FFFFFF","maxColor": "#6699ff"},
                            "legend": {"align": "right","layout": "vertical","margin": 0,"verticalAlign": "top","y": 25,"symbolHeight": 280},
                            "series": [{ "borderWidth": 1, "data": mydata,
                            "dataLabels": {"enabled": True,"color": "#000000" }}]
                         }
            }
        myjsonresult = json.dumps(myresult)
        yield [('highchartresult',)]
        yield (myjsonresult,)


def Source():
    return functions.vtable.vtbase.VTGenerator(highchartheatmap)


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
