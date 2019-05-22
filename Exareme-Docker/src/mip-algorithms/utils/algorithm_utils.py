from __future__ import division
from __future__ import print_function

import sqlite3
import pickle
import codecs

__PRIVACY_MAGIC_NUMBER = 10
P_VALUE_CUTOFF = 0.001
P_VALUE_CUTOFF_STR = '< ' + str(P_VALUE_CUTOFF)


class TransferData():
    def __add__(self, other):
        raise NotImplementedError('The __add__ method should be implemented by the child class.')

    @classmethod
    def load(cls, inputDB):
        conn = sqlite3.connect(inputDB)
        cur = conn.cursor()
        cur.execute('SELECT data FROM transfer')
        first = True
        result = None
        for row in cur:
            if first:
                result = pickle.loads(codecs.decode(row[0], 'ascii'))
                first = False
            else:
                result += pickle.loads(codecs.decode(row[0], 'ascii'))
        return result

    def transfer(self):
        print(codecs.encode(pickle.dumps(self), 'ascii'))


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
                print('Unpicklable object.')

    @classmethod
    def load(cls, fname):
        with open(fname, 'rb') as file:
            try:
                obj = pickle.load(file)
            except pickle.UnpicklingError:
                print('Cannot unpickle.')
                return
        return obj


def query_with_privacy(fname_db, query):
    conn = sqlite3.connect(fname_db)
    cur = conn.cursor()
    cur.execute(query)
    schema = [description[0] for description in cur.description]
    data = cur.fetchall()
    if len(data) < __PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Query violates privacy constraint.')
    return schema, data


def set_algorithms_output_data(data):
    print(data)


class PrivacyError(Exception):
    def __init__(self, message):
        super(PrivacyError, self).__init__(message)


class ExaremeError(Exception):
    def __init__(self, message):
        super(ExaremeError, self).__init__(message)
