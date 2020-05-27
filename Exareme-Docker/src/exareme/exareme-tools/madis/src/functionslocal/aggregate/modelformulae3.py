import functions
import itertools
import json
import re
from scipy import stats

from operator import itemgetter

__docformat__ = 'reStructuredText en'


# Example:
# select modelvariables('Country+Age+Diet+Country*Diet*Age-Country:Diet:Age+0','{"Country":["UK","USA"],"Diet":["A","B","C"],"Age":["young","old"]}');
#
# Based on https://thomasleeper.com/Rcourse/Tutorials/formulae.html
# Formula basics:
# Plus symbol (+).  It is used for seperating multiple independent variables :
# Minus symbol (-). Objects in the formula are ignored in an analysis
# Dot operator (.) .When used in a formula, it refers to all other variables in the matrix not yet included in the model. So, if we plan to run a regression
#                   on a matrix (or dataframe) containing the variables y, x1, z3, we can simply use the formula: y ~ . and avoid having to type all of the variables. -->TODO. NOT SUPPORTED YET
#Interaction terms
# Star/asterisk symbol (*).  It is used for interaction, which means that we want to include two variables and their interaction
# colon symbol (:).          It is used for interaction, which means that we want to include only the interaction between the two variables

# Drop intercept:  By either including a zero (0) in the formula

def simplified_formula(formula):
    formulaList = re.split('(\+|\-)',formula)
    formulaList=[x for x in formulaList if x] # remove nulls elements of the list
    if formulaList[0] != '-':
        formulaList.insert(0,'+')
    # print "formulaList",formulaList

    if "0" in formulaList:
        ind = formulaList.index("0")
        del formulaList[ind]
        del formulaList[ind-1]
        newschema1 = []
        # print "exist 0"
    else:
        newschema1 = ['intercept']
        if "1" in formulaList:
            ind = formulaList.index("1")
            del formulaList[ind]
            del formulaList[ind-1]
        # print "yes 1"

    removefromnewschema = []
    # Decompose formula so that it contains only +
    for f in xrange(len(formulaList)):
        # if formulaList[f]=='0':
        #     removefromnewschema.append('intercept')
        if ('*' in formulaList[f] or ':' in formulaList[f]):
            elements = re.split('\*',formulaList[f])
            elements = [x for x in elements if x] # remove nulls elements of the list
            if ('*' in formulaList[f]): k=1
            if (':' in formulaList[f]): k=2
            for L in range(1, len(elements)+1):
                for new_el in list(itertools.combinations(elements, L)):
                    new_el_str =""
                    new_el=[x for x in new_el]
                    for i in new_el:
                        new_el_str += str(i) + ":"
                    new_el_str = new_el_str[:-1]
                    # print str(k), new_el_str
                    if formulaList[f-1] == '+':
                        newschema1.append(new_el_str)
                    if formulaList[f-1] == '-':
                        removefromnewschema.append(new_el_str)
        else:
            if formulaList[f-1] == '+':
                newschema1.append(formulaList[f])
            if formulaList[f-1] == '-':
                removefromnewschema.append(formulaList[f])

    newschema2 = [x for x in newschema1 if x not in  removefromnewschema]
    #print newschema2

    return newschema2




def reorderFactors(schema):
    newschema =[]
    for i in xrange(0,max([x.count(":") for x in schema])+1):
        newschema += [x for x in schema if x.count(":")==i]
        print newschema
    return newschema



#It is used in Anova algorithm. It creates all the formulas that will be used as input to LR.
class create_simplified_formulas:

    registered = True

    def step(self, *args):
        self.formula = str(args[0])
        if len(args) ==2:
            self.type = int(args[1]) #Prepei na einai 1 or 2 or 3 . Epistrefei polles formules gia na treksoun se LR kai na ginei compute to anova
        else:
            self.type = 3; # Den paizei rolo to noumero. Epistrefei mono mia formula. Xrhsimopoieitai se LR
    def final(self):
        newschema = simplified_formula(self.formula)
        newschema2 = reorderFactors(newschema)

        # print newschema2
        yield ('no','formula')

        if self.type == 1:
            result = ""
            for i in xrange(len(newschema2)):
                result += "+" + str(newschema2[i])
                yield (str(i), result[1:],)
        elif self.type == 2:
            print "1", newschema2
            for i in xrange(len(newschema2)):
                result =""
                FactorA = re.split(':',str(newschema2[i]))
                FactorA= [x for x in FactorA if x] # remove nulls elements of the list
                print FactorA
                for j in xrange(len(newschema2)):
                    FactorB = re.split(':',str(newschema2[j]))
                    FactorB = [x for x in FactorB if x] # remove nulls elements of the list
                    print "B",FactorB
                    no = 0
                    for k in FactorA:
                        for l in FactorB:
                            if k==l:
                                no = no +1
                    if no != len(FactorA) :
                        result += "+" + str(newschema2[j])
                yield (str(i*2), result[1:],)
                yield (str(i*2+1), result[1:] + "+" + newschema2[i])

        elif self.type == 3:
            for i in xrange(len(newschema2)):
                result=""
                for j in xrange(len(newschema2)):
                    if i!=j:
                        result += "+" + str(newschema2[j])
                yield (str(i), result[1:],)

            result = ""
            for i in xrange(len(newschema2)):
                result += "+" + str(newschema2[i])
            yield (str(len(newschema2)), result[1:],)

        else: # Gia LR
            result = ""
            for i in xrange(len(newschema2)):
                result += "+" + str(newschema2[i])
            yield (str(0), result[1:],)


