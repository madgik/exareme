"""
Examples:

    >>> table1('''
    ... James   10	2
    ... Mark    7	3
    ... Lila    74	1
    ... ''')
    >>> sql("select * from table1")
    a     | b  | c
    --------------
    James | 10 | 2
    Mark  | 7  | 3
    Lila  | 74 | 1

    >>> sql("coutput '../../tests/output' split:2  mode:rcfile select hashmodarchdep(rank,2),* from (select a as name , b as age, c as rank from table1)")
    return_value
    ------------
    1
    >>> sql("unionallrcfiles file:../../tests/output")
    name  | age | rank
    ------------------
    Mark  | 7   | 3
    Lila  | 74  | 1
    James | 10  | 2

    >>> sql("coutput '../../tests/emptyoutput' split:2  mode:rcfile select hashmodarchdep(rank,2),* from (select a as name , b as age, c as rank from table1 limit 0)")
    return_value
    ------------
    1
    >>> sql("unionallrcfiles file:../../tests/emptyoutput")


    >>> sql("coutput '../../tests/outputsp8' split:8  mode:rcfile select hashmodarchdep(rank,8),* from (select a as name , b as age, c as rank from table1)")
    return_value
    ------------
    1
    >>> sql("unionallrcfiles file:../../tests/outputsp8")
    name  | age | rank
    ------------------
    James | 10  | 2
    Mark  | 7   | 3
    Lila  | 74  | 1
"""


import os.path
import sys
from vtout import SourceNtoOne
import functions
import lib.inoutparsing
import os
from itertools import izip , imap
import itertools
import cPickle as cPickle
import struct
import gc
import StringIO as StringIO
import cStringIO as cStringIO
import marshal
import zlib
from array import array




BLOCK_SIZE = 65536000
ZLIB = "zlib"
BZ2 = "bzip"
RCFILE=1
SDC=2
SPAC=3
registered=True

def getSize(v):
    t = type(v)

    if t == unicode:
        return 52 + 4*len(v)

    if t in (int, float, None):
        return 24

    return 37 + len(v)

