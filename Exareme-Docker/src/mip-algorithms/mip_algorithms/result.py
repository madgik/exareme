from mip_algorithms import logged


class AlgorithmResult(object):
    def __init__(self, raw_data, tables=None, highcharts=None):
        self.data = raw_data
        self.tables = tables
        self.highcharts = highcharts

    def output(self):
        result = [
            {
                "type": "application/json",
                "data": self.data
            }
        ]
        for table in self.tables:
            result.append({
                "type": "application/vnd.dataresource+json",
                "data": table
            })
        for hc in self.highcharts:
            result.append({
                "type": "application/vnd.highcharts+json",
                "data": hc
            })
        return {'result': result}


class TabularDataResource(object):
    @staticmethod
    @logged
    def make(fields, data, title):
        header = []
        for datum, name in zip(data[0], fields):
            dt = 'string' if type(datum) == str else 'number'
            header.append({
                "name": name,
                "type": dt
            })
        return {
            "name"   : title,
            "profile": "tabular-data-resource",
            "data"   : data,
            "schema" : {
                "fields": header
            }
        }


class HighChart(object):
    @classmethod
    @logged
    def correlation_heatmap(cls, correlation_matrix, x_axis_names, y_axis_names, title):
        data = []
        for i in xrange(len(y_axis_names)):
            for j in xrange(len(x_axis_names)):
                data.append({
                    'x'    : j,
                    'y'    : i,
                    'value': round(correlation_matrix[i, j], 4),
                    'name' : y_axis_names[i] + ' ~ ' + x_axis_names[j]
                })
        hc = {
            'chart'    : {
                'type'           : 'heatmap',
                'plotBorderWidth': 1
            },
            'title'    : {
                'text': title
            },
            'xAxis'    : {
                'categories': x_axis_names
            },
            'yAxis'    : {
                'categories': y_axis_names,
                'title'     : 'null'
            },
            'colorAxis': {
                'stops'   : [
                    [0, '#c4463a'],
                    [0.5, '#ffffff'],
                    [0.9, '#3060cf']
                ],
                'min'     : -1,
                'max'     : 1,
                'minColor': '#FFFFFF',
                'maxColor': "#6699ff"
            },
            'legend'   : {
                'align'        : 'right',
                'layout'       : 'vertical',
                'margin'       : 0,
                'verticalAlign': 'top',
                'y'            : 25,
                'symbolHeight' : 280
            },
            'tooltip'  : {
                'headerFormat': '',
                'pointFormat' : '<b>{point.name}: {point.value}</b>',
                'enabled'     : True
            },
            'series'   : [{
                'name'       : 'coefficients',
                'borderWidth': 1,
                'data'       : data,
                'dataLabels' : {
                    'enabled': True,
                    'color'  : '#000000'
                }
            }]
        }
        return hc