#dummy coding on the formula elements
class modelvariables:

    registered = True  # Value to define db operator

    def step(self, *args):
        self.formula = str(args[0])
        self.metadata = json.loads(args[1])
        # print "self.metadata", self.metadata

    def final(self):
        newschema2=simplified_formula(self.formula)
        newschema2 = list(set(newschema2)) #keep unique elements
        metadata = dict()
        referencevalue =dict()
        for pair in self.metadata:
            metadata[str(pair[0])]= re.split(',',str(pair[1]))
            referencevalue[str(pair[0])]= str(pair[2])
        newschema3 =[]
        # print "schema", newschema2
        # convert newschema2 to dummy variables
        for elements in newschema2:
            elements1 =elements
            elements = re.split(':',elements)
            new_elements = [] # einai mia lista apo listes
            for el in xrange(len(elements)):
                if elements[el] in metadata.keys():
                    new_el=[list(x) for x in list(itertools.product([elements[el]],metadata[elements[el]]))]
                    for x in new_el:
                        if x[1] == referencevalue[elements[el]]:
                            new_el.pop(new_el.index(x))
                    new_el =[str(x[0])+'('+str(x[1] +')') for x in new_el]
                    new_elements.append(new_el)
                else:
                     new_elements.append([elements[el]])
            if len(new_elements)==1:
                # print "AAAA",new_elements
                if isinstance(new_elements[0],list):
                    # print "ok"
                    for item in new_elements[0]:
                        newschema3.append([elements1,item])
                else:
                     newschema3.append([elements1,new_elements[0]])
                # print newschema3
            else:
                # print new_elements
                while len(new_elements)>1: #  Do Product operation when len(new_elements)>1
                    # print "BBB"
                    el1 = new_elements.pop(0)
                    el2 = new_elements.pop(0)
                    new_el=[list(x) for x in list(itertools.product(el1,el2))]
                    new_el =[str(x[0])+':'+str(x[1]) for x in new_el]
                    new_elements.insert(0,new_el) # einai mia lista apo listes
                for item in  new_elements[0]:
                    # print "CCCC"
                    newschema3.append([elements1,item])
        yield ('modelcolnames','modelcolnamesdummycodded',)

        for element in newschema3:
            yield (element[0], element[1] ,)


class sumofsquares:
    #Computes sum of squares based on type. It is used in Anova algorithm
    registered = True
    def __init__(self):
        self.SumOfSquares =dict()
        self.ModelVariables = dict()
        self.init  = True

    def step(self, *args):
        no = int(args[0])
        formula = str(args[1])
        sst = float(args[2])
        ssregs = float(args[3])
        sse = float(args[4])

        self.type = int(args[5])
        self.SumOfSquares[no] = ssregs
        self.ModelVariables[no] = formula

        if self.init == True:
            self.init = False
            self.minNo = no
            self.maxNo = no

        self.maxNo = max(no, self.maxNo)
        self.minNo = min(no ,self.minNo)

    def final(self):

        yield ("no","modelvariables","sumofsquares")
        if self.type == 1:

            self.ModelVariables[self.minNo] = self.ModelVariables[self.minNo].replace('+intercept+','')
            self.ModelVariables[self.minNo] = self.ModelVariables[self.minNo].replace('+intercept','')
            self.ModelVariables[self.minNo] = self.ModelVariables[self.minNo].replace('intercept+','')
            yield self.minNo,self.ModelVariables[self.minNo], self.SumOfSquares[self.minNo]

            for i in xrange(self.minNo+1,self.maxNo+1):
                self.ModelVariables[i] = self.ModelVariables[i].replace('+intercept+','')
                self.ModelVariables[i] = self.ModelVariables[i].replace('+intercept','')
                self.ModelVariables[i] = self.ModelVariables[i].replace('intercept+','')
                ModelVariables = re.split('\+',self.ModelVariables[i])
                yield i, ModelVariables[-1], self.SumOfSquares[i] -self.SumOfSquares[i-1]

        elif self.type == 2:
            no = 0
            for i in xrange(self.minNo,self.maxNo,2):
                print self.SumOfSquares[i+1],self.SumOfSquares[i]
                nowVariables = re.split('\+',self.ModelVariables[i])
                nowVariables=[x for x in nowVariables if x] # remove nulls elements of the list

                nowVariablesTotalGroup = re.split('\+',self.ModelVariables[i+1])
                nowVariablesTotalGroup=[x for x in nowVariablesTotalGroup if x] # remove nulls elements of the list
                missingitem = [item for item in nowVariablesTotalGroup if item not in nowVariables]

                if len(missingitem)==1:
                    yield no, missingitem[0], self.SumOfSquares[i+1]-self.SumOfSquares[i]
                    no = no + 1

        elif self.type == 3:
            totalVariables = re.split('\+',self.ModelVariables[self.maxNo])
            totalVariables=[x for x in totalVariables if x] # remove nulls elements of the list

            for i in xrange(self.minNo,self.maxNo):
                nowVariables = re.split('\+',self.ModelVariables[i])
                nowVariables=[x for x in nowVariables if x] # remove nulls elements of the list
                missingitem = [item for item in totalVariables if item not in nowVariables]

                yield i, missingitem[0], self.SumOfSquares[self.maxNo]-self.SumOfSquares[i]



