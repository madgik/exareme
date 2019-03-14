import apsw
import copy
import functions
import json
import operator
from collections import OrderedDict, deque
from lib import argsparse
from lib import schemaUtils

registered = True

constraints = {
    2: 'SQLITE_INDEX_CONSTRAINT_EQ',
    32: 'SQLITE_INDEX_CONSTRAINT_GE',
    4: 'SQLITE_INDEX_CONSTRAINT_GT',
    8: 'SQLITE_INDEX_CONSTRAINT_LE',
    16: 'SQLITE_INDEX_CONSTRAINT_LT',
    64: 'SQLITE_INDEX_CONSTRAINT_MATCH'
}


# Decorator to extended a function by calling first another function with no arguments

def echocall(func):
    def wrapper(*args, **kw):
        obj = args[0]
        Extra = ""
        if 'tablename' in obj.__dict__:
            Extra = obj.tablename
        if functions.settings['vtdebug']:
            print "Table %s:Before Calling %s.%s(%s)" % (Extra + str(obj), obj.__class__.__name__, func.__name__,
                                                         ','.join(
                                                             [repr(l) for l in args[1:]] + ["%s=%s" % (k, repr(v)) for
                                                                                            k, v in kw.items()]))
            aftermsg = "Table %s:After Calling %s.%s(%s)" % (Extra, obj.__class__.__name__, func.__name__, ','.join(
                [repr(l) for l in args[1:]] + ["%s=%s" % (k, repr(v)) for k, v in kw.items()]))
        a = func(*args, **kw)
        if functions.settings['vtdebug']:
            pass
            # print aftermsg
        return a
        # return func(*args, **kw)

    return wrapper


class Source:
    def __init__(self, boolargs=[], nonstringargs=dict(), needsescape=[]):
        self.tableObjs = dict()
        self.boolargs = boolargs
        self.nonstringargs = nonstringargs
        self.needsescape = needsescape

    @echocall
    def Create(self, db, modulename, dbname, tablename, *args):
        dictargs = {'tablename': tablename, 'db': db, 'dbname': dbname, 'modulename': modulename}
        self.tableObjs[tablename] = LTable(self.tableObjs, self.boolargs, self.nonstringargs, self.needsescape, *args,
                                           **dictargs)
        return [self.tableObjs[tablename].getschema(), self.tableObjs[tablename]]

    @echocall
    def Connect(self, db, modulename, dbname, tablename, *args):
        if tablename not in self.tableObjs:
            return Create(self, db, modulename, dbname, tablename, *args)
        return [self.tableObjs[tablename].getschema(), self.tableObjs[tablename]]


