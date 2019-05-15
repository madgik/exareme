import sqlite3
import pickle
import codecs


class TransferData(object):
    def __add__(self, other):
        raise NotImplementedError('The __add__ method should be implemented by the child class.')

    @classmethod
    def load(cls, inputDB):
        conn = sqlite3.connect(inputDB)
        cur = conn.cursor()

        cur.execute('SELECT results FROM transfer')

        first = True
        for row in cur:
            if first:
                result = pickle.loads(codecs.decode(row[0], 'ascii'))
                first = False
            else:
                result += pickle.loads(codecs.decode(row[0], 'ascii'))
        return result

    def transfer(self):
        print codecs.encode(pickle.dumps(self), 'ascii')


class StateData(object):  # TODO Call save in constructor to simplify algorithm code??

    def __init__(self, **kwargs):
        self.data = kwargs

    def get_data(self):
        return self.data

    def save(self, fname, pickle_protocol=2):
        with open(fname, 'wb') as file:
            try:
                pickle.dump(self, file, protocol=pickle_protocol)
            except pickle.PicklingError:
                print 'Unpicklable object.'

    @classmethod
    def load(cls, fname):
        with open(fname, 'rb') as file:
            try:
                obj = pickle.load(file)
            except pickle.UnpicklingError:
                print 'Cannot unpickle.'
                return
        return obj


def get_parameters(argv):
    opts = {}
    while argv:
        if argv[0][0] == '-':
            opts[argv[0]] = argv[1]
            argv = argv[2:]
        else:
            argv = argv[1:]
    return opts


def set_algorithms_output_data(data):
    print data
