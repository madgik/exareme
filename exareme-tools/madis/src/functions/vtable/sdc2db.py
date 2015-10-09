import os.path
import sys
import functions
import os
from itertools import repeat, imap
import cPickle
import cStringIO
import vtbase
import functions
import struct
import vtbase
import os
import re
import zlib
import apsw
from array import array
import marshal


if hasattr(sys, 'pypy_version_info'):
    from __pypy__ import newlist_hint

    def izip(*iterables):
        # izip('ABCD', 'xy') --> Ax By
        iterators = tuple(map(iter, iterables))
        ilen = len(iterables)
        res = [None] * ilen
        while True:
            ci = 0
            while ci < ilen:
                res[ci] = iterators[ci].next()
                ci += 1
            yield res
else:
    from itertools import izip
    newlist_hint = lambda size: []
    
registered=True



def imapm(function, iterable):
    # imap(pow, (2,3,10), (5,2,3)) --> 32 9 1000
    it = iter(iterable)
    while True:
        yield function(it.next())

def repeatm(object, times):
    for i in xrange(times):
        yield object

class SDC2DB(vtbase.VT):


    def VTiter(self, *args,**formatArgs):
        import msgpack
        import bz2
        serializer = msgpack
        largs, dictargs = self.full_parse(args)
        where = None
        mode = 'row'
        input = cStringIO.StringIO()

        if 'file' in dictargs:
            where=dictargs['file']
        else:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No destination provided")
        col = 0

        filename, ext=os.path.splitext(os.path.basename(where))
        if 'cols' in dictargs:
            a = re.split(' |,| , |, | ,' , dictargs['cols'])
            column = [x for x in a if x != '']
        else:
            col = 1
        start = 0
        end = sys.maxint-1
        if 'start' in dictargs:
            start = int(dictargs['start'])
        if 'end' in dictargs:
            end = int(dictargs['end'])

        fullpath = str(os.path.abspath(os.path.expandvars(os.path.expanduser(os.path.normcase(where)))))

        fileIterlist = []
        for x in xrange(start,end+1):
            try:
                fileIterlist.append(open(fullpath+"."+str(x), "rb"))
            except:
                break

        if fileIterlist == []:
            try:
                fileIterlist = [open(where, "rb")]
            except :
                raise  functions.OperatorError(__name__.rsplit('.')[-1],"No such file")
        cursor = []
        for filenum,fileIter in enumerate(fileIterlist):
            blocksize = struct.unpack('!i',fileIter.read(4))
            b = struct.unpack('!B',fileIter.read(1))
            schema = cPickle.load(fileIter)
            colnum = len(schema)
            if filenum == 0:
                yield schema
                def createdb(where, tname, schema, page_size=16384):
                        c=apsw.Connection(where)
                        cursor=c.cursor()
                        list(cursor.execute('pragma page_size='+str(page_size)+';pragma cache_size=-1000;pragma legacy_file_format=false;pragma synchronous=0;pragma journal_mode=OFF;PRAGMA locking_mode = EXCLUSIVE'))
                        create_schema='create table '+tname+' ('
                        create_schema+='`'+unicode(schema[0][0])+'`'+ (' '+unicode(schema[0][1]) if schema[0][1]!=None else '')
                        for colname, coltype in schema[1:]:
                            create_schema+=',`'+unicode(colname)+'`'+ (' '+unicode(coltype) if coltype!=None else '')
                        create_schema+='); begin exclusive;'
                        list(cursor.execute(create_schema))
                        insertquery="insert into "+tname+' values('+','.join(['?']*len(schema))+')'
                        return c, cursor, insertquery
                cur, cursor, insertquery=createdb(where+".db", filename, schema)
            input = cStringIO.StringIO()

           
            while True:
                    input.truncate(0)
                    try:
                        blocksize = struct.unpack('!i', fileIter.read(4))
                    except:
                        break
                    if blocksize[0]:
                        input.write(fileIter.read(blocksize[0]))
                        input.seek(0)
                        b = struct.unpack('!B', input.read(1))
                        if b[0]:
                            decompression = struct.unpack('!B', input.read(1))
                            if decompression[0] :
                                decompress = zlib.decompress
                            else:
                                decompress = bz2.decompress

                            type = '!'+'i'*(colnum*2+1)
                            ind = list(struct.unpack(type, input.read(4*(colnum*2+1))))

                            cols = [None]*colnum
                            for c in xrange(colnum):
                                s = serializer.loads(decompress(input.read(ind[c*2])))
                                if (len(s)>1 and ind[c*2+1]==0 and ind[colnum*2]>1):
                                    cols[c] = s
                                else:
                                    if len(s)==1:
                                        tmp = s[0]
                                        cols[c] = repeat(tmp, ind[colnum*2])
                                    elif len(s)<256:
                                        cols[c] = imap(s.__getitem__, array('B', decompress(input.read(ind[c*2+1]))))
                                    else:
                                        cols[c] = imap(s.__getitem__, array('H', decompress(input.read(ind[c*2+1]))))

    #                        for r in izip(*cols):
    #                            pass
                            cursor.executemany(insertquery, izip(*cols))

                        elif not b[0]:
                            schema = cPickle.load(fileIter)
        list(cursor.execute('commit'))
        cur.close()
        try:
            for fileObject in fileIterlist:
                fileObject.close()
        except NameError:
            pass

def Source():
    return vtbase.VTGenerator(SDC2DB)

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