def outputData(diter, schema, connection, *args, **formatArgs):
    import bz2
    import msgpack
    serializer = msgpack
    ### Parameter handling ###
    where=None
    mode = 'sdc'
    compression = 'zlib'
    level = 2
    split = 0
    if 'split' in formatArgs:
        split = 1
    if len(args)>0:
        where=args[0]
    elif 'file' in formatArgs:
        where=formatArgs['file']
    else:
        raise functions.OperatorError(__name__.rsplit('.')[-1],"No destination provided")
    if 'file' in formatArgs:
        del formatArgs['file']
    if 'mode' in formatArgs:
        mode = formatArgs['mode']
    if 'compr'  in formatArgs:
        if formatArgs['compr'] == "zlib":
            compression = ZLIB
        elif formatArgs['compr'] == "bz2":
            compression = BZ2
        else:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Wrong compression algorithm provided. Choose between zlib or bz2")

    if 'level' in formatArgs:
        l = formatArgs['level']
        try:
            if int(l)>=0 and int(l) <=9 :
                level = int(l)
            else :
                raise functions.OperatorError(__name__.rsplit('.')[-1],"Compression level should range from 0 to 9")
        except:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"Compression level should range from 0 to 9")
    filename, ext=os.path.splitext(os.path.basename(where))
    fullpath=os.path.split(where)[0]
    if split == 0:
        fileIter=open(where, "w+b")
        fastPickler = cPickle.Pickler(fileIter, 1)
        fastPickler.fast = 1
    else:
        fileIter = 1


  
    def spac(fileObject,lencols):
        colnum = len(schema)-1
        serializer.dump(schema[1:],fileObject)
        setcol = [set([]) for _ in xrange(colnum)]
        dictsize = 65536
        paxcols = []
        indextype = 'H'
        index_init = [0 for _ in xrange(3)]
        coldicts = [{} for _ in xrange(colnum)]
        prevsets =  [[] for _ in xrange(colnum)]
        count = 0
        blocknum = 0
        compress = bz2.compress
        

        while True:
            maxlen = 0
            exitGen = False
            rows = []
            try:
                for i in xrange(lencols):
                    rows.append((yield))
            except GeneratorExit:
                exitGen = True
            listofvals = zip(*rows)

            if listofvals!=[]:

                for i,col in enumerate(listofvals):
                    if i not in paxcols:
                        setcol[i].update(col)

                prev = fileObject.tell() + 8*(colnum+2)
                output = cStringIO.StringIO()
                headindex = [0 for _ in xrange(colnum+2)]

                if blocknum == 0:
                    for i in xrange(colnum):
                        headindex[i] = output.tell() + prev
                        if (len(setcol[i])*1.0/lencols>0.67):
                            paxcols.append(i)
                            l = index_init[:]
                            t = output.tell()
                            output.write(struct.pack('L'*len(l), *l))
                            output.write(compress(serializer.dumps(listofvals[i]),2))
                            l[0] = output.tell()
                            output.seek(t)
                            output.write(struct.pack('L'*len(l), *l))
                            output.seek(l[0])
                        else:
                            prevsets[i] = list(set(setcol[i]).copy())
                            coldicts[i] = dict(((x,y) for y,x in enumerate(prevsets[i])))
                            coldict = coldicts[i]
                            if len(prevsets[i])<256:
                                indextype='B'
                            else:
                                indextype='H'
                            l = index_init[:]
                            t = output.tell()
                            output.write(struct.pack('L'*len(l), *l))
                            output.write(compress(serializer.dumps(prevsets[i]),2))
                            l[0] = output.tell()
                            output.write(compress(array(indextype,[coldict[val] for val in listofvals[i]] ).tostring()))
                            l[1] = output.tell()
                            output.seek(t)
                            output.write(struct.pack('L'*len(l), *l))
                            output.seek(l[1])
                else:
                    for i in xrange(colnum):
                        headindex[i] = output.tell() + prev
                        if i in paxcols:
                            l = index_init[:]
                            t = output.tell()
                            output.write(struct.pack('L'*len(l), *l))
                            output.write(compress(serializer.dumps(listofvals[i]),2))
                            l[0] = output.tell()
                            output.seek(t)
                            output.write(struct.pack('L'*len(l), *l))
                            output.seek(l[0])
                            
                        else:
                            pset = set(prevsets[i])
                            difnew = list(setcol[i] - pset)
                            s = prevsets[i] + difnew
                            d = 0
                            if len(s) > dictsize:
                                difold = list(pset - setcol[i])
                                while len(s)>dictsize:
                                    s.remove(difold[d])
                                    d+=1

                            prevsets[i] = s
                            coldicts[i] = dict(((x,y) for y,x in enumerate(s)))
                            coldict = coldicts[i]
                            towritevalues = (x for x in xrange(len(coldict)-d, len(coldict)))

                            
                            l = index_init[:]
                            t = output.tell()
                            output.write(struct.pack('L'*len(l), *l))
                            if len(prevsets[i]) != 0 :
                                if len(prevsets[i])<256:
                                    indextype='B'
                                else:
                                    indextype='H'
                                output.write(compress(serializer.dumps(difnew),2))
                                l[0] = output.tell()
                                output.write(compress(array(indextype,towritevalues).tostring()))
                                l[1] = output.tell()

                            output.write(compress(array(indextype,[coldict[val] for val in listofvals[i]] ).tostring()))
                            l[2] = output.tell()
                            output.seek(t)
                            output.write(struct.pack('L'*len(l), *l))
                            output.seek(l[2])

                headindex[i+1] = output.tell()+ prev
                headindex[i+2] = count
                count=0
                fileObject.write(struct.pack('L'*len(headindex), *headindex))
                fileObject.write(output.getvalue())
                for s in setcol:
                    s.clear()
                gc.collect()
                blocknum+=1

            if exitGen:
                fileObject.close()
                break


    def sorteddictpercol(fileIter,lencols,compression,level):
        output = StringIO.StringIO()
        if split:
            output.write(struct.pack('!B', 0))
            cPickle.dump(schema[1:],output,1)
            colnum = len(schema)-1
            cz = output.getvalue()
            fileIter.write(struct.pack('!i', len(cz)))
            fileIter.write(cz)
            
        else:
            colnum = len(schema)
            fileIter.write(struct.pack('!B', 0))
            cPickle.dump(schema,fileIter,1)
        if hasattr(sys, 'pypy_version_info'):
            from __pypy__ import newlist_hint
            
        else:
            newlist_hint = lambda size: []
        paxcols = []
        blocknum = 0
        
