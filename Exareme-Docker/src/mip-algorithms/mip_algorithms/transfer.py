import codecs
import logging
import pickle
import sqlite3

import numpy as np
from mip_algorithms import logged, _LOGGING_LEVEL_ALG
from mip_algorithms.exceptions import TransferError


class TransferRule(object):
    def __init__(self, val):
        self.val = val

    def __add__(self, other):
        raise NotImplementedError

    def __repr__(self):
        cls_name = '{}'.format(self.__class__)[:-2].split('.')[-1]  # fixme find better solution
        return '{cls}({val})'.format(cls=cls_name, val=self.val)


class AddMe(TransferRule):
    @logged
    def __add__(self, other):
        return AddMe(self.val + other.val)


class MaxMe(TransferRule):
    @logged
    def __add__(self, other):
        return MaxMe(max(self.val, other.val))


class MinMe(TransferRule):
    @logged
    def __add__(self, other):
        return MinMe(min(self.val, other.val))


class ConcatMe(TransferRule):
    @logged
    def __add__(self, other):
        if type(self.val) == list and type(other.val) == list:
            return ConcatMe(self.val + other.val)
        elif type(self.val) == np.ndarray and type(other.val) == np.ndarray:
            return ConcatMe(np.concatenate((self.val, other.val)))


class DoNothing(TransferRule):
    @logged
    def __add__(self, other):
        if self.val != other.val:
            raise TransferError('Local nodes do not agree on common variable: {0}, {1}'.format(self.val, other.val))


class TransferStruct(object):
    def __init__(self, **kwargs):
        for name, val in kwargs.items():
            setattr(self, name, val)

    @logged
    def __add__(self, other):
        return TransferStruct(**{name: val + other.__dict__[name] for name, val in self.__dict__.items()})

    def __getitem__(self, name):
        return getattr(self, name).val

    def __repr__(self):
        if _LOGGING_LEVEL_ALG == logging.INFO:
            return 'TransferStruct()'
        elif _LOGGING_LEVEL_ALG == logging.DEBUG:
            r = 'TransferStruct('
            for k, v in self.__dict__.items():
                r += '\n{k}={v}, '.format(k=k, v=v)
            r += '\n)'
            return r

    def register_for_transfer(self, rule_cls, **kwarg):
        name, var = kwarg.popitem()
        setattr(self, name, rule_cls(var))

    @logged
    def transfer_all(self):
        print(codecs.encode(pickle.dumps(self), 'ascii'))
        # with open('transfer_db.txt', 'w') as f:
        #     f.write(codecs.encode(pickle.dumps(self), 'ascii'))

    @classmethod
    def fetch_all(cls, transfer_db):  # TODO replace with sqlalchemy
        conn = sqlite3.connect(transfer_db)
        cur = conn.cursor()
        cur.execute("SELECT data FROM transfer")  # fixme avoid sql literals!
        first = True
        result = None
        for row in cur:
            if first:
                result = pickle.loads(codecs.decode(row[0], 'ascii'))
                first = False
            else:
                result += pickle.loads(codecs.decode(row[0], 'ascii'))
        return result
        # with open(transfer_db, 'r') as f:
        #     content = f.read()
        #     return pickle.loads(codecs.decode(content, 'ascii'))