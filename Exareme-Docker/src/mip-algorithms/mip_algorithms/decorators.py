import logging
from functools import wraps

from mip_algorithms.data import AlgorithmData
from mip_algorithms.exceptions import UnknownFunctionError, AlgorithmError
from mip_algorithms.transfer import TransferStruct


def algorithm_methods_decorator(func):
    func_name = func.__name__
    if func_name == 'local_':
        wrapper = make_local_wrapper(func)
    elif func_name == 'global_':
        wrapper = make_global_wrapper(func)
    else:
        logging.error('Unknown function name.')
        raise UnknownFunctionError(func_name)
    return wrapper


def make_local_wrapper(func):
    @wraps(func)
    def wrapper(self):
        logging.info('Getting data from local db.')
        self.data = AlgorithmData(self._args)
        logging.info('Starting LOCAL EXECUTION')
        func(self)
        logging.info('Transferring data')
        self._transfer_struct.transfer_all()

    return wrapper


def make_global_wrapper(func):
    @wraps(func)
    def wrapper(self):
        self.data = AlgorithmError('There are no data available on the global node. Only local nodes can access '
                                   'data.')  # todo rephrase this
        logging.info('Fetching data.')
        self._transfer_struct = TransferStruct.fetch_all(transfer_db=self._args.local_step_dbs)
        logging.info('Starting GLOBAL EXECUTION')
        func(self)
        logging.info('Setting algorithm output')
        self.set_algorithms_output_data()

    return wrapper
