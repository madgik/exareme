import os
import logging

from .constants import LOGGING_LEVEL_ALG, LOGGING_LEVEL_SQL
from .algorithm import Algorithm
from .result import AlgorithmResult, TabularDataResource
from .exceptions import AlgorithmError, UserError
from .runner.runner import create_runner

__all__ = [
    "Algorithm",
    "AlgorithmResult",
    "TabularDataResource",
    "AlgorithmError",
    "UserError",
    "LOGGING_LEVEL_ALG",
    "create_runner",
]

logging.basicConfig(
    format="%(asctime)s - %(levelname)s: %(message)s",
    filename=os.path.join(os.path.dirname(__file__), "logs/mip.log"),
    level=LOGGING_LEVEL_ALG,
)
logging.getLogger("sqlalchemy.engine").setLevel(LOGGING_LEVEL_SQL)
