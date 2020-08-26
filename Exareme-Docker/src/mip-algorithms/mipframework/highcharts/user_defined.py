from colour import Color


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
    Tooltip,
    Annotations,
    LabelOptions,
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
                heatmap_data.append(
                    {
                        "y": i,
                        "x": j,
                        "value": round(matrix[i][j], 4),
                        "name": ynames[i] + " ~ " + xnames[j],
                    }
                )
        self.chart = (
            Heatmap_(title=Title(text=title))
            .set(xAxis=Axis(categories=xnames))
            .set(yAxis=Axis(categories=ynames))
            .set(
                colorAxis=ColorAxis(
                    min=min_val,
                    max=max_val,
                    minColor="#ff0000",
                    maxColor="#0000ff",
                    stops=[[0, "#c4463a"], [0.5, "#ffffff"], [0.9, "#3060cf"]],
                )
            )
            .set(series=Series(data=heatmap_data))
            .set(
                tooltip=Tooltip(
                    headerFormat="",
                    pointFormat="<b>{point.name}: {" "point.value}</b>",
                    enabled=True,
                )
            )
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
        data_labels = DataLabels(
            format="{point.name}: {point.value}",
            enabled=True,
            color="#222222",
            borderRadius=3,
            backgroundColor="rgba(252, 255, 197, 0.7)",
            borderWidth=2,
            borderColor="#AAA",
            padding=5,
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
            .set(series=Series(data=data, borderWidth=1, dataLabels=data_labels))
            .set(legend=Legend(enabled=False))
            .set(tooltip=Tooltip(enabled=False))
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
    def __init__(
        self,
        title,
        data,
        confidence_levels,
        e_name,
        o_name,
        model_deg,
        thres,
        p_val,
        n,
        cl1,
        under1,
        over1,
        cl2,
        under2,
        over2,
    ):
        self.chart = (
            Line_(title=Title(text=title))
            .set(xAxis=Axis(title=Title(text="EXPECTED ({})".format(e_name))))
            .set(yAxis=Axis(title=Title(text="OBSERVED ({})".format(o_name))))
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
            .set(
                annotations=RenderableList(
                    [
                        Annotations(
                            labels=RenderableList(
                                [
                                    Label(
                                        point={"x": 100, "y": 100},
                                        text="Polynomial degree: "
                                        + str(model_deg)
                                        + " <br/>Model selection significance level: "
                                        + str(thres)
                                        + " <br/>p-value: "
                                        + str(p_val)
                                        + " <br/>n: "
                                        + str(n),
                                        padding=10,
                                        shape="rect",
                                    )
                                ]
                            ),
                            labelOptions=LabelOptions(
                                borderRadius=5,
                                backgroundColor="#bbd9fa",
                                borderWidth=1,
                                borderColor="#9aa2ab",
                            ),
                        ),
                        Annotations(
                            labels=RenderableList(
                                [
                                    Label(
                                        point={"x": 800, "y": 600},
                                        text="Confidence level: "
                                        + str(cl1)
                                        + "<br/>Under the bisector: "
                                        + str(under1)
                                        + "<br/>Over the bisector: "
                                        + str(over1),
                                        padding=10,
                                        shape="rect",
                                    )
                                ]
                            ),
                            labelOptions=LabelOptions(
                                borderRadius=5,
                                backgroundColor="#6e7d8f",
                                borderWidth=1,
                                borderColor="#AAA",
                            ),
                        ),
                        Annotations(
                            labels=RenderableList(
                                [
                                    Label(
                                        point={"x": 1000, "y": 600},
                                        text="Confidence level: "
                                        + str(cl2)
                                        + "<br/>Under the bisector: "
                                        + str(under2)
                                        + "<br/>Over the bisector: "
                                        + str(over2),
                                        padding=10,
                                        shape="rect",
                                    )
                                ]
                            ),
                            labelOptions=LabelOptions(
                                borderRadius=5,
                                backgroundColor="#a5b4c7",
                                borderWidth=1,
                                borderColor="#AAA",
                            ),
                        ),
                    ]
                )
            )
        )


class SurvivalCurves:
    def __init__(
        self,
        timeline_dict,
        survival_function_dict,
        confidence_interval_dict,
        control_variable,
    ):
        global colors_dark, colors_light
        self.survival_function_dict = survival_function_dict
        self.timeline_dict = timeline_dict
        self.confidence_interval_dict = {}
        for key, ci in confidence_interval_dict.items():
            self.confidence_interval_dict[key] = [
                [x, y[0], y[1]] for x, y in zip(self.timeline_dict[key], ci)
            ]
        self.light_colors = {}
        for i, key in enumerate(self.timeline_dict.keys()):
            self.light_colors[key] = colors_light[i]
        self.dark_colors = {}
        for i, key in enumerate(self.timeline_dict.keys()):
            self.dark_colors[key] = colors_dark[i]
        self.control_variable = control_variable

    def render(self):
        return {
            "chart": {
                "type": "arearange",
                "zoomType": "x",
                "scrollablePlotArea": {"minWidth": 600, "scrollPositionX": 1},
            },
            "title": {"text": "Survival curve"},
            "xAxis": {"title": {"text": "Days since first visit"}},
            "plotOptions": {"arearange": {"step": "left"}},
            "yAxis": {"title": {"text": "Survival Probability"}},
            "tooltip": {"crosshairs": True, "shared": True,},
            "legend": {
                "align": "left",
                "verticalAlign": "middle",
                "layout": "vertical",
            },
            "series": [
                {
                    "data": list(self.confidence_interval_dict[key]),
                    "color": self.light_colors[key],
                    "marker": {"enabled": False},
                    "showInLegend": False,
                }
                for key in self.timeline_dict.keys()
            ]
            + [
                {
                    "type": "line",
                    "name": self.control_variable + ": " + str(key),
                    "step": True,
                    "data": zip(
                        self.timeline_dict[key], self.survival_function_dict[key]
                    ),
                    "color": self.dark_colors[key],
                    "marker": {"enabled": False},
                }
                for key in self.timeline_dict.keys()
            ],
        }


colors_dark = [
    "#7cb5ec",
    "#434348",
    "#90ed7d",
    "#f7a35c",
    "#8085e9",
    "#f15c80",
    "#e4d354",
    "#2b908f",
    "#f45b5b",
    "#91e8e1",
]
colors_light = [Color(c) for c in colors_dark]
for c in colors_light:
    c.luminance = (1 + c.luminance) / 2
colors_light = [c.get_hex() for c in colors_light]
