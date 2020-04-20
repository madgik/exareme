import logging
import pickle

from .loggingutils import logged
from .utils import one_kwarg, make_dirs


class State(object):
    def __init__(self):
        self.termination = False

    def __repr__(self):
        cls_name = type(self).__name__
        return "{}()".format(cls_name)

    @one_kwarg
    def register(self, **kwargs):
        name, var = kwargs.popitem()
        setattr(self, name, var)

    @logged
    def store_all(self, fn_state):
        make_dirs(fn_state)
        with open(fn_state, "wb") as f:
            try:
                pickle.dump(self, f, protocol=2)
            except pickle.PicklingError:
                logging.error("Cannot pickle object.")
                raise

    @classmethod
    @logged
    def load_all(cls, fn_state):
        with open(fn_state, "rb") as f:
            try:
                obj = pickle.load(f)
            except pickle.UnpicklingError:
                logging.error("Cannot unpickle.")
                raise
        return obj
