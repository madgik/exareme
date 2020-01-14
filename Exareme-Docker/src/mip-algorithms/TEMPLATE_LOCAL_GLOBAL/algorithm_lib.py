from __future__ import division
from __future__ import print_function

import json
import sys
from os import path

_new_path = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(_new_path)
while True:
    try:
        import utils.algorithm_utils
    except:
        sys.path.pop()
        _new_path = path.dirname(_new_path)
        sys.path.append(_new_path)
    else:
        break
del _new_path

from utils.algorithm_utils import query_from_formula, make_json_raw, TransferAndAggregateData


def get_data(args):
    args_x = list(
            args.x
                .replace(' ', '')
                .split(',')
    )
    variables = (args_x,)

    dataset = args.dataset
    formula = args.formula
    formula = formula.replace('_', '~')  # TODO Fix tilda problem and remove
    no_intercept = json.loads(args.no_intercept)
    input_local_DB = args.input_local_DB
    data_table = args.data_table
    metadata_table = args.metadata_table
    metadata_code_column = args.metadata_code_column
    metadata_isCategorical_column = args.metadata_isCategorical_column
    coding = None if args.coding == 'null' else args.coding
    _, df = query_from_formula(fname_db=input_local_DB, formula=formula, variables=variables,
                               dataset=dataset,
                               data_table=data_table, metadata_table=metadata_table,
                               metadata_code_column=metadata_code_column,
                               metadata_isCategorical_column=metadata_isCategorical_column,
                               no_intercept=no_intercept, coding=coding)

    return df


def local_1(local_in):
    # Unpack data
    df = local_in

    # TODO replace code below with algorithm code
    # -------------------------------------------
    n_obs, n_cols = len(df), len(df.columns)
    # -------------------------------------------

    local_out = TransferAndAggregateData(n_obs=(n_obs, 'do_nothing'), n_cols=(n_cols, 'do_nothing'))

    return local_out


def global_1(global_in):
    data = global_in.get_data()
    n_obs = data['n_obs']
    n_cols = data['n_cols']

    # TODO replace code below with algorithm code
    # -------------------------------------------
    pass
    # -------------------------------------------

    # Pack results into corresponding object
    result = AlgorithmResult(n_obs, n_cols)
    output = result.get_output()

    # Print output not allowing nans
    try:
        global_out = json.dumps(output, allow_nan=False)
    except ValueError:
        raise ValueError('Result contains NaNs.')
    return global_out


class AlgorithmResult(object):
    # Constructor TODO replace with your variables
    def __init__(self, n_obs, n_cols):
        self.n_obs = n_obs
        self.n_cols = n_cols

    # This method returns a json object with all the algorithm results
    def get_json_raw(self):
        return make_json_raw(n_obs=self.n_obs, n_cols=self.n_cols)

    # This method packs everything in one json object, to be output in the frontend.
    def get_output(self):
        result = {
            "result": [
                # Raw results
                {
                    "type": "application/json",
                    "data": self.get_json_raw()
                },
                # # Tabular data
                # {
                #     "type": "application/vnd.dataresource+json",
                #     "data": self.get_table()
                # }
            ]
        }
        return result
