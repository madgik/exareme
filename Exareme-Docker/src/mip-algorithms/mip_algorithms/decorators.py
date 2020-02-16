import logging
from functools import wraps

from mip_algorithms.data import AlgorithmData
from mip_algorithms.exceptions import UnknownFunctionError
from mip_algorithms.state import State
from mip_algorithms.transfer import TransferStruct


def algorithm_methods_decorator(func):
    func_name = func.__name__
    # Local-Global algs
    if func_name == 'local_':
        wrapper = make_wrapper(node='local', step='first')(func)
    elif func_name == 'global_':
        wrapper = make_wrapper(node='global', step='last')(func)
    # Iterative algs
    elif func_name == 'local_init':
        wrapper = make_wrapper(node='local', step='first', state='init')(func)
    elif func_name == 'global_init':
        wrapper = make_wrapper(node='global', state='init')(func)
    elif func_name == 'local_step':
        wrapper = make_wrapper(node='local', state='load')(func)
    elif func_name == 'global_step':
        wrapper = make_wrapper(node='global', state='load')(func)
    elif func_name == 'local_final':
        wrapper = make_wrapper(node='local', state='load')(func)
    elif func_name == 'global_final':
        wrapper = make_wrapper(node='global', step='last', state='load')(func)
    elif func_name == 'termination_condition':
        wrapper = make_termination_wrapper(func)
    # Error
    else:
        logging.error('Unknown function name.')
        raise UnknownFunctionError(func_name)
    return wrapper


def make_wrapper(node, step=None, state=None):
    assert node in {'local', 'global'}
    assert step in {'first', 'last', None}
    assert state in {'init', 'load', None}
    if node == 'local' and step is None and state != 'load':
        raise ValueError

    def outer_wrapper(func):
        @wraps(func)
        def inner_wrapper(self):
            # Acquire data from DB (if local node)
            if node == 'local':
                self.data = AlgorithmData(self._args)
            # Init or load state
            if state == 'init':
                self._state = State()
            elif state == 'load':
                self._state = State.load_all(fn_state=self._args.prev_state_pkl)
            # Fetch data
            if node == 'local' and step != 'first':
                self._transfer_struct = TransferStruct.fetch_all(transfer_db=self._args.global_step_db)
            elif node == 'global':
                self._transfer_struct = TransferStruct.fetch_all(transfer_db=self._args.local_step_dbs)
            # Execute node method
            func(self)
            # Transfer data
            if node == 'local':
                self._transfer_struct.transfer_all()
            elif node == 'global' and step != 'last':
                self._transfer_struct.transfer_all()
            # Save state
            if state is not None:
                self._state.save_all(fn_state=self._args.cur_state_pkl)
            # Output result
            if node == 'global' and step == 'last':
                self.set_algorithms_output_data()

        return inner_wrapper

    return outer_wrapper


def make_termination_wrapper(func):
    @wraps(func)
    def wrapper(self):
        # Load global state
        self._state = State.load_all(fn_state=self._args.prev_state_pkl)
        # Load `terminate`
        self.termination = self._state['termination']
        # Execute termination condition
        func(self)

    return wrapper
