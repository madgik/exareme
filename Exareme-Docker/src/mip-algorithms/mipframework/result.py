from .loggingutils import logged


class AlgorithmResult(object):
    def __init__(self, raw_data, tables=None, highcharts=None):
        self.data = raw_data
        self.tables = tables
        self.highcharts = highcharts

    @logged
    def output(self):
        result = [{"type": "application/json", "data": self.data}]
        if self.tables:
            for table in self.tables:
                result.append(
                    {
                        "type": "application/vnd.dataresource+json",
                        "data": table.render(),
                    }
                )
        if self.highcharts:
            for hc in self.highcharts:
                result.append(
                    {"type": "application/vnd.highcharts+json", "data": hc.render()}
                )
        return {"result": result}


class TabularDataResource(object):
    def __init__(self, fields, data, title):
        self.fields = fields
        self.data = data
        self.title = title

    @logged
    def render(self):
        header = []
        for datum, name in zip(self.data[0], self.fields):
            dt = "string" if type(datum) == str else "number"
            header.append({"name": name, "type": dt})
        return {
            "name": self.title,
            "profile": "tabular-data-resource",
            "data": self.data,
            "schema": {"fields": header},
        }
