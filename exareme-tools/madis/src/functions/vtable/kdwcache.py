"""

Examples:

.. doctest::
    >>> table1('''
    ... 2   1
    ... 4   1
    ... 6   1
    ... 8   1
    ... 10  1
    ... ''')

    >>> sql("select * from (wcache select * from table1)")
    a  | b
    ------
    2  | 1
    4  | 1
    6  | 1
    8  | 1
    10 | 1

    >>> table2('''
    ... 1   2
    ... 3   2
    ... 5   2
    ... 7   2
    ... 9   2
    ... 11  2
    ... ''')

    >>> sql("select * from (wcache select cast(a as long) as t1, cast(b as long) as a from table1), (wcache select cast(a as long) as t2, cast(b as long) as b from table2) where t1=t2")
    t1 | a | t2 | b
    ------------------
    2  | 1 | 1  | 2
    2  | 1 | 2  | None
    4  | 1 | 3  | 2
    4  | 1 | 4  | None
    6  | 1 | 5  | 2
    6  | 1 | 6  | None
    8  | 1 | 7  | 2
    8  | 1 | 8  | None
    10 | 1 | 9  | 2
    10 | 1 | 10 | None

    >>> sql("select * from (wcache select cast(a as long) as t1, cast(b as long) as a from table1), (wcache select cast(a as long) as t2, cast(b as long) as b from table2) where t1=t2 and b is null")
    t1 | a | t2 | b
    ------------------
    2  | 1 | 2  | None
    4  | 1 | 4  | None
    6  | 1 | 6  | None
    8  | 1 | 8  | None
    10 | 1 | 10 | None

    >>> table3('''
    ... 1   3
    ... 1   3
    ... 2   3
    ... 2   3
    ... 3   3
    ... 3   3
    ... 4   3
    ... 4   3
    ... 5   3
    ... 5   3
    ... ''')

    >>> sql("select * from (wcache select cast(a as long) as t1, cast(b as long) as a from table3), (wcache select cast(a as long) as t2, cast(b as long) as b from table1) where t1=t2")
    t1 | a | t2 | b
    ------------------
    1  | 3 | 1  | None
    1  | 3 | 1  | None
    2  | 3 | 2  | 1
    2  | 3 | 2  | 1
    3  | 3 | 3  | None
    3  | 3 | 3  | None
    4  | 3 | 4  | 1
    4  | 3 | 4  | 1
    5  | 3 | 5  | None
    5  | 3 | 5  | None

.. seealso::

    * :ref:`tutcache`

"""

import copy
import setpath
import functions
import apsw
import sys
import operator
import json
import itertools
from lib import argsparse
from collections import OrderedDict, deque
from lib import schemaUtils
from lib import kdtree
import time

registered=True

constraints={
2:'SQLITE_INDEX_CONSTRAINT_EQ',
32:'SQLITE_INDEX_CONSTRAINT_GE',
4:'SQLITE_INDEX_CONSTRAINT_GT',
8:'SQLITE_INDEX_CONSTRAINT_LE',
16:'SQLITE_INDEX_CONSTRAINT_LT',
64:'SQLITE_INDEX_CONSTRAINT_MATCH'
}


# Decorator to extended a function by calling first another function with no arguments

def echocall(func):
    def wrapper(*args, **kw):
        obj=args[0]
        Extra=""
        if 'tablename' in obj.__dict__:
            Extra=obj.tablename
        if functions.settings['vtdebug']:
            print "Table %s:Before Calling %s.%s(%s)" %(Extra+str(obj),obj.__class__.__name__,func.__name__,','.join([repr(l) for l in args[1:]]+["%s=%s" %(k,repr(v)) for k,v in kw.items()]))
            aftermsg="Table %s:After Calling %s.%s(%s)" %(Extra,obj.__class__.__name__,func.__name__,','.join([repr(l) for l in args[1:]]+["%s=%s" %(k,repr(v)) for k,v in kw.items()]))
        a=func(*args, **kw)
        if functions.settings['vtdebug']:
            pass
            #print aftermsg
        return a
        #return func(*args, **kw)
    return wrapper

