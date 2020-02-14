import errno
import os
from functools import wraps

from mip_algorithms import logged


@logged
def make_dirs(fname):
    if not os.path.exists(os.path.dirname(fname)):
        try:
            os.makedirs(os.path.dirname(fname))
        except OSError as exc:  # Guard against race condition
            if exc.errno != errno.EEXIST:
                raise


def one_kwarg(func):
    @wraps(func)
    def onekwarg_wrapper(self, **kwarg):
        if len(kwarg) != 1:
            raise ValueError('Please push one variable at the time.')
        func(self, **kwarg)

    return onekwarg_wrapper