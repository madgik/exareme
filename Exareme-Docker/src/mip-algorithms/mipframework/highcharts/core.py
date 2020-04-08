import json


class RenderableList(list):
    def render(self):
        return [i.render() for i in self]


class Field(object):
    _fields = []

    def __init__(self, **kwargs):
        for name, value in kwargs.items():
            if name in self._fields:
                if isinstance(value, ListField):
                    setattr(self, name, RenderableList([value]))
                else:
                    setattr(self, name, value)
            else:
                raise ValueError("Unrecognized field {}.".format(name))

    def set(self, **kwargs):
        assert len(kwargs) == 1, "Add one item at the time."
        name, value = kwargs.popitem()
        if name in self._fields:
            if isinstance(value, ListField):
                setattr(self, name, RenderableList([value]))
            else:
                setattr(self, name, value)
            return self
        else:
            raise ValueError("Unrecognized field {}.".format(name))

    def render(self):
        return {
            name: value.render()
            if isinstance(value, (Field, RenderableList))
            else value
            for name, value in self.__dict__.items()
        }

    def render_to_json(self, indent=4):
        return json.dumps(self.render(), indent=indent)


class ListField(Field):
    def add(self, **kwargs):
        assert len(kwargs) == 1, "Add one item at the time."
        name, value = kwargs.popitem()
        setattr(self, name, getattr(self, name).append(value))


class Highchart(Field):
    _fields = [
        "chart",
        "series",
        "subtitle",
        "title",
        "xAxis",
        "yAxis",
        "zAxis",
        "colorAxis",
        "legend",
    ]


class Chart(Field):
    _fields = ["type", "zoomType", "plotBorderWidth"]


class Series(ListField):
    _fields = [
        "type",
        "data",
        "useHTML",
        "label",
        "dataLabels",
        "borderWidth",
        "name",
        "color",
        "colorKey",
    ]


class Legend(Field):
    _fields = ["enabled"]


class Label(Field):
    _fields = ["onArea"]


class DataLabels(ListField):
    _fields = ["format", "enabled", "color"]


class Title(Field):
    _fields = ["text", "useHTML"]


class Axis(Field):
    _fields = [
        "categories",
        "max",
        "min",
        "title",
        "gridLineWidth",
        "tickLength",
        "startOnTick",
        "endOnTick",
        "maxPadding",
    ]


class ColorAxis(Field):
    _fields = ["min", "max", "minColor", "maxColor"]


class PlotOptions(Field):
    _fields = []


class Area_(Highchart):
    def __init__(self, **kwargs):
        if "chart" in kwargs.keys():
            raise KeyError("Cannot set chart type in subclasses of Highchart.")
        self.chart = Chart(type="area", zoomType="xy")
        super(Area_, self).__init__(**kwargs)


class Bar_(Highchart):
    def __init__(self, **kwargs):
        if "chart" in kwargs.keys():
            raise KeyError("Cannot set chart type in subclasses of Highchart.")
        self.chart = Chart(type="bar")
        super(Bar_, self).__init__(**kwargs)


class Bubble_(Highchart):
    def __init__(self, **kwargs):
        if "chart" in kwargs.keys():
            raise KeyError("Cannot set chart type in subclasses of Highchart.")
        self.chart = Chart(type="bubble", plotBorderWidth=1, zoomType="xy")
        super(Bubble_, self).__init__(**kwargs)


class Column_(Highchart):
    def __init__(self, **kwargs):
        if "chart" in kwargs.keys():
            raise KeyError("Cannot set chart type in subclasses of Highchart.")
        self.chart = Chart(type="column")
        super(Column_, self).__init__(**kwargs)


class Errorbar_(Highchart):
    def __init__(self, **kwargs):
        if "chart" in kwargs.keys():
            raise KeyError("Cannot set chart type in subclasses of Highchart.")
        self.chart = Chart(type="errorbar")
        super(Errorbar_, self).__init__(**kwargs)


class Heatmap_(Highchart):
    def __init__(self, **kwargs):
        if "chart" in kwargs.keys():
            raise KeyError("Cannot set chart type in subclasses of Highchart.")
        self.chart = Chart(type="heatmap")
        super(Heatmap_, self).__init__(**kwargs)


class Line_(Highchart):
    def __init__(self, **kwargs):
        if "chart" in kwargs.keys():
            raise KeyError("Cannot set chart type in subclasses of Highchart.")
        self.chart = Chart(type="line")
        super(Line_, self).__init__(**kwargs)


class Scatter_(Highchart):
    def __init__(self, **kwargs):
        if "chart" in kwargs.keys():
            raise KeyError("Cannot set chart type in subclasses of Highchart.")
        self.chart = Chart(type="bar")
        super(Scatter_, self).__init__(**kwargs)