class Source:
    def __init__(self,boolargs=[],nonstringargs=dict(),needsescape=[]):
        self.tableObjs=dict()
        self.boolargs=boolargs
        self.nonstringargs=nonstringargs
        self.needsescape=needsescape
    @echocall
    def Create(self, db, modulename, dbname, tablename,*args):
        dictargs={'tablename':tablename,'db':db,'dbname':dbname,'modulename':modulename}
        self.tableObjs[tablename]=LTable(self.tableObjs,self.boolargs,self.nonstringargs,self.needsescape,*args,**dictargs)
        return [self.tableObjs[tablename].getschema(),self.tableObjs[tablename]]
    @echocall
    def Connect(self, db, modulename, dbname, tablename,*args):
        if tablename not in self.tableObjs:
            return Create(self, db, modulename, dbname, tablename,*args)
        return [self.tableObjs[tablename].getschema(),self.tableObjs[tablename]]

class Cache:

    def __init__(self, querycursor, query):
        self.query = query
        self.cursor = querycursor
        self.ordercaches = {}
        self.orderbys = []
        self.statistics = {}
        self.maxlen = 1000
        self.lastvalue = None
        self.ungroupedrow = None
        self.stopiteration = False

    def window_index(self, data):
        data=[x+(y,) for x,y in itertools.izip(data,itertools.count())]
        return kdtree.kdtree(data)

    def scanRow(self, row, constraints, constraintargs):
        for i, c in enumerate(constraints):
            if c[1] == 2:
                if not row[c[0]] == constraintargs[i]:
                    return False
            elif c[1] == 32:
                if not row[c[0]] >= constraintargs[i]:
                    return False
            elif c[1] == 4:
                if not row[c[0]] > constraintargs[i]:
                    return False
            elif c[1] == 8:
                if not row[c[0]] <= constraintargs[i]:
                    return False
            elif c[1] == 16:
                if not row[c[0]] < constraintargs[i]:
                    return False

        return True

    def addOrderBy(self, orderbys):
        if orderbys in self.orderbys:
            return
        else:
            if len(orderbys) != 0 and len([item for item in orderbys if item[0] == 0]) == 0:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "Orders are not in time column ")

            self.orderbys.append(orderbys)
            self.statistics[orderbys] = [None, 0]
            self.ordercaches[orderbys] = (OrderedDict(), deque(), OrderedDict())
            for k, v in self.ordercaches[self.orderbys[0]][1]:
                data = self.orderByData(orderbys, copy.deepcopy(v))
                self.ordercaches[orderbys][0][k] = data
                self.ordercaches[orderbys][1].append([data[0], data])

    def orderByData(self, orderbys, data):
        for o in reversed(orderbys):
            data.sort(key=operator.itemgetter(o[0]),reverse=o[1])

        return data

    def nextGroup(self):
        firstcachelist = self.ordercaches[self.orderbys[0]][1]
        if len(firstcachelist) == 0:
            self.ungroupedrow = self.cursor.next()

        prevrow = self.ungroupedrow
        currentrow = prevrow
        self.tempcachelist = [currentrow[0], []]

        try:
            while prevrow[0] == currentrow[0]:
                self.tempcachelist[1].append(currentrow)
                prevrow, currentrow = currentrow, self.cursor.next()
            self.ungroupedrow = currentrow
        except StopIteration:
            if not self.stopiteration:
                self.stopiteration = True
                pass
            else:
                raise StopIteration

        for orderbys in self.orderbys:
            data = self.orderByData(orderbys, copy.deepcopy(self.tempcachelist[1]))
            self.ordercaches[orderbys][1].appendleft([self.tempcachelist[0], data])
            self.ordercaches[orderbys][0][self.tempcachelist[0]] = data
            if self.statistics[orderbys][1] > 160:
                self.ordercaches[orderbys][2][self.tempcachelist[0]] = self.window_index(data)

        if len(firstcachelist) > self.maxlen:
            for cache in self.ordercaches.values():
                cache[0].popitem(last=False)
                cache[1].pop()

        self.lastvalue = self.tempcachelist[0]

    def getFromBeyond(self, nextscanvalue, orderbys):
        # If must get new value
        cachelist = self.ordercaches[orderbys][1]
        if cachelist[0][0] == nextscanvalue:
            self.nextGroup()
            ret = [cachelist[0]]
        # If values exists in cache
        else:
            ret=[[k, v] for (k, v) in cachelist if k>nextscanvalue]
            ret.reverse()

        return ret

    def scan(self, orderbys):
        cachelist = self.ordercaches[orderbys][1]
        if len(cachelist) == 0:
            self.nextGroup()

        cacheinstance = copy.deepcopy(cachelist)
        cacheinstance.reverse()
        nextscanvalue=cacheinstance[-1][0]
        while True:
            for k, g in cacheinstance:
                for v in g:
                    yield v

            cacheinstance=copy.deepcopy(self.getFromBeyond(nextscanvalue, orderbys))
            nextscanvalue = cacheinstance[-1][0]

    def innerJoin(self, key, constraints, constraintargs, orderbys):
        try:
            while self.lastvalue==None or key>self.lastvalue:
                self.nextGroup()
        except StopIteration:
            pass

        stats = self.statistics[orderbys]
        if stats[0] != key:
            stats[0] = key
            stats[1] = 0
        else:
            stats[1] += 1

        try:
            return kdtree.query(self.ordercaches[orderbys][2][key], constraints, constraintargs)
        except KeyError:
            try:
                window = []
                for r in self.ordercaches[orderbys][0][key]:
                    if self.scanRow(r, constraints, constraintargs):
                        window.append(r)

                return window
            except KeyError:
                return []

