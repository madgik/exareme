from .core import Heatmap_, Area_, Title, Axis, ColorAxis, Series, Legend, DataLabels, Label


class HighchartTemplate(object):
    chart = None

    def render(self):
        return self.chart.render()

    def render_to_json(self, indent=4):
        return self.chart.render_to_json(indent=indent)


class CorrelationHeatmap(HighchartTemplate):
    def __init__(self, title, matrix, min_val, max_val, xnames=None, ynames=None):
        heatmap_data = []
        for i in range(len(matrix)):
            for j in range(len(matrix[0])):
                heatmap_data.append([i, j, matrix[i][j]])
        self.chart = Heatmap_(title=Title(text=title)) \
            .set(xAxis=Axis(categories=xnames)) \
            .set(yAxis=Axis(categories=ynames)) \
            .set(colorAxis=ColorAxis(min=min_val, max=max_val, minColor='#ff0000', maxColor='#0000ff')) \
            .set(series=Series(data=heatmap_data))


class ConfusionMatrix(HighchartTemplate):
    def __init__(self, title, confusion_matrix):
        assert type(confusion_matrix) == dict, 'Expecting a dictionary with keys: TP, FP, FN, TN'
        min_val = 0
        max_val = max(confusion_matrix.values())
        data = [{
            "name" : 'True Positives',
            "x"    : 0,
            "y"    : 1,
            "value": confusion_matrix['TP']
        }, {
            "name" : 'False Positives',
            "x"    : 1,
            "y"    : 1,
            "value": confusion_matrix['FP']
        }, {
            "name" : 'False Negatives',
            "x"    : 0,
            "y"    : 0,
            "value": confusion_matrix['FN']
        }, {
            "name" : 'True Negatives',
            "x"    : 1,
            "y"    : 0,
            "value": confusion_matrix['TN']
        }]
        dataLables = DataLabels(format='{point.name}: {point.value}', enabled=True, color='#333333')
        self.chart = Heatmap_(title=Title(text=title)) \
            .set(xAxis=Axis(categories=["Condition Positives", "Condition Negatives"])) \
            .set(yAxis=Axis(categories=["Prediction Negatives", "Prediction Positives"], title=None)) \
            .set(colorAxis=ColorAxis(min=min_val, max=max_val, minColor='#ffffff', maxColor='#0000ff')) \
            .set(series=Series(data=data, borderWidth=1, dataLabels=dataLables)) \
            .set(legend=Legend(enabled=False))


class ROC(HighchartTemplate):
    def __init__(self, title, roc_curve, auc, gini):
        self.chart = Area_(title=Title(text=title)) \
            .set(xAxis=Axis(min=-0.05, max=1.05, title=Title(text='False Positive Rate'))) \
            .set(yAxis=Axis(min=-0.05, max=1.05, title=Title(text='True Positive Rate'))) \
            .set(series=Series(data=roc_curve, useHTML=True, label=Label(onArea=True),
                               name="AUC " + str(auc) + "<br/>Gini Coefficient " + str(gini))) \
            .set(legend=Legend(enabled=False))
