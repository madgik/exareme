import logging
import re
from functools import wraps

from .data import AlgorithmData
from .exceptions import UnknownFunctionError
from .state import State
from .transfer import TransferStruct

_LOCAL_STEP = re.compile(r"^local_step(_[2-9])?$")
_GLOBAL_STEP = re.compile(r"^global_step(_[2-9])?$")


def algorithm_methods_decorator(func):
    func_name = func.__name__
    # Local-Global algorithms
    if func_name == "local_":
        wrapper = make_wrapper(node="local", step="first")(func)
    elif func_name == "global_":
        wrapper = make_wrapper(node="global", step="last")(func)
    # Iterative algorithms
    elif func_name == "local_init":
        wrapper = make_wrapper(node="local", step="first", state="init")(func)
    elif func_name == "global_init":
        wrapper = make_wrapper(node="global", state="init")(func)
    elif _LOCAL_STEP.match(func_name):
        wrapper = make_wrapper(node="local", state="load")(func)
    elif _GLOBAL_STEP.match(func_name):
        wrapper = make_wrapper(node="global", state="load")(func)
    elif func_name == "local_final":
        wrapper = make_wrapper(node="local", state="load")(func)
    elif func_name == "global_final":
        wrapper = make_wrapper(node="global", step="last", state="load")(func)
    elif func_name == "termination_condition":
        wrapper = make_termination_wrapper(func)
    # Purely local algorithms
    elif func_name == "local_pure":
        wrapper = make_pure_local_wrapper(func)
    # Error
    else:
        logging.error("Unknown function name.")
        raise UnknownFunctionError(func_name)
    return wrapper


def make_wrapper(node, step=None, state=None):
    assert node in {"local", "global"}
    assert step in {"first", "last", None}
    assert state in {"init", "load", None}
    if node == "local" and step is None and state != "load":
        raise ValueError

    def outer_wrapper(func):
        @wraps(func)
        def inner_wrapper(self):

            # Acquire data from DB (if local node)
            if node == "local":
                self.data = AlgorithmData(self._args)
                self.metadata = self.data.metadata
                del self.data.metadata

            # Init or load state
            if state == "init":
                self._state = State()
            elif state == "load":
                self._state = State.load_all(fn_state=self._args.prev_state_pkl)

            # Fetch data
            if node == "local" and step != "first":
                transfer_db = self._args.global_step_db
                self._transfer_struct = TransferStruct.fetch_all(
                    transfer_db=transfer_db
                )
            elif node == "global":
                transfer_db = self._args.local_step_dbs
                self._transfer_struct = TransferStruct.fetch_all(
                    transfer_db=transfer_db
                )

            # Execute node method
            func(self)

            # Transfer data
            if node == "local" or (node == "global" and step != "last"):
                self._transfer_struct.transfer_all()

            # Save state
            if state is not None:
                self._state.store_all(fn_state=self._args.cur_state_pkl)

            # Output result
            if node == "global" and step == "last":
                self.set_output()

        return inner_wrapper

    return outer_wrapper


def make_termination_wrapper(func):
    @wraps(func)
    def wrapper(self):
        # Load global state
        self._state = State.load_all(fn_state=self._args.prev_state_pkl)
        # Load `terminate`
        self._termination = self._state.termination
        # Execute termination condition
        func(self)

    return wrapper


def make_pure_local_wrapper(func):
    @wraps(func)
    def wrapper(self):
        self.data = AlgorithmData(self._args)
        self.metadata = self.data.metadata
        del self.data.metadata
        func(self)
        self.set_output()

    return wrapper
