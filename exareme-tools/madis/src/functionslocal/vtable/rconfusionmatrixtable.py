"""

"""
import setpath
import vtbase
import functions

from rpy2.robjects.packages import importr
from rpy2.robjects import FloatVector


#sudo pip install rpy2==2.8.0
#install.packages('caret')
#install.packages("e1071")

from rpy2.robjects.packages import importr
import rpy2.robjects as robjects
from rpy2.robjects import StrVector

### Classic stream iterator
registered=True

class rconfusionmatrixtable(vtbase.VT): #predictedclass,actualclass,val
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        self.predictedclasses =[]
        self.actualclasses = []
        self.namesofclasses =[]

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument")
        query = dictargs['query']

        cur = envars['db'].cursor()
        c = cur.execute(query)

        for myrow in c:
            if myrow[0] not in self.namesofclasses:
                self.namesofclasses.append(myrow[0])

            for i in xrange(myrow[2]):
                self.predictedclasses.append(myrow[0])
                self.actualclasses.append(myrow[1])
            self.noclasses = myrow[3]

        #print self.predictedclasses
        # print self.actualclasses


        caret = importr('caret')
        e = importr('e1071')
        base = importr('base')

        #print 'Eleni'
        yield [('statscolname',), ('typestats',), ('statsval',)]
        if len(StrVector(self.predictedclasses))>0 and len(StrVector(self.actualclasses))>0 :
            Rresult = caret.confusionMatrix(base.factor(StrVector(self.predictedclasses)),
                                            base.factor(StrVector(self.actualclasses)))

            #Rresult[0] -->Positive Class
            if self.noclasses == 2:
               yield ("Positive Class", 'overall', Rresult[0][0])
            else:
               yield ("Positive Class", 'overall', '')

            #Rresult[1] -->Table (I have already computed this)

            #Rresult[2] -->overall statistics
            yield ("Accuracy", 'overall',Rresult[2][0])
            yield ("Kappa", 'overall', Rresult[2][1])
            yield ("AccuracyLower",'overall', Rresult[2][2])
            yield ("AccuracyUpper",'overall', Rresult[2][3])
            yield ("AccuracyNull",'overall', Rresult[2][4])
            yield ("AccuracyPValue",'overall', Rresult[2][5])
            yield ("McnemarPValue",'overall',Rresult[2][6])

            #Rresult[3] -->byClass statistics
            print self.namesofclasses
            for i in xrange(self.noclasses):
                print i
                yield ("Sensitivity", self.namesofclasses[i],Rresult[3][0 + i])
                yield ("Specificity",self.namesofclasses[i],Rresult[3][1*self.noclasses + i])
                yield ("Pos Pred Value",self.namesofclasses[i],Rresult[3][2*self.noclasses + i])
                yield ("Neg Pred Value",self.namesofclasses[i],Rresult[3][3*self.noclasses+ i ])
                yield ("Precision",self.namesofclasses[i],Rresult[3][4*self.noclasses+i])
                yield ("Recall",self.namesofclasses[i],Rresult[3][5*self.noclasses+i])
                yield ("F1",self.namesofclasses[i],Rresult[3][6*self.noclasses+i])
                yield ("Prevalence",self.namesofclasses[i],Rresult[3][7*self.noclasses+i])
                yield ("Detection Rate",self.namesofclasses[i],Rresult[3][8*self.noclasses+i])
                yield ("Detection Prevalence",self.namesofclasses[i],Rresult[3][9*self.noclasses+i])
                yield ("Balanced Accuracy",self.namesofclasses[i], Rresult[3][10*self.noclasses+i])

def Source():
    return vtbase.VTGenerator(rconfusionmatrixtable)

if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    import setpath
    from functions import *
    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest
        doctest.testmod()
