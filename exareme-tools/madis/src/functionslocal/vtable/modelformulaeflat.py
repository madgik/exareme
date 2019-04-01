import setpath
import functions
import json
import re
registered=True

'''
Example
drop table if exists mydata;
create table mydata as select 1 as adnicategory_AD, 0 as adnicategory_CN, 0 as adnicategory_MCI , 1 as gender_F ,0 as gender_M, 0.1 as x;

insert into mydata  select 1 , 0 , 0 , 0 ,1 , 0.6;
insert into mydata  select 0 , 1 , 0 , 1 ,0 , 0.6;
insert into mydata  select 0 , 1 , 0 , 0 ,1 , 1.5;
insert into mydata  select 0 , 0 , 1 , 1 ,0 , 0.6;
insert into mydata  select 0 , 0 , 1 , 0 ,1 , 3 ;

var 'x' 'adnicategory*x+gender';
modelFormulae formula:%{x} select * from mydata;


'''

def getIndexOfTuple(mylist, value):
    index = []
    for i in xrange(len(mylist)):
        if mylist[i].split('_')[0] == value:
            index.append(mylist[i])
    return index

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


class modelformulaeflat(functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'formula' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No formula ")
        formula = str(dictargs['formula'])

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        oldschema = []
        for i in xrange(len(schema)):
            oldschema.append(str(schema[i][0]))

        print "oldschema", oldschema
        print formula

        newschema1 = ['intercept']
        removefromnewschema = []
        formulaList = re.split('(\+|\-)',formula)

        if formulaList[0] =='':
            formulaList.pop(0)
        if formulaList[0] != '-':
            formulaList.insert(0,'+')

        for f in xrange(len(formulaList)):
            if formulaList[f]!= '+' and formulaList[f]!='-':
                print "newitem", formulaList[f-1],formulaList[f]
                if formulaList[f]=='0':
                    removefromnewschema.append('intercept')
                elif ('*' in formulaList[f]):
                    fpartsA = formulaList[f].split('*')
                    index1 = getIndexOfTuple(oldschema,fpartsA[0])
                    index2 = getIndexOfTuple(oldschema,fpartsA[1])
                    print index1,index2
                    if formulaList[f-1] == '+':  # add these columns
                        for ind1 in index1:
                            newschema1.append(ind1)
                        for ind2 in index2:
                            newschema1.append(ind2)
                        for ind1 in index1:
                            for ind2 in index2:
                                newschema1.append(ind1+':'+ind2)
                    if formulaList[f-1] == '-':  #remove these columns
                        for ind1 in index1:
                            removefromnewschema.append(ind1)
                        for ind2 in index2:
                            removefromnewschema.append(ind2)
                        for ind1 in index1:
                            for ind2 in index2:
                                removefromnewschema.append(ind1+':'+ind2)
                elif (':' in formulaList[f]):
                    fpartsA = formulaList[f].split(':')
                    index1 = getIndexOfTuple(oldschema,fpartsA[0])
                    index2 = getIndexOfTuple(oldschema,fpartsA[1])
                    print "here", fpartsA[0], fpartsA[1],index1,index2
                    if len(index1) ==0 :
                        index1= [fpartsA[0]]
                    if len(index2) ==0 :
                        index2 = [fpartsA[1]]
                    print "here", fpartsA[0], fpartsA[1],index1,index2
                    if formulaList[f-1] == '+':  # add these columns
                        for ind1 in index1:
                            for ind2 in index2:
                                newschema1.append(ind1+':'+ind2)
                    if formulaList[f-1] == '-':  #remove these columns
                        for ind1 in index1:
                            for ind2 in index2:
                                removefromnewschema.append(ind1+':'+ind2)
                else:
                    index1 = getIndexOfTuple(oldschema,formulaList[f])
                    if formulaList[f-1] == '+':
                        for ind1 in index1:
                            newschema1.append(ind1)
                    if formulaList[f-1] == '-':
                        for ind1 in index1:
                            removefromnewschema.append(ind1)
        print "A1", newschema1
        print "A2", removefromnewschema

        newschema = [x for x in newschema1 if x not in  removefromnewschema]
        newschema = list(set(newschema))


        print "schema", newschema
        yield tuple((x,) for x in newschema)

        for myrow in c:
            # print myrow
            res = []
            for x in newschema:
                if 'intercept' in x:
                    res.append(1)
                elif ':' in x:
                    # print "x",x
                    parts = x.split(':')
                    index1 = oldschema.index(parts[0])
                    index2 = oldschema.index(parts[1])

                    # print "indexes11", index1,index2
                    res.append(myrow[index1]*myrow[index2])
                else:
                    # print "x",x
                    index1 = oldschema.index(x)
                    # print "indexes2", index1
                    res.append(myrow[index1])

            # print "res", res
            yield tuple(res,)


def Source():
    return functions.vtable.vtbase.VTGenerator(modelformulaeflat)


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
        doctest.tes