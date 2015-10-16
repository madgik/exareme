import sys
from itertools import repeat, imap
import cPickle
import cStringIO
import struct
import zlib
import apsw
from array import array
import marshal
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

registered=True

class Decompression:
    
    def __init__ (self):
        self.blocknumber = 0
        
    def decompressblock(self,inputblock):
        import bz2
        try:
            import msgpack
        except ImportError:
            import marshal as msgpack
        serializer = msgpack
        self.blocknumber += 1
        input = cStringIO.StringIO(inputblock)
        if self.blocknumber == 1 :
            # schema block
           
            b = struct.unpack('!B',input.read(1))
            if not b[0]:
                self.schema = cPickle.load(input)
            else :
                raise error('Not a schema block!')
            yield self.schema

        
        colnum = len(self.schema)
        while True:
            try:
                b = struct.unpack('!B', input.read(1))
            except:
                break
            if b[0]:
                decompression = struct.unpack('!B', input.read(1))
                if decompression[0] :
                    decompress = zlib.decompress
                else:
                    decompress = bz2.decompress

                type = '!'+'i'*(colnum*2+1)
                ind = list(struct.unpack(type, input.read(4*(colnum*2+1))))
                if sum(ind)==0:   # rowstore
                    s = serializer.loads(input.read())
                    for rec in s:
                        yield rec
                else:
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

                    iterators = tuple(map(iter, cols))
                    ilen = len(cols)
                    res = [None] * ilen

                    while True:
                        ci = 0
                        try:
                            while ci < ilen:
                                res[ci] = iterators[ci].next()
                                ci += 1
                            yield res
                        except:
                            break
            elif not b[0]:
                cPickle.load(input)



class Compression:

    def __init__ (self,schema):
        import bz2
        self.schema = schema
        self.paxcols = []
        self.currentalgorithm = 'sdicc'
        self.blocknumber = 0
        self.comprblocknumber = 0
        self.maxlevel = 7
        self.lencols = 0
        self.compressiondict = {
        0:('row',zlib.compress,0),
        1:('sdicc',zlib.compress,0),
        2:('sdicc',zlib.compress,1),
        3:('sdicc',zlib.compress,4),
        4:('sdicc',zlib.compress,5),
        5:('sdicc',zlib.compress,6),
        6:('sdicc',bz2.compress,1),
        7:('sdicc',bz2.compress,9)
        }

    def getmaxlevel(self):
        return self.maxlevel

    def reset(self,schema):
        self.schema = schema
        self.paxcols = []
        self.currentalgorithm = 'sdicc'
        self.blocknumber = 0
        self.comprblocknumber = 0
        self.lencols = 0


    def setlevel(self,formatArgs):
        
         if formatArgs is not None and 'level' in formatArgs:
            level = formatArgs['level']
            self.compress = self.compressiondict[level][1]
            self.level = self.compressiondict[level][2]
            if self.compressiondict[level][0] != self.currentalgorithm:
                self.comprblocknumber = 0
                self.currentalgorithm = self.compressiondict[level][0]
         else :
            self.compress = zlib.compress
            self.level = 1
            if self.currentalgorithm != 'sdicc':
                self.currentalgorithm = 'sdicc'
                self.currentalgorithm = 0

    def getSize(self,v):
        t = type(v)

        if t == unicode:
            return 52 + 4*len(v)

        if t in (int, float, None):
            return 24

        return 37 + len(v)


    def compress(self):

        output = StringIO.StringIO()
        output.write(struct.pack('!B', 0))
        cPickle.dump(self.schema,output,1)

        formatArgs = {}
        formatArgs = (yield)
        exitGen = False
        while not exitGen:
            self.setlevel(formatArgs)
            count = 0
            bsize = 0
            rows = []
            while bsize<BLOCK_SIZE and count<=65535:
                row = yield None
                if row == None:
                    exitGen = True
                    break
                else:
                    rows.append(row)
                    if self.lencols == 0:
                        count += 1
                        bsize += sum((self.getSize(v) for v in row))
                if self.lencols == 0:
                    self.lencols = count
                count = self.lencols
            if self.currentalgorithm == 'sdicc':
                if self.blocknumber > 0 :
                    formatArgs = yield self.sdicc(rows, count)
                else :
                    formatArgs = yield output.getvalue()+self.sdicc(rows, count)
            elif self.currentalgorithm == "row" :
                if self.blocknumber > 0 :
                    formatArgs = yield self.row(rows, count)
                else :
                    formatArgs = yield output.getvalue()+self.row(rows, count)


    def row(self, diter, lencols):
        try:
            import msgpack
        except ImportError:
            import marshal as msgpack
        import bz2
        serializer = msgpack
        output = StringIO.StringIO()
        colnum = len(self.schema)
        output.truncate(0)

        output.write(struct.pack('!B', 1))
        output.write(struct.pack('!B', 0))
        headindex = [0 for _ in xrange((colnum*2)+1)]
        type = '!'+'i'*len(headindex)
        output.write(struct.pack(type, *headindex))
        output.write(serializer.dumps(diter))
        
        return output.getvalue()


    def sdicc(self, diter, lencols):
        try:
            import msgpack
        except ImportError:
            import marshal as msgpack
        import bz2
        serializer = msgpack
        output = StringIO.StringIO()
        colnum = len(self.schema)
        output.truncate(0)

        output.write(struct.pack('!B', 1))
        if self.compress == bz2.compress:
            output.write(struct.pack('!B', 0))
        else:
            output.write(struct.pack('!B', 1))

        headindex = [0 for _ in xrange((colnum*2)+1)]
        type = '!'+'i'*len(headindex)
        output.write(struct.pack(type, *headindex))

        for i, col in enumerate(([x[c] for x in diter] for c in xrange(colnum))):

            if self.blocknumber==0:
                s = sorted(set(col))
                lens = len(s)
                if lens>50*1.0*lencols/100:
                    self.paxcols.append(i)
                    l = output.tell()