class Cache:

    def __init__(self, querycursor, query):
        # print Cache
        self.query = query
        self.cursor = querycursor
        self.ordercaches = {}
        self.orderbys = []
        self.maxlen = 1000
        self.stopiteration = False
        self.cachetotalwindownumber = 0
        self.tempcachelist = None
        self.lastvalue = None
        self.stopiteration = True

    def addOrderBy(self, orderbys):
        if orderbys in self.orderbys:
            return
        else:
            if len(orderbys) != 0 and len([item for item in orderbys if item[0] == 0]) == 0:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "Orders are not in time column ")

            self.orderbys.append(orderbys)
            self.ordercaches[orderbys] = (OrderedDict(), deque())
            for k, v in self.ordercaches[self.orderbys[0]][1]:
                data = self.orderByData(orderbys, copy.copy(v))
                self.ordercaches[orderbys][0][k] = data
                self.ordercaches[orderbys][1].append([k, data])

    def orderByData(self, orderbys, data):
        for o in reversed(orderbys):
            data.sort(key=operator.itemgetter(o[0]), reverse=o[1])

        return data

    def nextGroup(self):
        while not self.nextValue():
            pass

    # return True if close a group, else return False
    def nextValue(self):
        currentrow = self.cursor.next()
        if self.tempcachelist is None:
            self.tempcachelist = [currentrow[0], [currentrow]]
            self.stopiteration = False

            return False

        if self.tempcachelist[0] != currentrow[0]:
            for orderbys in self.orderbys:
                data = self.orderByData(orderbys, copy.copy(self.tempcachelist[1]))
                self.ordercaches[orderbys][1].appendleft([self.tempcachelist[0], data])
                self.ordercaches[orderbys][0][self.tempcachelist[0]] = data

            firstcachelist = self.ordercaches[self.orderbys[0]][1]
            if len(firstcachelist) > self.maxlen:
                for cache in self.ordercaches.values():
                    cache[0].popitem(last=False)
                    cache[1].pop()

            self.cachetotalwindownumber += 1

            self.lastvalue = self.tempcachelist[0]
            self.tempcachelist = [currentrow[0], [currentrow]]

            return True
        else:
            self.tempcachelist[1].append(currentrow)

            return False

    def scan(self, orderbys):
        cachelist = self.ordercaches[orderbys][1]
        # If order needed
        if len(orderbys) > 1:
            try:
                if len(cachelist) == 0:
                    self.nextGroup()

                totalwindownumber = self.cachetotalwindownumber - (len(cachelist) - 1)
                while True:
                    if (self.cachetotalwindownumber - totalwindownumber) >= len(cachelist):
                        totalwindownumber = self.cachetotalwindownumber - (len(cachelist) - 1)

                    while (self.cachetotalwindownumber - totalwindownumber) < 0:
                        self.nextGroup()

                    idx = self.cachetotalwindownumber - totalwindownumber
                    for v in cachelist[idx][1]:
                        yield v

                    totalwindownumber += 1
            except StopIteration:
                for v in self.tempcachelist[1]:
                    yield v

                raise StopIteration
        else:
            if self.tempcachelist is None:
                self.nextValue()

            totalwindownumber = self.cachetotalwindownumber - len(cachelist)
            tuplenumber = 0
            while True:
                if (self.cachetotalwindownumber - totalwindownumber) == -1 and (
                        len(self.tempcachelist[1]) - 1) < tuplenumber:
                    self.nextValue()

                idx = self.cachetotalwindownumber - totalwindownumber
                if idx >= len(cachelist):
                    totalwindownumber = self.cachetotalwindownumber - (len(cachelist) - 1)
                    tuplenumber = 0
                elif idx >= 0 and (len(cachelist[idx][1]) - 1) < tuplenumber:
                    totalwindownumber += 1
                    tuplenumber = 0

                idx = self.cachetotalwindownumber - totalwindownumber
                if idx == -1:
                    yield self.tempcachelist[1][tuplenumber]
                else:
                    yield cachelist[idx][1][tuplenumber]

                tuplenumber += 1

    # def innerJoin(self, key, constraints, constraintargs, orderbys):
    #     # print 'constraints, constraintargs, orderbys', constraints, constraintargs, orderbys
    #     try:adp@
    #         while self.lastvalue is None or key > self.lastvalue:
    #             self.nextGroup()
    #     except StopIteration:
    #         pass
    #
    #     try:
    #         return self.ordercaches[orderbys][0][key]
    #     except KeyError:
    #         return []

    def innerJoin(self, key, orderbys):
        while self.tempcachelist is None or key > self.tempcachelist[0]:
            self.nextValue()

        if self.tempcachelist[0] == key:
            totalwindownumber = self.cachetotalwindownumber + 1
            tuplenumber = 0
            while True:
                idx = self.cachetotalwindownumber - totalwindownumber
                if idx > -1:
                    cachelist = self.ordercaches[orderbys][1][idx]
                    for v in cachelist[1][tuplenumber:]:
                        yield v

                    break
                else:
                    if (len(self.tempcachelist[1]) - 1) < tuplenumber:
                        if self.nextValue():
                            break

                    yield self.tempcachelist[1][tuplenumber]
                    tuplenumber += 1
        else:
            try:
                for v in self.ordercaches[orderbys][0][key]:
                    yield v
            except KeyError:
                raise StopIteration


