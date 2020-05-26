import os
import logging
import logging.handlers
from pathlib import Path

from .constants import LOGGING_LEVEL_ALG


LOG_FILENAME = "/var/log/mipframework.log"
try:
    open(LOG_FILENAME, "w")
except IOError:
    LOG_FILENAME = os.path.expanduser("~/mipframework.log")

miplogger = logging.getLogger("miplogger")
handler = logging.handlers.RotatingFileHandler(
    str(LOG_FILENAME), maxBytes=int(1e7), backupCount=10
)
formatter = logging.Formatter("%(asctime)s - %(levelname)s: %(message)s")
handler.setFormatter(formatter)
miplogger.setLevel(LOGGING_LEVEL_ALG)
miplogger.addHandler(handler)
miplogger.propagate = False


def log_this(method, **kwargs):
    if LOGGING_LEVEL_ALG == logging.INFO:
        miplogger.info("Starting: {method}".format(method=method))
    elif LOGGING_LEVEL_ALG == logging.DEBUG:
        arguments = ",".join(["{k}={v}".format(k=k, v=v) for k, v in kwargs.items()])
        miplogger.debug(
            "Starting: {method}, "
            "{arguments}".format(method=method, arguments=arguments)
        )


def repr_with_logging(self, **kwargs):
    cls = type(self).__name__
    if LOGGING_LEVEL_ALG == logging.INFO:
        return "{cls}()".format(cls=cls)
    elif LOGGING_LEVEL_ALG == logging.DEBUG:
        arguments = ",".join(["{k}={v}".format(k=k, v=v) for k, v in kwargs.items()])
        return "{cls}({arguments})".format(cls=cls, arguments=arguments)


def logged(func):
    def logging_wrapper(*args, **kwargs):
        cls = get_class_name(args)
        if LOGGING_LEVEL_ALG == logging.INFO:
            miplogger.info("Starting: {0}{1}".format(cls, func.__name__))
        elif LOGGING_LEVEL_ALG == logging.DEBUG:
            miplogger.debug(
                "Starting: {0}{1},args: {2},kwargs: {3}".format(
                    cls, func.__name__, args, kwargs
                )
            )
        try:
            res = func(*args, **kwargs)
        except Exception as e:
            miplogger.error(e)
            raise
        return res

    def get_class_name(args):
        if is_classmethod(args[0]):
            cls = args[0].__name__ + "."
        elif is_mipframework_method(args[0]):
            cls = type(args[0]).__name__ + "."
        else:
            cls = ""
        return cls

    logging_wrapper._original = func
    return logging_wrapper


def is_classmethod(arg):
    return type(arg) == type


def is_mipframework_method(arg):
    return (hasattr(arg, "__module__") and "mipframework" in arg.__module__) or (
        hasattr(type(arg), "__base__")
        and "mipframework" in type(arg).__base__.__module__
    )