class LTable: ####Init means setschema and execstatus
    autostring='automatic_vtable'
    @echocall
    def __init__(self,tblist,boolargs,nonstringargs,needsescape,*args,**envars): # envars tablename, auto  , OPTIONAL []
        self.delayedexception=None
        self.tblist=tblist
        self.auto=False
        self.first=True
        self.schema="create table %s('Error')" % (envars['tablename'])
        self.tablename=envars['tablename']
        self.description=None
        self.consdict={}
        self.coldata=[]
        self.rowids=[]
        self.kdindex=None
        self.lastcalculatedidx=None
        self.ordered=False
        self.envarsdb=envars['db']

        self.innerjoin=True

        self.query = None
        self.keepcursor = True

        largs, kargs = [] ,dict()
        try:
            largs, kargs = argsparse.parse(args,boolargs,nonstringargs,needsescape)
        except Exception,e:
            raise functions.MadisError(e)

        if 'fullouterjoin' in kargs:
            if str(kargs['fullouterjoin']).lower() == 't' or str(kargs['fullouterjoin']).lower() == 'true':
                self.innerjoin=False

        try:
            self.query=kargs['query']
            self.q=envars['db'].cursor().execute(kargs['query'])
            self.description=self.q.getdescription()
        except apsw.ExecutionCompleteError:
            raise functions.DynamicSchemaWithEmptyResultError(__name__.rsplit('.')[-1])

        self._setschema()
        self.cache=Cache(self.q, self.query)

    @echocall
    def _setschema(self):
        descr=self.description ### get list of tuples columnname, type
        self.schema=schemaUtils.CreateStatement(descr, self.tablename)

    @echocall
    def getschema(self):
        if functions.settings['tracing']:
            print "VT schema:%s" %(self.schema)
        return self.schema

    def getnewcursor(self):
        if self.keepcursor:
            self.keepcursor = False
            return self.q
        try:
            return self.envarsdb.cursor().execute(self.query)
        except apsw.ExecutionCompleteError:
            raise functions.DynamicSchemaWithEmptyResultError(__name__.rsplit('.')[-1])

    @echocall
    def BestIndex(self, constraint_param, orderbys):
        indexes = []
        newcons = []
        i = 0

        timecolseen = False
        for c in constraint_param:
            if c[1] == apsw.SQLITE_INDEX_CONSTRAINT_MATCH:
                indexes.append(None)
            else:
                if c[1] == apsw.SQLITE_INDEX_CONSTRAINT_EQ and c[0] == 0:
                    timecolseen = True

                indexes.append((i, True))
                i += 1
                newcons.append(c)

        consname = json.dumps(newcons, separators=(',', ':')) + json.dumps(orderbys, separators=(',', ':'))
        self.consdict[consname] = (newcons, orderbys)
        cost = 0

        # Cost of scan
        if newcons == [] or timecolseen == False:
            cost = 1000000000

        # order
        ordered = True
        if len(orderbys) != 0 and len([item for item in orderbys if item[0] == 0]) == 0:
            ordered = False

        return indexes, 0, consname, ordered, cost

    @echocall
    def Open(self):
        return Cursor(self)

    @echocall
    def Disconnect(self):
        pass

    @echocall
    def Destroy(self):
        """
        This method is called when the table is no longer used
        """
        del self.cache
        del(self.tblist[self.tablename])

