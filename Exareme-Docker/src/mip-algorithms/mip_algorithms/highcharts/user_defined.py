from mip_algorithms.highcharts.core import Heatmap_, Title, Axis, ColorAxis, Series


class HighchartTemplate(object):
    chart = None

    def render(self):
        return self.chart.render()

    def render_to_json(self, indent=4):
        return self.chart.render_to_json(indent=indent)


class CorrelationHeatmap(HighchartTemplate):
    def __init__(self, title, matrix, min, max, xnames=None, ynames=None):
        heatmap_data = []
        for i in range(len(matrix)):
            for j in range(len(matrix[0])):
                heatmap_data.append([i, j, matrix[i][j]])
        self.chart = Heatmap_(title=Title(text=title)) \
            .set(xAxis=Axis(categories=xnames)) \
            .set(yAxis=Axis(categories=ynames)) \
            .set(colorAxis=ColorAxis(min=min, max=max, minColor='#ff0000', maxColor='#0000ff')) \
            .set(series=Series(data=heatmap_data))


# TODO:
        # Highchart ROC
        # highchart_roc = {
        #     "chart"  : {
        #         "type"    : "area",
        #         "zoomType": "xy"
        #     },
        #     "title"  : {
        #         "text": "ROC"
        #     },
        #     "xAxis"  : {
        #         "min"  : -0.05,
        #         "max"  : 1.05,
        #         "title": {
        #             "text": "False Positive Rate"
        #         }
        #     },
        #     "yAxis"  : {
        #         "title": {
        #             "text": "True Positive Rate"
        #         }
        #     },
        #     "legend" : {
        #         "enabled": False
        #     },
        #     "series" : [{
        #         "useHTML": True,
        #         "name"   : "AUC " + str(AUC) + "<br/>Gini Coefficient " + str(gini),
        #         "label"  : {
        #             "onArea": True
        #         },
        #         "data"   : list(zip(FP_rate, TP_rate))
        #     }],
        #     'tooltip': {
        #         'enabled'     : True,
        #         'headerFormat': '',
        #         'pointFormat' : '{point.x}, {point.y}'
        #     }
        # }
        # Highchart confusion matrix
        # highchart_conf_matr = {
        #
        #     "chart"    : {
        #         "type": "heatmap",
        #
        #     },
        #     "title"    : {
        #         "useHTML": True,
        #         "text"   : "Confusion Matrix<br/><center><font size='2'>Binary categories: TODO<br/>" +
        #                    "</font></center>"
        #     },
        #     "xAxis"    : {
        #         "categories": ["Condition Positives", "Condition Negatives"]
        #     },
        #     "yAxis"    : {
        #         "categories": ["Prediction Negatives", "Prediction Positives"],
        #         "title"     : "null"
        #     },
        #     "colorAxis": {
        #         "min"     : 0,
        #         "minColor": "#FFFFFF",
        #         "maxColor": "#6699ff"
        #     },
        #     "legend"   : {
        #         "enabled": False,
        #     },
        #     "tooltip"  : {
        #         "enabled": False
        #     },
        #     "series"   : [{
        #         "dataLabels" : [{
        #             "format" : '{point.name}: {point.value}',
        #             "enabled": True,
        #             "color"  : '#333333'
        #         }],
        #         "name"       : 'Confusion Matrix',
        #         "borderWidth": 1,
        #         "data"       : [{
        #             "name" : 'True Positives',
        #             "x"    : 0,
        #             "y"    : 1,
        #             "value": TP
        #         }, {
        #             "name" : 'False Positives',
        #             "x"    : 1,
        #             "y"    : 1,
        #             "value": FP
        #         }, {
        #             "name" : 'False Negatives',
        #             "x"    : 0,
        #             "y"    : 0,
        #             "value": FN
        #         }, {
        #             "name" : 'True Negatives',
        #             "x"    : 1,
        #             "y"    : 0,
        #             "value": TN
        #         }]
        #     }]
        # }
