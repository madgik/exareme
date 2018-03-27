__docformat__ = 'reStructuredText en'

class histogramresultsviewer:
    # input colname0 id0 minvalue0 maxvalue0 colname1 id1 val total

    import itertools
    registered = True #Value to define db operator

    def __init__(self):
        self.n = 0
        self.myhist = dict()
        self.buckets =dict()
        self.column2names=dict()

    def step(self, *args):
        if self.n == 0:
            self.column1name = str(args[0])
            self.column2name = str(args[4])
        try:
            if args[5] == None:
                self.myhist[int(args[1])] = int(args[7])
            else:
                self.myhist[int(args[1]),int(args[5])] = int(args[7])
                self.column2names[int(args[5])] = str(args[6])
            self.buckets[int(args[1])]= str(str(args[2])+" - " + str(args[3]))
            self.n += 1

        except (ValueError, TypeError):
            raise

    def final(self):
        yield ('highchartresult',)
        print self.n
        if self.n > 0:
            if self.column2name != 'None':
                myresult =  "{\"chart\": { \"type\": \"column\"},\
                             \"title\": { \"text\": \"Histogram\"},\
                             \"subtitle\": {\"text\": \" " + self.column1name + " - " + self.column2name + " \"}, \
                             \"xAxis\": { \"categories\": ["
                for i in xrange(len(self.buckets)):
                    myresult += "\"" + self.buckets[i] +"\""
                    if i< len(self.buckets)-1:
                        myresult +=","

                myresult += " ],\"crosshair\": true},\
                                \"yAxis\": { \"min\": 0, \"title\": { \"text\": \"Number of Participants\" } },\
                                 \"tooltip\": { \"headerFormat\": \"<span style='font-size:10px'>{point.key}</span><table>\",\
                                           \"pointFormat\": \"<tr><td style='color:{series.color};padding:0'>{series.name}: </td><td style='padding:0'><b>{point.y:.0f} </b></td></tr>\",\
                                           \"footerFormat\":\"</table>\",\
                                           \"shared\": true,\
                                           \"useHTML\": true},\
                                \"plotOptions\": { \"column\": { \"pointPadding\": 0.2, \"borderWidth\": 0 }}, \
                                \"series\": [ "
                for i in xrange(len(self.column2names)):
                    myresult += "{ \"name\": \" "+ self.column2names[i]+" \", \"data\": ["
                    for j in xrange(len(self.buckets)):
                        myresult +=  str(self.myhist[j,i])
                        if j<len(self.buckets)-1:
                            myresult+=","
                    myresult += "]}"
                    if i<len(self.column2names)-1:
                        myresult+=","
                myresult+="]}"
            else:
                myresult =  "{\"chart\": { \"type\": \"column\"},\
                             \"title\": { \"text\": \"Histogram\"},\
                             \"subtitle\": {\"text\": \" " + self.column1name + " \"}, \
                             \"xAxis\": { \"categories\": ["
                for i in xrange(len(self.buckets)):
                    myresult += "\"" + self.buckets[i] +"\""
                    if i< len(self.buckets)-1:
                        myresult +=","

                myresult += " ],\"crosshair\": true},\
                                \"yAxis\": { \"min\": 0, \"title\": { \"text\": \"Number of Participants\" } },\
                                \"tooltip\": { \"headerFormat\": \"<span style='font-size:10px'>{point.key}</span><table>\",\
                                           \"pointFormat\": \"<tr><td style='color:{series.color};padding:0'>{series.name}: </td><td style='padding:0'><b>{point.y:.0f} </b></td></tr>\",\
                                           \"footerFormat\":\"</table>\",\
                                           \"shared\": true,\
                                           \"useHTML\": true},\
                                \"plotOptions\": { \"column\": { \"pointPadding\": 0.2, \"borderWidth\": 0 }}, \
                                \"series\": [ "

                myresult += "{ \"name\": \" "+ self.column1name +" \" , \"data\": ["
                for j in xrange(len(self.buckets)):
                    myresult +=  str(self.myhist[j])
                    if j<len(self.buckets)-1:
                        myresult+=","
                myresult += "]}]}"
        else:

            myresult =  "{\"chart\": { \"type\": \"column\"},\
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