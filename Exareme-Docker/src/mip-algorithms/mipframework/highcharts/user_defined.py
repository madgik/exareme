from .core import Heatmap_, Area_, Column_, Bubble_, Line_
from .core import (
    Title,
    Axis,
    ColorAxis,
    Series,
    Legend,
    DataLabels,
    Label,
    RenderableList,
)


class HighchartTemplate(object):
    chart = None

    def render(self):
        return self.chart.render()

    def render_to_json(self, indent=4):
        return self.chart.render_to_json(indent=indent)

    def __str__(self):
        return str(self.render_to_json())

    def __repr__(self):
        cls = type(self)
        return "{}()".format(cls.__name__)


class CorrelationHeatmap(HighchartTemplate):
    def __init__(self, title, matrix, min_val, max_val, xnames=None, ynames=None):
        heatmap_data = []
        for i in range(len(matrix)):
            for j in range(len(matrix[0])):
                heatmap_data.append([i, j, matrix[i][j]])
        self.chart = (
            Heatmap_(title=Title(text=title))
            .set(xAxis=Axis(categories=xnames))
            .set(yAxis=Axis(categories=ynames))
            .set(
                colorAxis=ColorAxis(
                    min=min_val, max=max_val, minColor="#ff0000", maxColor="#0000ff"
                )
            )
            .set(series=Series(data=heatmap_data))
        )


class ConfusionMatrix(HighchartTemplate):
    def __init__(self, title, confusion_matrix):
        assert type(confusion_matrix) == dict, "Expecting dictionary"
        min_val = 0
        max_val = max(confusion_matrix.values())
        data = [
            {
                "name": "True Positives",
                "x": 0,
                "y": 1,
                "value": confusion_matrix["True Positives"],
            },
            {
                "name": "False Positives",
                "x": 1,
                "y": 1,
                "value": confusion_matrix["False Positives"],
            },
            {
                "name": "False Negatives",
                "x": 0,
                "y": 0,
                "value": confusion_matrix["False Negatives"],
            },
            {
                "name": "True Negatives",
                "x": 1,
                "y": 0,
                "value": confusion_matrix["True Negatives"],
            },
        ]
        dataLables = DataLabels(
            format="{point.name}: {point.value}", enabled=True, color="#333333"
        )
        self.chart = (
            Heatmap_(title=Title(text=title))
            .set(xAxis=Axis(categories=["Condition Positives", "Condition Negatives"]))
            .set(
                yAxis=Axis(
                    categories=["Prediction Negatives", "Prediction Positives"],
                    title=None,
                )
            )
            .set(
                colorAxis=ColorAxis(
                    min=min_val, max=max_val, minColor="#ffffff", maxColor="#0000ff"
                )
            )
            .set(series=Series(data=data, borderWidth=1, dataLabels=dataLables))
            .set(legend=Legend(enabled=False))
        )


class ROC(HighchartTemplate):
    def __init__(self, title, roc_curve, auc, gini):
        self.chart = (
            Area_(title=Title(text=title))
            .set(
                xAxis=Axis(min=-0.05, max=1.05, title=Title(text="False Positive Rate"))
            )
            .set(
                yAxis=Axis(min=-0.05, max=1.05, title=Title(text="True Positive Rate"))
            )
            .set(
                series=Series(
                    data=roc_curve,
                    useHTML=True,
                    label=Label(onArea=True),
                    name="AUC " + str(auc) + "<br/>Gini Coefficient " + str(gini),
                )
            )
            .set(legend=Legend(enabled=False))
        )


class ScreePlot(HighchartTemplate):
    def __init__(self, title, data, xtitle):
        self.chart = (
            Column_(title=Title(text=title))
            .set(
                xAxis=Axis(categories=list(range(len(data))), title=Title(text=xtitle))
            )
            .set(yAxis=Axis(title=Title(text=title)))
            .set(
                series=RenderableList(
                    [
                        Series(data=data, type="column"),
                        Series(data=data, type="line", color="#0A1E6E"),
                    ]
                )
            )
            .set(legend=Legend(enabled=False))
        )


class BubbleGridPlot(HighchartTemplate):
    def __init__(self, title, data, var_names, xtitle, ytitle):
        self.chart = (
            Bubble_(title=Title(text=title))
            .set(legend=Legend(enabled=False))
            .set(
                xAxis=Axis(
                    title=Title(text=xtitle),
                    gridLineWidth=1,
                    tickLength=500,
                    categories=list(range(len(data))),
                )
            )
            .set(
                yAxis=Axis(
                    title=Title(text=ytitle),
                    categories=var_names,
                    startOnTick=False,
                    endOnTick=False,
                    maxPadding=0.2,
                )
            )
            .set(
                colorAxis=ColorAxis(
                    min=0, max=max([abs(elem) for row in data for elem in row])
                )
            )
            .set(
                series=Series(
                    data=[
                        {"x": i, "y": j, "z": abs(elem)}
                        for i, row in enumerate(data)
                        for j, elem in enumerate(row)
                    ],
                    colorKey="z",
                )
            )
        )


class CalibrationBeltPlot(HighchartTemplate):
    def __init__(self, title, data, confidence_levels, e_name, o_name):
        self.chart = (
            Line_(title=Title(text=title))
            .set(xAxis=Axis(title=Title(text="EXPECTED({})".format(e_name))))
            .set(yAxis=Axis(title=Title(text="OBSERVED({})".format(o_name))))
            .set(
                series=RenderableList(
                    [
                        Series(
                            name="Confidence level " + str(confidence_levels[1]),
                            data=data[1],
                            type="arearange",
                            lineWidth=0,
                            linkedTo=":previous",
                            color="#a5b4c7",
                            zIndex=0,
                            marker={"enabled": False},
                        ),
                        Series(
                            name="Confidence level " + str(confidence_levels[0]),
                            data=data[0],
                            type="arearange",
                            lineWidth=0,
                            linkedTo=":previous",
                            color="#6e7d8f",
                            zIndex=0,
                            marker={"enabled": False},
                        ),
                        Series(
                            name="Bisector",
                            data=[[0, 0], [1, 1]],
                            zIndex=2,
                            color="#fc7938",
                            lineWidth=1.5,
                            dashStyle="Dash",
                            allowPointSelect=False,
                            marker={"enabled": False},
                            label={"enabled": False},
                        ),
                    ]
                )
            )
        )
