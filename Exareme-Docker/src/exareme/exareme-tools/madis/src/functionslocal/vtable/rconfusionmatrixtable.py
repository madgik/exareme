"""

"""
import setpath
import functions
import json
import sys
from rpy2.robjects import StrVector
from rpy2.robjects.packages import importr
from rpy2.rinterface import RRuntimeError

import warnings
warnings.filterwarnings("ignore")

caret = importr('caret')
e = importr('e1071')
base = importr('base')

### Classic stream iterator
registered=True

class rconfusionmatrixtable(functions.vtable.vtbase.VT): #predictedclass,actualclass,val
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)
		
	if 'query' not in dictargs:
	    raise functions.OperatorError(__name__.rsplit('.')[-1], "No query argument")
	query = dictargs['query']
		
        cur = envars['db'].cursor()
        c = cur.execute(query)
		
        predictedclasses =[]
        actualclasses = []
        classnames = []
        for myrow in c:
            for i in xrange(myrow[2]):
                predictedclasses.append(myrow[0])
                actualclasses.append(myrow[1])
	    if myrow[0] not in classnames:
		classnames.append(myrow[0])
			
	numberofclassnames = len(classnames)
		
        print "Predicted vector:", predictedclasses
        print "Actual vector:", actualclasses

	#print (classnames)
	predictedData = base.factor(base.c(StrVector(predictedclasses)), base.c(StrVector(classnames)))
	truthData  = base.factor(base.c(StrVector(actualclasses)), base.c(StrVector(classnames)))
	Rresult = caret.confusionMatrix(predictedData,truthData)
	print 'Rresult[1]', Rresult[1]
	print 'Rresult[2]', Rresult[2]
	print 'Rresult[3]', Rresult[3]
		
	#####################################################
	dataOverall = []
	if numberofclassnames == 2:
	    dataOverall.append(["Positive Class",Rresult[0][0]])
	else:
	    dataOverall.append(["Positive Class",None])
	
	#Rresult[1] -->Table (I have already computed this)
	#Rresult[2] -->overall statistics
	dataOverall.append(["Accuracy",(Rresult[2][0])])
	dataOverall.append(["Kappa",(Rresult[2][1])])
	dataOverall.append(["AccuracyLower",(Rresult[2][2])])
	dataOverall.append(["AccuracyUpper",(Rresult[2][3])])
	dataOverall.append(["AccuracyNull",(Rresult[2][4])])
	dataOverall.append(["AccuracyPValue",(Rresult[2][5])])
	dataOverall.append(["McnemarPValue",(Rresult[2][6])])

	ResultOverall = { "data": {
				"profile": "tabular-data-resource",
				"data": dataOverall,
				"name": "Overall",
				"schema": {
				  "fields": [
					{
					  "type": "text",
					  "name": "StatisticName"
					},
					{
					  "type": "real",
					  "name": "Value"
					}
				  ]
				}
			  },
			  "type": "application/vnd.dataresource+json"
		}
	print "ResultOverall", ResultOverall
        #####################################################

	FieldClassNames =  [
		  { "type": "text",
			"name": "StatisticName" }]
        for i in range(len(classnames)):
	    FieldClassNames.append(
			  {
				"type": "real",
				"name": classnames[i] + " class"
			  })

	DataClassNames = [["Sensitivity"],["Specificity"],["Pos Pred Value"],["Neg Pred Value"],["Precision"],["Recall"],
		["F1"],["Prevalence"],["Detection Rate"],["Detection Prevalence"],["Balanced Accuracy"]]

	#Rresult[3] -->byClass statistics
	 
	i = 0
	for k in range(len(DataClassNames)):
	    for l in range(len(classnames)):
		if str(Rresult[3][i])!='nan' and str(Rresult[3][i])!='NA':
	            DataClassNames[k].append(Rresult[3][i])
		else:
		    DataClassNames[k].append(None)
		i = i + 1
				
	ResultClassNames = {
	"data": {
	    "profile": "tabular-data-resource",
	    "data": DataClassNames,
	    "name": "ClassNames",
	    "schema": {"fields": FieldClassNames}
		},
	"type": "application/vnd.dataresource+json"}
        
        print "resultClassNames", ResultClassNames 

        yield (['statscolname'],['statsval'],)

        a = json.dumps(ResultOverall)
        a = a.replace(' ','')
        yield ("ResultOverall" , a)

        b = json.dumps(ResultClassNames)
        b = b.replace(' ','')
        yield ("ResultClassNames",b)


def Source():
    return functions.vtable.vtbase.VTGenerator(rconfusionmatrixtable)

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
