class linearregressionresultsviewer:
    registered = True  # Value to define db operator

    def __init__(self):
        self.n = 0
        self.mydata = dict()
        self.variablenames = []

    def step(self, *args):
        # if self.n == 0:
        #     print args, len(args)
        # self.noofvariables = args[4]
        # self.noofclusters = args[5]
        try:
            self.variablenames.append(str(args[0]))
            self.mydata[(args[0])] = str(args[1]), str(args[2]), str(args[3]), str(args[4])
            self.n += 1
            # if self.n <= self.noofvariables :
            #     self.variablenames.append(str(args[1]))
        except (ValueError, TypeError):
            raise

    def final(self):
        yield ('linearregressionresult',)

        myresult = "{\"resources\": [{\"name\": \"linear-regression\", \"profile\": \"tabular-data-resource\", \
                   \"data\": [[\"variable\", \"estimate\", \"standard_error\", \"t-value\", \"p-value\"]"
        if len(self.variablenames) != 0:
            myresult += ","
        for i in xrange(len(self.variablenames)):
            myresult += "[\"" + str(self.variablenames[i]) + "\","
            # row=[]
            # row.append(self.variablenames[i])
            for j in xrange(4):
                myresult += "\"" + str(self.mydata[(self.variablenames[i])][j]) + "\""
                if j < 3:
                    myresult += ","
                    # row.append(self.mydata[(self.variablenames[i])][j])
            # myresult+= str(row)
            if i < len(self.variablenames) - 1:
                myresult += "],"

        if len(self.variablenames) != 0:
            myresult += "]"

        myresult += "],\"schema\":  { \"fields\": [{\"name\": \"variable\", \"type\": \"string\"}, \
                  {\"name\": \"estimate\", \"type\": \"number\"},{\"name\": \"standard_error\", \"type\": \"number\"}, \
                  {\"name\": \"t-value\", \"type\": \"number\"}, {\"name\": \"p-value\", \"type\": \"string\"}] } }]}"

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
