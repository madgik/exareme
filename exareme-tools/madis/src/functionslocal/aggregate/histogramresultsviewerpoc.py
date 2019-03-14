from array import *

try:
    from collections import OrderedDict
except ImportError:
    # Python 2.6
    from lib.collections26 import OrderedDict

__docformat__ = 'reStructuredText en'


class histogramresultsviewerpoc:
    # input colname0 id0 minvalue0 maxvalue0 colname1 id1 val total

    registered = True  # Value to define db operator

    def __init__(self):
        self.n = 0
        self.myhist = dict()
        self.buckets = dict()
        self.column2names = dict()

    def step(self, *args):
        if self.n == 0:
            self.column1name = str(args[0])
            self.column2name = str(args[4])
        try:
            if args[5] == None:
                self.myhist[int(args[1])] = int(args[7])
            else:
                self.myhist[int(args[1]), int(args[5])] = int(args[7])
                self.column2names[int(args[5])] = str(args[6])
            if str(args[2]) == str(args[3]):
                self.buckets[int(args[1])] = str(args[2])
            else:
                self.buckets[int(args[1])] = str(str(args[2]) + " - " + str(args[3]))
            self.n += 1
        except (ValueError, TypeError):
            raise

    def final(self):
        yield ('highchartresult',)
        if self.n > 0:
            if self.column2name != 'None':
                myresult = "{\"chart\": { \"type\": \"column\"},\
                             \"title\": { \"text\": \"Histogram\"},\
                             \"subtitle\": {\"text\": \" " + self.column1name + " - " + self.column2name + " \"}, \
                             \"xAxis\": { \"categories\": ["
                for key in sorted(self.buckets):
                    myresult += "\"" + self.buckets[key] + "\"" + ","
                myresult = myresult[0:-1]

                myresult += " ],\"crosshair\": true},\
                                \"yAxis\": { \"min\": 0, \"title\": { \"text\": \"Number of Participants\" } },\
                                 \"tooltip\": { \"headerFormat\": \"<span style='font-size:10px'>{point.key}</span><table>\",\
                                           \"pointFormat\": \"<tr><td style='color:{series.color};padding:0'>{series.name}: </td><td style='padding:0'><b>{point.y:.0f} </b></td></tr>\",\
                                           \"footerFormat\":\"</table>\",\
                                           \"shared\": true,\
                                           \"useHTML\": true},\
                                \"plotOptions\": { \"column\": { \"pointPadding\": 0.2, \"borderWidth\": 0 }}, \
                                \"series\": [ "

                for key1 in sorted(self.column2names):
                    myresult += "{ \"name\": \" " + self.column2names[key1] + " \", \"data\": ["
                    for key in sorted(self.buckets):
                        myresult += str(self.myhist[key, key1]) + ","
                    myresult = myresult[0:-1]

                    myresult += "]},"
                myresult = myresult[0:-1]
                myresult += "]}"
            else:
                myresult = "{\"chart\": { \"type\": \"column\"},\
                             \"title\": { \"text\": \"Histogram\"},\
                             \"subtitle\": {\"text\": \" " + self.column1name + " \"}, \
                             \"xAxis\": { \"categories\": ["

                for key in sorted(self.buckets):
                    myresult += "\"" + self.buckets[key] + "\"" + ","
                myresult = myresult[0:-1]

                myresult += " ],\"crosshair\": true},\
                                \"yAxis\": { \"min\": 0, \"title\": { \"text\": \"Number of Participants\" } },\
                                \"tooltip\": { \"headerFormat\": \"<span style='font-size:10px'>{point.key}</span><table>\",\
                                           \"pointFormat\": \"<tr><td style='color:{series.color};padding:0'>{series.name}: </td><td style='padding:0'><b>{point.y:.0f} </b></td></tr>\",\
                                           \"footerFormat\":\"</table>\",\
                                           \"shared\": true,\
                                           \"useHTML\": true},\
                                \"plotOptions\": { \"column\": { \"pointPadding\": 0.2, \"borderWidth\": 0 }}, \
                                \"series\": [ "

                myresult += "{ \"name\": \" " + self.column1name + " \" , \"data\": ["
                for key in sorted(self.buckets):
                    myresult += str(self.myhist[key]) + ","
                myresult = myresult[0:-1]
                myresult += "]}]}"
        else:
            myresult = "{\"chart\": { \"type\": \"column\"},\
                             \"title\": { \"text\": \"Histogram\"},\
                              \"xAxis\": { \"categories\": [],\"crosshair\": true},\
                                \"yAxis\": { \"min\": 0, \"title\": { \"text\": \"Number of Participants\" } },\
                                 \"tooltip\": { \"headerFormat\": \"<span style='font-size:10px'>{point.key}</span><table>\",\
                                           \"pointFormat\": \"<tr><td style='color:{series.color};padding:0'>{series.name}: </td><td style='padding:0'><b>{point.y:.0f} </b></td></tr>\",\
                                           \"footerFormat\":\"</table>\",\
                                           \"shared\": true,\
                                           \"useHTML\": true},\
                                \"plotOptions\": { \"column\": { \"pointPadding\": 0.2, \"borderWidth\": 0 }}, \
                                \"series\": []}"

        yield (myresult,)


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    from functions import *

    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest

        doctest.testmod()
