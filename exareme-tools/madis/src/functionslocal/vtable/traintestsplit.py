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

        # get arguments

        if 'test_size'  in dictargs:
            if dictargs['test_size'] != "None":
                if '.' not in  str(dictargs['test_size']):
                     self.test_size = int(dictargs['test_size'])
                else:
                     self.test_size = float(dictargs['test_size'])
            else:
                self.test_size = None
        else:
            self.test_size = None

        if 'train_size'  in dictargs:
            if dictargs['train_size'] != "None":
                 if '.' not in  str(dictargs['train_size']):
                     self.train_size = int(dictargs['train_size'])
                 else:
                     self.train_size = float(dictargs['train_size'])
            else:
                self.train_size = None
        else:
            self.train_size = None

        if 'random_state'  in dictargs:
            if dictargs['random_state'] != "None":
                self.random_state = int(dictargs['random_state'])
            else:
                self.random_state = None
        else:
            self.random_state = None

        if 'shuffle'  in dictargs:
            if dictargs['shuffle'] != "None":
                self.shuffle = bool(dictargs['shuffle'])
            else:
                self.shuffle = True
        else:
            self.shuffle = None

        # print largs
        # print dictargs

        self.data = []

        if 'query' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No query argument ")
        query=dictargs['query']

        cur = envars['db'].cursor()
        c=cur.execute(query)

        for r in c:
            if r[0].isdigit():
                self.data.append(r[0])
            else:
                self.data.append(str(r[0]))

        X = np.array(self.data)
        y =range(len(self.data))

        # print self.test_size, self.train_size, self.random_state, self.shuffle

        yield [('rid',), ('idofset',)]

        try:
            X_train, X_test, y_train, y_test =  train_test_split(X,y, test_size = self.test_size, train_size=self.train_size, random_state=self.random_state)

        except ValueError as e:
                yield(-1,str(e))

        finally:
            # print X_train
            # print X_test
            # print y_train
            # print y_test
            for i in X_train: yield (i,'Train')
            for i in X_test: yield (i,'Test')






def Source():
    return functions.vtable.vtbase.VTGenerator(traintestsplit)

