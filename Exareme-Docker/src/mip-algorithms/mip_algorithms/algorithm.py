import json
import logging
import os
from string import capwords

from mip_algorithms import logged
from mip_algorithms.decorators import algorithm_methods_decorator
from mip_algorithms.parameters import Parameters, parse_exareme_args
from mip_algorithms.transfer import AddMe, MaxMe, MinMe, ConcatMe, DoNothing, TransferStruct
from mip_algorithms.utils import one_kwarg

_ALLOWED_METHODS = {'local_', 'global_'}


class AlgorithmMeta(type):
    def __new__(mcs, name, bases, attrs):
        for key, attr in attrs.items():
            if callable(attr):
                # todo understand: if ifs order is changed I get
                #   File "/root/mip-algorithms/PEARSON_EXPERIMENTAL/pearson.py", line 20, in local_
                #       left_vars = right_vars = self.data.variables
                #       AttributeError: 'NoneType' object has no attribute 'variables'
                if hasattr(attr, '__name__'):
                    if attr.__name__ not in (_ALLOWED_METHODS | {'__repr__', '__init__', '__str__'}):
                        attrs[key] = logged(attr)
                    if attr.__name__ in _ALLOWED_METHODS:
                        attrs[key] = logged(algorithm_methods_decorator(attr))
        return type.__new__(mcs, name, bases, attrs)


class Algorithm(object):
    __metaclass__ = AlgorithmMeta

    def __init__(self, alg_file, cli_args):
        self._folder_path, name = os.path.split(alg_file)
        self._name = os.path.splitext(name)[0]
        self._args = parse_exareme_args(self._folder_path, cli_args)
        self.parameters = Parameters(self._args)
        self.data = None
        self._transfer_struct = TransferStruct()
        self._state = None
        self._termination = False
        self.result = None

    def __repr__(self):
        name = ''.join(capwords(self._name, '_').split('_'))
        return '{name}()'.format(name=name)

    def set_algorithms_output_data(self):
        try:
            print(json.dumps(self.result.output(), allow_nan=False))
        except ValueError:
            print('Result contains NaNs.')

    @one_kwarg
    def push_and_aggree(self, **kwarg):
        self._transfer_struct.register(DoNothing, **kwarg)

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
            logging.debug('Fetching: {0}'.format(ret))
            return ret
        except KeyError:
            logging.error('Cannot fetch unknown variable.')
            raise
        except TypeError:
            logging.error('{} is not a variable name.'.format(name))
            raise

    @one_kwarg
    def store(self, **kwargs):
        self._state.register(**kwargs)

    def load(self, name):
        return getattr(self._state, name=name)

    def terminate(self):
        self.store(termination=True)

    def termination_condition(self):
        if self._termination:
            print('STOP')
        else:
            print('CONTINUE')

    def execute(self, input_args):  # todo Make mixin classes for local-global etc with different init and execute
        #    classes and make specific algorithms inherit them for debugging
        pass


class LocalGlobal(Algorithm):
    def local_(self):
        raise NotImplementedError

    def global_(self):
        raise NotImplementedError
