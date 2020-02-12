import logging
import os

_LOGGING_LEVEL = logging.DEBUG

logging.basicConfig(
        format='%(asctime)s - %(levelname)s: %(message)s',
        filename=os.path.splitext('/root/experimental.log')[0] + '.log',
        level=_LOGGING_LEVEL
)
logger = logging.getLogger('sqlalchemy.engine').setLevel(logging.DEBUG)  # todo see if we can use DEBUG here
# todo see if it makes sense to return a logger object above
