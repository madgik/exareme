import logging

from .constants import LOGGING_LEVEL_ALG


def log_this(method, **kwargs):
    if LOGGING_LEVEL_ALG == logging.INFO:
        logging.info("Starting: {method}.".format(method=method))
    elif LOGGING_LEVEL_ALG == logging.DEBUG:
        arguments = ",".join(["\n{k}={v}".format(k=k, v=v) for k, v in kwargs.items()])
        logging.debug(
            "Starting: {method}, "
            "{arguments}".format(method=method, arguments=arguments)
        )


def repr_with_logging(self, **kwargs):
    cls = type(self).__name__
    if LOGGING_LEVEL_ALG == logging.INFO:
        return "{cls}()".format(cls=cls)
    elif LOGGING_LEVEL_ALG == logging.DEBUG:
        arguments = ",".join(["\n{k}={v}".format(k=k, v=v) for k, v in kwargs.items()])
        return "{cls}({arguments})".format(cls=cls, arguments=arguments)


def logged(func):
    def logging_wrapper(*args, **kwargs):
        if LOGGING_LEVEL_ALG == logging.INFO:
            logging.info(
                "Starting: {0}.{1}".format(type(args[0]).__name__, func.__name__)
            )
        elif LOGGING_LEVEL_ALG == logging.DEBUG:
            logging.debug(
                "Starting: {0}.{1},\nargs: \n{2},\nkwargs: \n{3}".format(
                    type(args[0]).__name__, func.__name__, args, kwargs
                )
            )
        return func(*args, **kwargs)

    logging_wrapper._original = func
    return logging_wrapper
