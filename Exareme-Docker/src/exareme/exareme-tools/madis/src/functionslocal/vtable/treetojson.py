import setpath
import functions
import json
import ast
registered=True


# [1|1[2|0[3|CL_tear_prod_rate[4|normal[5|2[6|
# [1|1[2|0[3|CL_tear_prod_rate[4|reduced[5|[6|none
# [1|2[2|1[3|CL_astigmatism[4|no[5|3[6|
# [1|2[2|1[3|CL_astigmatism[4|yes[5|4[6|
# [1|3[2|2[3|CL_age[4|pre-presbyopic[5|[6|soft
# [1|3[2|2[3|CL_age[4|presbyopic[5|5[6|
# [1|3[2|2[3|CL_age[4|young[5|[6|soft
# [1|4[2|2[3|CL_spectacle_prescrip[4|hypermetrope[5|6[6|
# [1|4[2|2[3|CL_spectacle_prescrip[4|myope[5|[6|hard
# [1|5[2|3[3|CL_spectacle_prescrip[4|hypermetrope[5|[6|soft
# [1|5[2|3[3|CL_spectacle_prescrip[4|myope[5|[6|none
# [1|6[2|4[3|CL_age[4|pre-presbyopic[5|[6|none
# [1|6[2|4[3|CL_age[4|presbyopic[5|[6|none
# [1|6[2|4[3|CL_age[4|young[5|[6|hard
# --- [0|Column names ---
# [1|currentnode [2|parentnode [3|colname [4|colval [5|nextnode [6|leafval

def recursive_checkchilds(resulttable, nodestoinsert, level):

    for i in xrange(len(resulttable)):
        # print "CCC",level, resulttable[i]['currentnode']
        if 'childnodes' in resulttable[i].keys():
            if str(resulttable[i]['childnodes'])== str(level):
                for k in nodestoinsert:
                    k.pop('id')
                resulttable[i]['childnodes']= nodestoinsert
                # print "DDD",resulttable[i]['nextnode']
                return
            elif "colname" in str(resulttable[i]['childnodes']): #is a dict containing childs
                     recursive_checkchilds(resulttable[i]['childnodes'], nodestoinsert, level)


class treetojson(functions.vtable.vtbase.VT):

    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        cur = envars['db'].cursor()
        c=cur.execute(query)
        schema = cur.getdescriptionsafe()

        init = True

        for myrow in c: # assume that it is ordered by nodeno
            # print myrow
            level = int(myrow[0]) #currentlevel
            nodestoinsert = ast.literal_eval(myrow[1]) #nodes of the level at hand. It is a dice
            for i in nodestoinsert:
                if str(i['leafval']) =="": i.pop('leafval')
                if str(i['childnodes'])=="": i.pop('childnodes')

            if init is True:
                for k in nodestoinsert:
                    k.pop('id')
                resulttable =  nodestoinsert
                init = False
            else:
                # print "AA", resulttable
                recursive_checkchilds(resulttable,nodestoinsert,level)

        # print "RESULT",resulttable
        yield [('result',),]
        # print str(resulttable)
        yield [str(resulttable),]





# formattreetotableoutput  select currentnode as nodeno ,jgroup(jpack(colname||"="||colval,nextnode,leafval)) as nodeinfo from  mytree group by currentnode ;



def Source():
    return functions.vtable.vtbase.VTGenerator(treetojson)



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