#        tempio = cStringIO.StringIO()
#        fastPickler = cPickle.Pickler(tempio, 2)
#        fastPickler.fast = 1
        exitGen=False
        compress = zlib.compress
        if compression == BZ2:
            compress = bz2.compress
        if lencols == 0:
            (yield)

        
        while not exitGen:
            output.truncate(0)
            mrows = newlist_hint(lencols)
            try:
                for i in xrange(lencols):
                    mrows.append((yield))
            except GeneratorExit:
                exitGen = True
            
            count = len(mrows)
            output.write(struct.pack('!B', 1))
            if compression == BZ2:
                output.write(struct.pack('!B', 0))
            else:
                output.write(struct.pack('!B', 1))
            
            headindex = [0 for _ in xrange((colnum*2)+1)]
            type = '!'+'i'*len(headindex)
            output.write(struct.pack(type, *headindex))

            if mrows != []:
                
                for i, col in enumerate(([x[c] for x in mrows] for c in xrange(colnum))):

                    if blocknum==0:
                        s = sorted(set(col))
                        lens = len(s)
                        if lens>50*1.0*count/100:
                            paxcols.append(i)
                            l = output.tell()
#                            tempio.truncate(0)
#                            fastPickler.dump(col)
                            output.write(compress(serializer.dumps(col),level))
                            headindex[i*2] = output.tell() - l
                        else:
                            coldict = dict(((x,y) for y,x in enumerate(s)))
                            l = output.tell()
#                            tempio.truncate(0)
#                            fastPickler.dump(s)
                            output.write(compress(serializer.dumps(s),level))
                            headindex[i*2] = output.tell()-l
                            if lens>1:
                                if lens<256:
                                    output.write(compress(array('B',[coldict[y] for y in col]).tostring(),level))
                                else:
                                    output.write(compress(array('H',[coldict[y] for y in col]).tostring(),level))
                            headindex[i*2+1] = output.tell()-l-headindex[i*2]
                    else:
                        if i in paxcols:
                            l = output.tell()
#                            tempio.truncate(0)
#                            fastPickler.dump(col)
                            output.write(compress(serializer.dumps(col),level))
                            headindex[i*2] = output.tell() - l
                        else:
                            s = sorted(set(col))
                            lens = len(s)
                            coldict = dict(((x,y) for y,x in enumerate(s)))
                            l = output.tell()
#                            tempio.truncate(0)
#                            fastPickler.dump(s)
                            output.write(compress(serializer.dumps(s),level))
                            headindex[i*2] = output.tell()-l
                            if lens>1:
                                if lens<256:
                                    output.write(compress(array('B',[coldict[y] for y in col]).tostring(),level))
                                else:
                                    output.write(compress(array('H',[coldict[y] for y in col]).tostring(),level))
                            headindex[i*2+1] = output.tell()-l-headindex[i*2]

                blocknum=1
                headindex[colnum*2] = count
                output.seek(0)
                type = '!'+'i'*len(headindex)
                output.write(struct.pack('!B', 1))
                if compression == BZ2:
                    output.write(struct.pack('!B', 0))
                else:
                    output.write(struct.pack('!B', 1))
                output.write(struct.pack(type, *headindex))
                cz = output.getvalue()
                fileIter.write(struct.pack('!i',len(cz)))
                fileIter.write(cz)
        fileIter.close()


   

    def rcfile(fileObject,lencols,compression,level):
        colnum = len(schema) - 1
        structHeader = '!'+'i' * colnum
        indexinit = [0 for _ in xrange(colnum)]
        fileObject.write(struct.pack('!B', 0))
        cPickle.dump(schema[1:],fileObject,1)
#        l = cStringIO.StringIO()
#        fastPickler = cPickle.Pickler(l, 2)
#        fastPickler.fast = 1
        exitGen = False
        compress = zlib.compress
        if compression == BZ2:
            compress = bz2.compress
        if lencols == 0:
            (yield)
            
        while not exitGen:
            rows = []
            try:
                for i in xrange(lencols):
                    rows.append((yield))
            except GeneratorExit:
                exitGen = True
                
            index = indexinit[:]
            output = cStringIO.StringIO()
            
            output.write(struct.pack('!B', 1))
            output.write(struct.pack(structHeader, *index))
            if rows != []:
                for i, col in enumerate(([x[c] for x in rows] for c in xrange(colnum))):
