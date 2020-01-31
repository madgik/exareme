from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import json

from utils.algorithm_utils import TransferAndAggregateData, StateData, ExaremeError, query_from_formula, \
    set_algorithms_output_data


# ============================= INIT ============================= #
def get_data(args):
    pass


def template_init_local(args, local_in):
    pass


def template_init_global(args, global_in):
    pass


# =========================== ITERATION ========================== #
def template_step_local(args, local_state, local_in):
    pass


def template_step_global(args, global_state, global_in):
    pass


def termination_condition(args, global_state):
    pass


# ============================= FINAL ============================ #
def template_finalize_local(args, local_state, local_in):
    pass


def template_finalize_global(args, global_state, global_in):
    pass


# ============================ RESULTS =========================== #
class TemplateResult:
    def __init__(self):
        pass

    def get_json_raw(self):
        pass

    def get_tabular(self):
        pass

    def get_highchart(self):
        pass

    def get_output(self):
        return {
            "result": [
                # Raw results
                {
                    "type": "application/json",
                    "data": self.get_json_raw()
                },
                # Tabular
                {
                    "type": "application/vnd.dataresource+json",
                    "data": self.get_tabular()
                },
                # Highcharts
                {
                    "type": "application/vnd.highcharts+json",
                    "data": self.get_highchart()
                }
            ]
        }