# no modelvariables, sumofsquares, '%{metadata}', N )  from sumofsquares
#https://www.theanalysisfactor.com/calculate-effect-size/
class anovastatistics:

    registered = True
    def __init__(self):
        self.df = dict()
        self.sumofsquares = dict()
        self.df =dict()
        self.meansquare = dict()
        self.rows = dict()
        self.SStotal = 0
        self.dfTotal = 0;

    def step(self, *args):
        no = int(args[0])
        modelvariables = str(args[1])
        self.sumofsquares[modelvariables] = float(args[2])
        self.rows[no]= modelvariables
        self.metadata = json.loads(args[3])
        metadata = dict()
        for pair in self.metadata:
            metadata[str(pair[0])]= re.split(',',str(pair[1]))
        N = int(args[4])
        if modelvariables != 'intercept':
            self.SStotal += float(args[2])

        #1. df Computation
        if modelvariables not in ['residuals'] and modelvariables not in ['intercept']:
            colNamesWithValsList = re.split(':',modelvariables)
            colNamesWithValsList=[x for x in colNamesWithValsList if x] # remove nulls elements of the list
            self.df[modelvariables] = 1;
            for c in colNamesWithValsList:
                # print modelvariables
                self.df[modelvariables] = self.df[modelvariables] * (len(metadata[c])-1)
            self.dfTotal = self.dfTotal + self.df[modelvariables]
        elif modelvariables in ['residuals']:
            self.df[modelvariables] = N; #pairnei timh sto final . Edw to arxikopoiw
            # self.df[modelvariables] = 1;
            # for c in metadata:
            #     self.df[modelvariables] = self.df[modelvariables] * len(metadata[c])
            # self.df[modelvariables] = N - self.df[modelvariables]
        elif modelvariables in ['intercept']:
            self.df[modelvariables] = 1

        #2. Mean Square Computation
        if  self.df[modelvariables] != 0:
            self.meansquare[modelvariables] =  self.sumofsquares[modelvariables] / self.df[modelvariables]

    def final(self):
        # print str(self.df['residuals']), str(self.dfTotal)
        yield ('no','modelvariables', 'sumofsquares', 'df', 'meansquare',"f","p","etasquared","partetasquared", "omegasquared")

        self.df['residuals'] =  self.df['residuals'] - self.dfTotal - 1
        self.meansquare['residuals'] =  self.sumofsquares['residuals'] / self.df['residuals']

        for c in xrange(len(self.rows)):
            key = self.rows[c]
            if self.df[key] !=0:
                F = self.meansquare[key]/self.meansquare['residuals']
                P = stats.f.sf(F,self.df[key],self.df['residuals'])
            etasquared = self.sumofsquares[key] / self.SStotal
            # print etasquared
            partetasquared = self.sumofsquares[key]/(self.sumofsquares[key]+self.sumofsquares['residuals'])
            omegasquared = (self.sumofsquares[key] - self.df[key] *self.meansquare['residuals'])/(self.SStotal + self.meansquare['residuals'])
            if self.df[key] != 0: 
                yield c,key,self.sumofsquares[key],self.df[key],self.meansquare[key],F,P,etasquared,partetasquared,omegasquared
            else:
                yield c,key,self.sumofsquares[key],self.df[key],None,None,None,etasquared,partetasquared,omegasquared





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
