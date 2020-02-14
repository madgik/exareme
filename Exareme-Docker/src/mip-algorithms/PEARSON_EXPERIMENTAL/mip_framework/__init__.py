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
