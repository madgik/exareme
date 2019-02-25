import setpath
import functions
import numpy as np
from sklearn.model_selection import train_test_split

### Classic stream iterator
registered=True

class traintestsplit(functions.vtable.vtbase.VT):
#https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.train_test_split.html
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        self.test_size = 0.25
        self.train_size = None
        self.random_state = None
        self.shuffle = True
        self.stratify  = None

        # get arguments
        if 'test_size'  in dictargs:
            if type(dictargs['test_size']) is int:  self.test_size = int(dictargs['test_size'])
            if type(dictargs['test_size']) is float: self.test_size = float(dictargs['test_size'])
        if 'train_size'  in dictargs:
             if type(dictargs['train_size']) is int: self.train_size = int(dictargs['train_size'])
             if type(dictargs['train_size']) is float: self.train_size = float(dictargs['train_size'])
        if 'random_state'  in dictargs:
            self.random_state = int(dictargs['random_state'])
        if 'shuffle'  in dictargs:
            self.shuffle = bool(dictargs['shuffle'])

        #print largs
        #print dictargs

        self.data = []

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query=dictargs['query']

        cur = envars['db'].cursor()
        c=cur.execute(query)

        for r in c:
            if r[0].isdigit():
                print 'a'
                self.data.append(r[0])
            else:
                print 'b'
                self.data.append(str(r[0]))

        print "data", self.data
        X = np.array(self.data)
        print X
        y =range(len(self.data))
        print y

        print self.test_size, self.train_size, self.random_state, self.shuffle, self.stratify
        X_train, X_test, y_train, y_test =  train_test_split(X,y, test_size = self.test_size, train_size=self.train_size, random_state=self.random_state, shuffle=self.shuffle)

        print X_train
        print X_test
        print y_train
        print y_test

        yield [('rid',), ('idofset',)]


        for i in X_train:
                yield (i,'Train')
        for i in X_test:
                yield (i,'Test')





def Source():
    return functions.vtable.vtbase.VTGenerator(traintestsplit)

