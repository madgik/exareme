import setpath
import functions
import json
import ast
registered=True



class formattreetotableoutput(functions.vtable.vtbase.VT):

    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']


        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        resulttable = []
        for myrow in c: # assume that it is ordered by nodeno
            level = int(myrow[0]) #currentnode
            nodestoinsert = ast.literal_eval(myrow[1]) #colname,colval,nextnode,leafval
            set1=[]
            set2=[]
            for node in nodestoinsert:
                if node[3]=="-":
                    set1.append(node)
                    set1 = sorted(set1, key=lambda (colname,colval,nextnode,leafval):(colval,leafval))
                else :
                    set2.append(node)
                    set2 = sorted(set2, key=lambda (colname,colval,nextnode,leafval):(colval,nextnode))
            print "AA",nodestoinsert
            print "Set1",set1
            print "Set2",set2
            nodestoinsert= set2 + set1
            print "BB",nodestoinsert

            print nodestoinsert
            indexi = 0
            levelsi = 0
            for i in xrange(len(resulttable)):
                if str(resulttable[i][4])== str(level):
                    indexi = i
                    levelsi= int(resulttable[i][1])+ 1
            for i in xrange(len(nodestoinsert)):
                resulttable.insert(i + indexi+1, [level,levelsi,nodestoinsert[i][0],nodestoinsert[i][1],nodestoinsert[i][2],nodestoinsert[i][3]])

        yield [('no',),('result',),]
        print resulttable
        for i in xrange(len(resulttable)):
            result =""
            for j in xrange(resulttable[i][1]):
                result+="| "
            result=result + resulttable[i][2] + "=" + resulttable[i][3]
            if resulttable[i][5]!="":
                result+=":"+resulttable[i][5]

            yield i,result




# formattreetotableoutput  select currentnode as nodeno ,jgroup(jpack(colname||"="||colval,nextnode,leafval)) as nodeinfo from  mytree group by currentnode ;



def Source():
    return functions.vtable.vtbase.VTGenerator(formattreetotableoutput)



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