#                    l.truncate(0)
#                    fastPickler.dump(col)
                    cz = zlib.compress(serializer.dumps(col), 5)
                    output.write(cz)
                    index[i] = len(cz)
                output.seek(1)
                output.write(struct.pack(structHeader, *index))
                fileObject.write(output.getvalue())
        fileObject.close()

   

    def calclencols(mode):
            if mode==RCFILE:
                count = 0
                bsize = 0
                rows = []
                try:
                    while bsize<BLOCK_SIZE:
                        row = diter.next()
                        rows.append(row)
                        count += 1
                        if split:
                            bsize += sum((getSize(v) for v in row[1:]))
                        else:
                            bsize += sum((getSize(v) for v in row))
                except StopIteration:
                    pass
                return count+10*count/100 , rows
            if mode==SDC or mode==SPAC:
                count = 0
                bsize = 0
                rows = []
                try:
                    while bsize<BLOCK_SIZE and count<65535:
                        row = diter.next()
                        rows.append(row)
                        count += 1
                        if split:
                            bsize += sum((getSize(v) for v in row[1:]))
                        else:
                            bsize += sum((getSize(v) for v in row))
                        

                except StopIteration:
                    pass
                return count , rows


    if mode == 'spac':
        if 'split' in formatArgs:
            filesNum = int(formatArgs['split'])
            filesList = [None]*filesNum
            lencols , rows = calclencols(SPAC)
            for key in xrange(int(formatArgs['split'])) :
                filesList[key] = open(os.path.join(fullpath, filename+'.'+str(key)), 'a')

            spacgen = [spac(x,lencols) for x in filesList]
            spacgensend = [x.send for x in spacgen]
            for j in spacgensend:
                j(None)
            for row in rows:
                spacgensend[row[0]](row[1:])
            del(rows)
            for row in diter:
                spacgensend[row[0]](row[1:])
            for j in spacgen:
                j.close()



    elif mode == 'sdc':
        if 'split' in formatArgs:
            filesNum = int(formatArgs['split'])
            filesList = [None]*filesNum
            lencols , rows = calclencols(SDC)
            for key in xrange(int(formatArgs['split'])) :
                filesList[key] = open(os.path.join(fullpath, filename+'.'+str(key)), 'wb')
            sdcgen = [sorteddictpercol(x,lencols,compression,level) for x in filesList]
            sdcgensend = [x.send for x in sdcgen]
            for j in sdcgensend:
                j(None)
            for row in rows:
                sdcgensend[row[0]](row[1:])
            del(rows)
            for row in diter:
                sdcgensend[row[0]](row[1:])
            for j in sdcgen:
                j.close()
        else:
            lencols , rows = calclencols(SDC)
            sdcgen = sorteddictpercol(fileIter,lencols,compression,level)
            sdcgensend = sdcgen.send
            sdcgensend(None)
            for row in rows:
                sdcgensend(row)
            del(rows)
            for row in diter:
                sdcgensend(row)
            sdcgen.close()
                

    
    elif mode == 'rcfile':
        if 'split' in formatArgs:
            filesNum = int(formatArgs['split'])
            filesList = [None]*filesNum
            lencols , rows = calclencols(RCFILE)
            for key in xrange(int(formatArgs['split'])) :
                filesList[key] = open(os.path.join(fullpath, filename+'.'+str(key)), 'wb')


            rcgen = [rcfile(x,lencols,compression,level) for x in filesList]
            rcgensend = [x.send for x in rcgen]
            for j in rcgensend:
                j(None)
            for row in rows:
                rcgensend[row[0]](row[1:])
            del(rows)
            for row in diter:
                rcgensend[row[0]](row[1:])
            for j in rcgen:
                j.close()

    elif mode :
        raise functions.OperatorError(__name__.rsplit('.')[-1],"Wrong compression format provided.Choose between sdc,rcfile or spac")



     
    try:
        if 'split' not in formatArgs:
            fileIter.close()
    except NameError:
        pass

boolargs=lib.inoutparsing.boolargs+['compression']


def Source():
    global boolargs, nonstringargs
    return SourceNtoOne(outputData,boolargs, lib.inoutparsing.nonstringargs,lib.inoutparsing.needsescape, connectionhandler=True)


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
