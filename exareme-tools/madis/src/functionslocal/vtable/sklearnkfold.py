import setpath
import functions
import numpy as np
from sklearn.model_selection import KFold

### Classic stream iterator
registered=True

class sklearnkfold(functions.vtable.vtbase.VT):
#https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.KFold.html
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        # get arguments
        if 'splits' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No splits argument.")
        else :
            self.n_splits = int(dictargs['splits'])

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
                self.data.append(r[0])
            else:
                self.data.append(str(r[0]))
        yield [('rid',), ('idofset',)]


        # print "data", self.data
        X = np.array(self.data)
        # print X

        kf = KFold(self.n_splits)
        kf.get_n_splits(X)
        # print"KF", kf

        try:
            for train_index, test_index in kf.split(X):
                # print("TRAIN:", train_index ,"TEST:", test_index)
                j = 0
                for train_index, test_index  in kf.split(X):
                    for k in test_index:
                        yield (self.data[k],j)
                    j += 1
        except:
                yield(-1,"Cannot have number of splits greater than the number of samples")




def Source():
    return functions.vtable.vtbase.VTGenerator(sklearnkfold)


