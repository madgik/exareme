"""


"""
import sklearn

registered = True
__author__ = 'root'
import os.path
import sys
from vtout import SourceNtoOne


import setpath
import vtbase
import functions
import gc
import lib.inoutparsing

class sktrain(vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query = dictargs['query']

        if 'classname' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No classname argument ")
        classname = dictargs['classname']

        cur = envars['db'].cursor()
        c = cur.execute(query, parse=False)
        schema = []

        try:
            schema = [x[0] for x in cur.getdescriptionsafe()]
        except StopIteration:
            try:
                raise
            finally:
                try:
                    c.close()
                except:
                    pass
        try:
            #-- IMPORT MODULES ---
            import itertools
            from sklearn.linear_model import *
            from sklearn.neighbors import *
            from sklearn.svm import *
            from sklearn.naive_bayes import *
            from sklearn.tree import *
            from sklearn.ensemble import *
            # from sklearn.cluster import AgglomerativeClustering
            import cPickle as cp
            import numpy as np
            # import unicodedata
            import zlib
            # --------------------

            idclassname = schema.index(classname)
            # print idclassname
            trainList = []
            targetList = []
            for row in c:
                trainList.append(row[0:idclassname]+row[idclassname+1:len(row)])
                targetList.append(round(float(row[idclassname])))

            train = np.array(trainList).astype(np.float)
            target = np.array(targetList).astype(np.float)

            # model = eval(formatArgs['initstr']) #initialize model
            model = sklearn.ensemble.ExtraTreesClassifier()
            model.fit(train, target);

            fet_ind = np.argsort(model.feature_importances_)[::-1]
            fet_imp = model.feature_importances_[fet_ind]

            yield [('colname',), ('val',)]

            del schema[idclassname]
            lr = len(schema)

            # print lr, schema
            for i in xrange(0, lr):
                yield (schema[i], fet_imp[i])
        except Exception as ex:
            import traceback
            raise functions.OperatorError(__name__.rsplit('.')[-1],str(ex) + str(traceback.format_exc()))


def Source():
    return vtbase.VTGenerator(sktrain)
