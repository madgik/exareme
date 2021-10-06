import json
import logging
import os
import re
from argparse import ArgumentParser

from mipframework import LOGGING_LEVEL_ALG
from mipframework.loggingutils import logged, repr_with_logging

SHARED_ALGORITHM_ARGS = {
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
            if name not in SHARED_ALGORITHM_ARGS:
                setattr(self, name, val)

    def __getitem__(self, name):
        return getattr(self, name)

    def __repr__(self):
        repr_with_logging(self, **self.__dict__)


@logged
def parse_cli_args(algorithm_folder_path, cli_args):
    algorithm_params = get_algorithm_params(algorithm_folder_path)
    parser = get_parser(algorithm_params)
    args = get_args(parser, algorithm_params, cli_args)
    return args


def get_algorithm_params(algorithm_folder_path):
    prop_path = os.path.join(algorithm_folder_path, "properties.json")
    with open(prop_path, "r") as prop:
        params = json.load(prop)["parameters"]
    return [{"name": p["name"], "required": p["valueNotBlank"]} for p in params]


def get_parser(algorithm_params):
    parser = ArgumentParser()
    for argname in SHARED_ALGORITHM_ARGS:
        parser.add_argument("-" + argname)
    for p in algorithm_params:
        name = "-" + p["name"]
        required = p["required"]
        parser.add_argument(name, required=required)
    return parser


def get_args(parser, algorithm_params, cli_args):
    escaped_cli_args, escaped_argnames = escape_cli_args(cli_args, algorithm_params)
    args, _ = parser.parse_known_args(escaped_cli_args)
    if escaped_argnames:
        remove_escape_chars_from_args(args, escaped_argnames)
    process_args(args)
    return args


def escape_cli_args(cli_args, algorithm_params):
    escaped_cli_args = list(cli_args)
    algorithm_param_names = [param["name"] for param in algorithm_params]
    all_args = set(SHARED_ALGORITHM_ARGS) | set(algorithm_param_names)
    escaped_argnames = []  # remember escaped args to undo later (see below)
    for i, argname in enumerate(escaped_cli_args):
        if argname.replace("-", "") in all_args:
            continue
        if argname.startswith("-"):
            escaped_cli_args[i] = "\\" + argname
            # arg name is one position begore its value
            escaped_argnames.append(escaped_cli_args[i - 1].replace("-", ""))
    return escaped_cli_args, escaped_argnames


def remove_escape_chars_from_args(args, escaped_argnames):
    for argname in escaped_argnames:
        argval_without_escape = getattr(args, argname).replace("\\", "")
        setattr(args, argname, argval_without_escape)


def process_args(args):
    args.y = re.split(r"\s*,\s*", args.y)
    args.var_names = list(args.y)
    if hasattr(args, "x") and args.x:
        args.x = re.split(r"\s*,\s*", args.x)
        args.var_names += list(args.x)
    args.dataset = re.split(r"\s*,\s*", args.dataset)
    args.filter = json.loads(args.filter) if args.filter else None
    if hasattr(args, "coding"):
        args.coding = None if args.coding == "null" else args.coding
    if hasattr(args, "formula") and args.formula:
        args.formula = json.loads(args.formula)
    if not hasattr(args, "coding") or not args.coding:
        args.coding = "Treatment"
