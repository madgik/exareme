import json

__all__ = [
    'Highchart',
    'Chart',
    'BarChart',
    'BubbleChart',
    'ColumnChart',
    'ErrorbarChart',
    'HeatmapChart',
    'LineChart',
    'ScatterChart',
    'Series',
    'Title',
    'Axis'
]


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
                raise ValueError('Unrecognized field {}.'.format(name))

    def set(self, **kwargs):
        assert len(kwargs) == 1, 'Add one item at the time.'
        name, value = kwargs.popitem()
        if name in self._fields:
            if isinstance(value, ListField):
                setattr(self, name, RenderableList([value]))
            else:
                setattr(self, name, value)
            return self
        else:
            raise ValueError('Unrecognized field {}.'.format(name))

    def render(self):
        return {name: value.render() if isinstance(value, (Field, RenderableList)) else value
                for name, value in self.__dict__.items()}

    def render_to_json(self, indent=4):
        return json.dumps(self.render(), indent=indent)


class ListField(Field):
    def add(self, **kwargs):
        assert len(kwargs) == 1, 'Add one item at the time.'
        name, value = kwargs.popitem()
        setattr(self, name, getattr(self, name).append(value))


class Highchart(Field):
    _fields = ['chart', 'series', 'subtitle', 'title', 'xAxis', 'yAxis', 'zAxis', 'colorAxis']


class Chart(Field):
    _fields = ['type']


class BarChart(Highchart):
    def __init__(self, **kwargs):
        if 'chart' in kwargs.keys():
            raise KeyError('Cannot set chart type in subclasses of Highchart.')
        self.chart = Chart(type='bar')
        super(BarChart, self).__init__(**kwargs)


class BubbleChart(Highchart):
    def __init__(self, **kwargs):
        if 'chart' in kwargs.keys():
            raise KeyError('Cannot set chart type in subclasses of Highchart.')
        self.chart = Chart(type='bubble')
        super(BubbleChart, self).__init__(**kwargs)


class ColumnChart(Highchart):
    def __init__(self, **kwargs):
        if 'chart' in kwargs.keys():
            raise KeyError('Cannot set chart type in subclasses of Highchart.')
        self.chart = Chart(type='column')
        super(ColumnChart, self).__init__(**kwargs)


class ErrorbarChart(Highchart):
    def __init__(self, **kwargs):
        if 'chart' in kwargs.keys():
            raise KeyError('Cannot set chart type in subclasses of Highchart.')
        self.chart = Chart(type='errorbar')
        super(ErrorbarChart, self).__init__(**kwargs)


class HeatmapChart(Highchart):
    def __init__(self, **kwargs):
        if 'chart' in kwargs.keys():
            raise KeyError('Cannot set chart type in subclasses of Highchart.')
        self.chart = Chart(type='heatmap')
        super(HeatmapChart, self).__init__(**kwargs)


class LineChart(Highchart):
    def __init__(self, **kwargs):
        if 'chart' in kwargs.keys():
            raise KeyError('Cannot set chart type in subclasses of Highchart.')
        self.chart = Chart(type='line')
        super(LineChart, self).__init__(**kwargs)


class ScatterChart(Highchart):
    def __init__(self, **kwargs):
        if 'chart' in kwargs.keys():
            raise KeyError('Cannot set chart type in subclasses of Highchart.')
        self.chart = Chart(type='bar')
        super(ScatterChart, self).__init__(**kwargs)


class Series(ListField):
    _fields = ['type', 'data']


class Title(Field):
    _fields = ['text']


class Axis(Field):
    _fields = ['categories', 'max', 'min', 'title']


class ColorAxis(Field):
    _fields = ['min', 'max', 'minColor', 'maxColor']


class HighchartTemplate(object):
    chart = None

    def render(self):
        return self.chart.render()

    def render_to_json(self, indent=4):
        return self.chart.render_to_json(indent=indent)


class Heatmap(HighchartTemplate):
    def __init__(self, title, matrix, min, max, xnames=None, ynames=None):
        heatmap_data = []
        for i in range(len(matrix)):
            for j in range(len(matrix[0])):
                heatmap_data.append([i, j, matrix[i][j]])
        self.chart = HeatmapChart(title=Title(text=title)) \
            .set(xAxis=Axis(categories=xnames)) \
            .set(yAxis=Axis(categories=ynames)) \
            .set(colorAxis=ColorAxis(min=min, max=max, minColor='#ff0000', maxColor='#0000ff')) \
            .set(series=Series(data=heatmap_data))
