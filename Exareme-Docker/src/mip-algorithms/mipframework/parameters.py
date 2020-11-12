import json
import logging
import os
import re
from argparse import ArgumentParser

from . import LOGGING_LEVEL_ALG
from .loggingutils import logged, repr_with_logging

COMMON_ALGORITHM_ARGUMENTS = {
    "input_local_DB",
    "db_query",
    "cur_state_pkl",
    "prev_state_pkl",
    "local_step_dbs",
    "global_step_db",
    "data_table",
    "metadata_table",
    "metadata_code_column",
    "metadata_label_column",
    "metadata_isCategorical_column",
    "metadata_enumerations_column",
    "metadata_minValue_column",
    "metadata_maxValue_column",
    "metadata_sqlType_column",
}


class Parameters(object):
    def __init__(self, args):
        for name, val in vars(args).items():
            if name not in COMMON_ALGORITHM_ARGUMENTS:
                setattr(self, name, val)

    def __getitem__(self, name):
        return getattr(self, name)

    def __repr__(self):
        repr_with_logging(self, **self.__dict__)


@logged
def parse_exareme_args(fp, cli_args):
    parser = ArgumentParser()
    # Add common arguments
    for argname in COMMON_ALGORITHM_ARGUMENTS:
        parser.add_argument("-" + argname)
    # Add algorithm specific arguments
    prop_path = os.path.join(fp, "properties.json")
    with open(prop_path, "r") as prop:
        params = json.load(prop)["parameters"]
    algorithm_param_names = []
    for p in params:
        name = "-" + p["name"]
        algorithm_param_names.append(p["name"])
        required = p["valueNotBlank"]
        parser.add_argument(name, required=required)
    # Escape args starting with dash (e.g. see agegroup='-50y')
    all_args = set(COMMON_ALGORITHM_ARGUMENTS) | set(algorithm_param_names)
    escaped_args = []  # remember escaped args to undo later (see below)
    for i, argname in enumerate(cli_args):
        if argname.replace("-", "") in all_args:
            continue
        if argname.startswith("-"):
            cli_args[i] = "\\" + argname
            # arg name is one position begore its value
            escaped_args.append(cli_args[i - 1].replace("-", ""))
    # Parse and process
    args, _ = parser.parse_known_args(cli_args)
    args.y = re.split(r"\s*,\s*", args.y)
    args.var_names = list(args.y)
    args.formula_is_equation = False
    if hasattr(args, "x") and args.x:
        args.x = re.split(r"\s*,\s*", args.x)
        args.var_names += list(args.x)
        args.formula_is_equation = True
    args.dataset = re.split(r"\s*,\s*", args.dataset)
    args.filter = json.loads(args.filter) if args.filter else None
    if hasattr(args, "coding"):
        args.coding = None if args.coding == "null" else args.coding
    # Undo arg escaping (see above)
    if escaped_args:
        for argname in escaped_args:
            argval_without_escape = getattr(args, argname).replace("\\", "")
            setattr(args, argname, argval_without_escape)
    return args