# Represents a cursor
class Cursor:
    def __init__(self, table):
        self.table=table
        self.query=table.query
        self.eof=False
        self.pos=0
        self.timeargpos = None
        self.resultrows = []
        self.timecolumn=0
        self.data=list()
        self.unreadValue=None
        self.lasttime=None
        self.firsttime=True

    def getTimeConstraintArg(self, constraints, constraintsargs):
        for i, c in enumerate(constraints):
            if c[1] == apsw.SQLITE_INDEX_CONSTRAINT_EQ and c[0] == 0:
                return constraintsargs[i]

        raise functions.OperatorError(__name__.rsplit('.')[-1], "Not Defined Equal Constraint in Timecolumn")

    def Filter(self, indexnum, indexname, constraintargs):
        self.eof=False
        constraints, orderbys=self.table.consdict[indexname]
        self.table.cache.addOrderBy(orderbys)

        # print 'constraints, orderbys', constraints, orderbys
        if len(constraintargs) == 0:
            self.resultrows = self.table.cache.scan(orderbys)
        else:
            key = self.getTimeConstraintArg(constraints, constraintargs)
            self.resultrows=iter(self.table.cache.innerJoin(key, constraints, constraintargs, orderbys))

        try:
            self.row=self.resultrows.next()
        except StopIteration:
            self.eof=True

#    @echocall #-- Commented out for speed reasons
    def Eof(self):
        return self.eof

#    @echocall #-- Commented out for speed reasons
    def Rowid(self):
        return self.pos

#    @echocall #-- Commented out for speed reasons
    def Column(self, col):
        return self.row[col]

#    @echocall #-- Commented out for speed reasons
    def Next(self):
        try:
            self.row=self.resultrows.next()
            # print self.row
        except StopIteration:
            self.eof=True

    @echocall
    def Close(self):
        del(self.resultrows)

import re
onlyalphnum=re.compile(ur'[a-zA-Z]\w*$')

def schemastr(tablename,colnames,typenames=None):
    stripedcolnames=[el if onlyalphnum.match(el) else '"'+el.replace('"','""')+'"' for el in colnames]
    if not typenames:
        return "create table %s(%s)" %(tablename,','.join(['"'+str(c)+'"' for c in unify(stripedcolnames)]))
    else:
        stripedtypenames=['' if el=="None" else el if onlyalphnum.match(el) else '"'+el.replace('"','""')+'"' for el in typenames]
        return "create table %s(%s)" %(tablename,','.join([str(c)+' '+str(t) for c,t in zip(unify(stripedcolnames),stripedtypenames)]))

def unify(slist):
    if len(set(slist))==len(slist):
        return slist
    eldict={}
    for s in slist:
        if s in eldict:
            eldict[s]+=1
        else:
            eldict[s]=1
    for val,fr in eldict.items():
        if fr==1:
            del eldict[val]
    for val in eldict:
        eldict[val]=1
    uniquelist=[]
    for s in slist:
        if s in eldict:
            uniquelist+=[s+str(eldict[s])]
            eldict[s]+=1
        else:
            uniquelist+=[s]

    return uniquelist


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

