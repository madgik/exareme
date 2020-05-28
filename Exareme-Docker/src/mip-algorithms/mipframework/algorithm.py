import json
import logging
import os
import re
from string import capwords
import warnings

from .loggingutils import logged
from .decorators import algorithm_methods_decorator
from .parameters import Parameters, parse_exareme_args
from .transfer import AddMe, MaxMe, MinMe, ConcatMe, DoNothing, TransferStruct
from .helpers import one_kwarg

_MAIN_METHODS = re.compile(
    r"""^((local_|global_)
        (init|step(_[2-9])?|final|)?
        |termination_condition)$""",
    re.VERBOSE,
)


class MetaAlgorithm(type):
    def __new__(mcs, name, bases, attrs):
        for key, attr in attrs.items():
            if cannot_be_decorated(attr):
                continue
            if can_be_logged(attr):
                attrs[key] = logged(attr)
            if in_main_methods(attr):
                attrs[key] = logged(algorithm_methods_decorator(attr))
        return type.__new__(mcs, name, bases, attrs)


def cannot_be_decorated(attr):
    return not callable(attr) or not hasattr(attr, "__name__")


def can_be_logged(attr):
    return attr.__name__ not in {
        "__init__",
        "__repr__",
        "__str__",
    } and not _MAIN_METHODS.match(attr.__name__)


def in_main_methods(attr):
    return _MAIN_METHODS.match(attr.__name__)


class Algorithm(object):
    __metaclass__ = MetaAlgorithm

    def __init__(self, alg_file, cli_args, intercept=True, privacy=True):
        warnings.filterwarnings("ignore")
        self._folder_path, name = os.path.split(alg_file)
        self._name = os.path.splitext(name)[0]
        self._args = parse_exareme_args(self._folder_path, cli_args)
        self._args.intercept = intercept
        self._args.privacy = privacy
        self.parameters = Parameters(self._args)
        self.data = None
        self.metadata = None
        self._transfer_struct = TransferStruct()
        self._state = None
        self._termination = False
        self.result = None

    def __repr__(self):
        name = type(self).__name__
        return "{name}()".format(name=name)

    def set_output(self):
        try:
            res = json.dumps(self.result.output())
            logging.debug("Algorithm output:\n {res}".format(res=res, indent=4))
            print(json.dumps(self.result.output(), allow_nan=True, indent=4))
        except ValueError:
            logging.error("Result contains NaNs.")
            raise
        except TypeError:
            logging.error(
                "Cannot JSON serialize {res}".format(res=self.result.output())
            )
            raise

    @one_kwarg
    def push(self, **kwarg):
        self._transfer_struct.register(DoNothing, **kwarg)

    push_and_agree = push

    @one_kwarg
    def push_and_add(self, **kwarg):
        self._transfer_struct.register(AddMe, **kwarg)

    @one_kwarg
    def push_and_min(self, **kwarg):
        self._transfer_struct.register(MinMe, **kwarg)

    @one_kwarg
    def push_and_max(self, **kwarg):
        self._transfer_struct.register(MaxMe, **kwarg)

    @one_kwarg
    def push_and_concat(self, **kwarg):
        self._transfer_struct.register(ConcatMe, **kwarg)

    def fetch(self, name):
        try:
            ret = self._transfer_struct[name]
            logging.debug("Fetching: {0}".format(ret))
            return ret
        except KeyError:
            logging.error("Cannot fetch unknown variable.")
            raise
        except TypeError:
            logging.error("{} is not a variable name.".format(name))
            raise

    @one_kwarg
    def store(self, **kwargs):
        self._state.register(**kwargs)

    def load(self, name):
        return getattr(self._state, name)

    def terminate(self):
        self.store(termination=True)

    def termination_condition(self):
        if self._termination:
            print("STOP")
        else:
            print("CONTINUE")