#                            tempio.truncate(0)
#                            fastPickler.dump(col)
                    output.write(self.compress(serializer.dumps(col),self.level))
                    headindex[i*2] = output.tell() - l
                else:
                    coldict = dict(((x,y) for y,x in enumerate(s)))
                    l = output.tell()
#                            tempio.truncate(0)
#                            fastPickler.dump(s)
                    output.write(self.compress(serializer.dumps(s),self.level))
                    headindex[i*2] = output.tell()-l
                    if lens>1:
                        if lens<256:
                            output.write(self.compress(array('B',[coldict[y] for y in col]).tostring(),self.level))
                        else:
                            output.write(self.compress(array('H',[coldict[y] for y in col]).tostring(),self.level))
                    headindex[i*2+1] = output.tell()-l-headindex[i*2]
            else:
                if i in self.paxcols:
                    l = output.tell()
#                            tempio.truncate(0)
#                            fastPickler.dump(col)
                    output.write(self.compress(serializer.dumps(col),self.level))
                    headindex[i*2] = output.tell() - l
                else:
                    s = sorted(set(col))
                    lens = len(s)
                    coldict = dict(((x,y) for y,x in enumerate(s)))
                    l = output.tell()
#                            tempio.truncate(0)
#                            fastPickler.dump(s)
                    output.write(self.compress(serializer.dumps(s),self.level))
                    headindex[i*2] = output.tell()-l
                    if lens>1:
                        if lens<256:
                            output.write(self.compress(array('B',[coldict[y] for y in col]).tostring(),self.level))
                        else:
                            output.write(self.compress(array('H',[coldict[y] for y in col]).tostring(),self.level))
                    headindex[i*2+1] = output.tell()-l-headindex[i*2]

        self.blocknumber=1
        headindex[colnum*2] = lencols
        output.seek(0)
        type = '!'+'i'*len(headindex)
        output.write(struct.pack('!B', 1))
        if self.compress == bz2.compress:
            output.write(struct.pack('!B', 0))
        else:
            output.write(struct.pack('!B', 1))
        output.write(struct.pack(type, *headindex))
        return output.getvalue()




