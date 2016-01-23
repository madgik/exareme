# """
# .. function:: sktrain (args,query:None)
#
# sktrain filename: "mymodel" initstr:"initstr" select * from t;
#
#     Implements the supervised algorithm initialized by initstr fitting the data ("training") provided by table t. All the columns comprise the training set except for the last one which involves the target values (continuous for regression, classes for classification). A model (named mymodel) is constructed in order to be used later for the prediction of new unlabeled data. The model has been serialized and compressed.
#
#     Examples:
#     Next query initializes a support vector classifier, trains the data given by table t and finally a model, named SVMmodel, is constructed.
#
#
#     >>> table1('''
#     ... 0.0   4.4   0
#     ... 2.1   2.2   2
#     ... -2.1   4.4   0
#     ... 2.1   2.2   0
#     ... 0.0   4.4   2
#     ... -4.2   4.4   2
#     ... -4.2   4.4   1
#     ... -2.1   -0.0   0
#     ... 2.1   -0.0   0
#     ... -2.1   -2.2   0
#     ... -4.2   -0.0   2
#     ... ''')
#
#     First 2 columns consist of the training data (two features) and the last one depicts the class each dato belongs to.
#
#     sql("sktrain filename:SVMmodel initstr:SVC(kernel='linear')")
#     sktrain filename:SVMmodel initstr:SVC(kernel='linear') select * from table1;
#     ------------------------------
#
# """
#
#
# registered = True
# __author__ = 'root'
# import os.path
# import sys
# import setpath
# from vtout import SourceNtoOne
# import functions
# import lib.inoutparsing
#
#
# def outdata(diter, schema, connection, *args, **formatArgs):
#     # -- IMPORT MODULES ---
#     import itertools
#     from sklearn.linear_model import *
#     from sklearn.neighbors import *
#     from sklearn.svm import *
#     from sklearn.naive_bayes import *
#     from sklearn.tree import *
#     from sklearn.ensemble import *
#     # from sklearn.cluster import AgglomerativeClustering
#     import cPickle as cp
#     import numpy as np
#     # import unicodedata
#     import zlib
#     # ---------------------
#
#     f = open(formatArgs['filename'], 'w')
#
#     # Make rows -> cols
#     gen = itertools.izip(*diter)
#
#     #Split data into train and target sets:
#     train = itertools.islice(gen,0,len(schema)-1)
#     target = itertools.islice(gen,0,1)
#
#     # Reverse again
#     train = np.array(list(itertools.izip(*train))).astype(np.float)
#     target = np.array(list(itertools.chain(*(itertools.izip(*target))))).astype(np.float) #target1 has 2 dimensions. Scikit expects 1: i.chain(reversed vector)
#
#     # Model initialization and training
#     initalg = eval(formatArgs['initstr']) #initialize model
#     print initalg
#     alg = initalg.fit(train,target) #fit model to data
#     pstr = cp.dumps(alg, 2) # Serialization
#     f.write(zlib.compress(pstr,3)) # Compression
#
#
# boolargs = lib.inoutparsing.boolargs
#
#
# def Source():
#     global boolargs, nonstringargs
#     return SourceNtoOne(outdata, boolargs, lib.inoutparsing.nonstringargs, lib.inoutparsing.needsescape,
#                         connectionhandler=True)
#
#
# if not ('.' in __name__):
#     """
#     This is needed to be able to test the function, put it at the end of every
#     new function you create
#     """
#     import sys
#     import setpath
#     from functions import *
#
#     testfunction()
#     if __name__ == "__main__":
#         reload(sys)
#         sys.setdefaultencoding('utf-8')
#         import doctest
#
#         doctest.testmod()
