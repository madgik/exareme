import logging
import os

_LOGGING_LEVEL_ALG = logging.DEBUG
_LOGGING_LEVEL_SQL = logging.INFO

logging.basicConfig(
        format='%(asctime)s - %(levelname)s: %(message)s',
        filename=os.path.splitext('/root/experimental.log')[0] + '.log',
        level=_LOGGING_LEVEL_ALG
)
logger = logging.getLogger('sqlalchemy.engine').setLevel(_LOGGING_LEVEL_SQL)
# todo see if it makes sense to return a logger object above


def logged(func):
    # @wraps  todo why this fails??
    def logging_wrapper(*args, **kwargs):
        try:
            if _LOGGING_LEVEL_ALG == logging.INFO:
                logging.info("Starting: '{0}'".format(func.__name__))
            elif _LOGGING_LEVEL_ALG == logging.DEBUG:
                logging.debug("Starting: '{0}',\nargs: \n{1},\nkwargs: \n{2}"
                              .format(func.__name__, args, kwargs))
            return func(*args, **kwargs)
        except Exception as e:
            logging.exception(e)
            raise e

    return logging_wrapper


from algorithm import Algorithm
from result import AlgorithmResult, TabularDataResource, HighChart
from exceptions import AlgorithmError

__all__ = ['Algorithm', 'AlgorithmResult', 'TabularDataResource', 'HighChart', 'AlgorithmError']