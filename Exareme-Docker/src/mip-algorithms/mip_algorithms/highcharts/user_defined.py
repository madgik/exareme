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
