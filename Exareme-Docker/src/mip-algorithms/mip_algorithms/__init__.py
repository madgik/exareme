import logging

from mip_algorithms.constants import LOGGING_LEVEL_ALG, LOGGING_LEVEL_SQL

__all__ = ['Algorithm', 'AlgorithmResult', 'TabularDataResource', 'HighChart', 'AlgorithmError', 'logged',
           'LOGGING_LEVEL_ALG']

logging.basicConfig(
        format='%(asctime)s - %(levelname)s: %(message)s',
        filename='/root/mip.log',
        level=LOGGING_LEVEL_ALG
)
logging.getLogger('sqlalchemy.engine').setLevel(LOGGING_LEVEL_SQL)


def logged(func):
    def logging_wrapper(*args, **kwargs):
        try:
            if LOGGING_LEVEL_ALG == logging.INFO:
                logging.info("Starting: '{0}'".format(func.__name__))
            elif LOGGING_LEVEL_ALG == logging.DEBUG:
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