class LTable:  ####Init means setschema and execstatus
    autostring = 'automatic_vtable'

    @echocall
    def __init__(self, tblist, boolargs, nonstringargs, needsescape, *args,
                 **envars):  # envars tablename, auto  , OPTIONAL []
        self.delayedexception = None
        self.tblist = tblist
        self.auto = False
        self.first = True
        self.schema = "create table %s('Error')" % (envars['tablename'])
        self.tablename = envars['tablename']
        self.description = None
        self.consdict = {}
        self.coldata = []
        self.rowids = []
        self.kdindex = None
        self.lastcalculatedidx = None
        self.ordered = False
        self.envarsdb = envars['db']

        self.innerjoin = True

        self.query = None
        self.keepcursor = True

        largs, kargs = [], dict()
        try:
            largs, kargs = argsparse.parse(args, boolargs, nonstringargs, needsescape)
        except Exception, e:
            raise functions.MadisError(e)

        if 'fullouterjoin' in kargs:
            if str(kargs['fullouterjoin']).lower() == 't' or str(kargs['fullouterjoin']).lower() == 'true':
                self.innerjoin = False

        try:
            self.query = kargs['query']
            self.q = envars['db'].cursor().execute(kargs['query'])
            self.description = self.q.getdescription()
        except apsw.ExecutionCompleteError:
            raise functions.DynamicSchemaWithEmptyResultError(__name__.rsplit('.')[-1])

        self._setschema()
        self.cache = Cache(self.q, self.query)

    @echocall
    def _setschema(self):
        descr = self.description  ### get list of tuples columnname, type
        self.schema = schemaUtils.CreateStatement(descr, self.tablename)

    @echocall
    def getschema(self):
        if functions.settings['tracing']:
            print "VT schema:%s" % (self.schema)
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

        # print 'constraint_param, orderbys', constraint_param, orderbys
        timecolseen = False
        for c in constraint_param:
            if c[1] == apsw.SQLITE_INDEX_CONSTRAINT_MATCH or c[0] != 0:
                indexes.append(None)
            else:
                if c[1] != apsw.SQLITE_INDEX_CONSTRAINT_EQ or timecolseen:
                    indexes.append(None)
                else:
                    indexes.append((0, True))
                    newcons.append(c)
                    timecolseen = True

        consname = json.dumps(newcons, separators=(',', ':')) + json.dumps(orderbys, separators=(',', ':'))

        self.consdict[consname] = (newcons, orderbys)

        cost = 0
        # if newcons == [] and ((0, False),) != orderbys:
        if newcons == []:
            cost = 1000000000

        ordered = True
        if len(orderbys) != 0 and len([item for item in orderbys if item[0] == 0]) == 0:
            ordered = False

        # print 'Bestndex', indexes, 0, consname, ordered, cost
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
        del (self.tblist[self.tablename])


# Represents a cursor
class Cursor:
    def __init__(self, table):
        # print Cursor
        self.table = table
        self.query = table.query
        self.eof = False
        self.pos = 0
        self.timeargpos = None
        self.resultrows = []
        self.timecolumn = 0
        self.data = list()
        self.unreadValue = None
        self.lasttime = None
        self.firsttime = True

    def getTimeConstraintArg(self, constraints, constraintsargs):
        for i, c in enumerate(constraints):
            if c[1] == apsw.SQLITE_INDEX_CONSTRAINT_EQ and c[0] == 0:
                return constraintsargs[i]

        raise functions.OperatorError(__name__.rsplit('.')[-1], "Not Defined Equal Constraint in Timecolumn")

    def Filter(self, indexnum, indexname, constraintargs):
        self.eof = False
        constraints, orderbys = self.table.consdict[indexname]
        self.table.cache.addOrderBy(orderbys)

        # print 'constraints, orderbys, constraintargs', constraints, orderbys, constraintargs
        if len(constraintargs) == 0:
            self.resultrows = self.table.cache.scan(orderbys)
        else:
            key = self.getTimeConstraintArg(constraints, constraintargs)
            self.resultrows = self.table.cache.innerJoin(key, orderbys)

        try:
            self.row = self.resultrows.next()
        except StopIteration:
            self.eof = True

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
            self.row = self.resultrows.next()
        except StopIteration:
            self.eof = True

    @echocall
    def Close(self):
        del (self.resultrows)


import re

onlyalphnum = re.compile(ur'[a-zA-Z]\w*$')


def schemastr(tablename, colnames, typenames=None):
    stripedcolnames = [el if onlyalphnum.match(el) else '"' + el.replace('"', '""') + '"' for el in colnames]
    if not typenames:
        return "create table %s(%s)" % (tablename, ','.join(['"' + str(c) + '"' for c in unify(stripedcolnames)]))
    else:
        stripedtypenames = ['' if el == "None" else el if onlyalphnum.match(el) else '"' + el.replace('"', '""') + '"'
                            for el in typenames]
        return "create table %s(%s)" % (
        tablename, ','.join([str(c) + ' ' + str(t) for c, t in zip(unify(stripedcolnames), stripedtypenames)]))


def unify(slist):
    if len(set(slist)) == len(slist):
        return slist
    eldict = {}
    for s in slist:
        if s in eldict:
            eldict[s] += 1
        else:
            eldict[s] = 1
    for val, fr in eldict.items():
        if fr == 1:
            del eldict[val]
    for val in eldict:
        eldict[val] = 1
    uniquelist = []
    for s in slist:
        if s in eldict:
            uniquelist += [s + str(eldict[s])]
            eldict[s] += 1
        else:
            uniquelist += [s]

    return uniquelist


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
