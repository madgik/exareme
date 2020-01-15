from __future__ import division
from __future__ import print_function

import json
import sys
from os import path

import numpy as np
from lifelines import KaplanMeierFitter

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

from utils.algorithm_utils import make_json_raw, TransferAndAggregateData

PRIVACY_MAGIC_NUMBER = 15


def get_data(args):
    # # Parse args
    # arg_events = args.events
    #
    # dataset = args.dataset
    #
    # input_local_DB = args.input_local_DB
    # data_table = args.data_table
    # metadata_table = args.metadata_table
    # metadata_code_column = args.metadata_code_column
    # metadata_isCategorical_column = args.metadata_isCategorical_column

    total_duration = 30
    events = np.random.randint(0, 2, 2000)
    durations = np.random.randint(0, 20, 2000)
    # events = np.array([1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0])
    # durations = np.array([3, 6, 7, 4, 5, 1, 2, 2, 9, 5, 6, 9, 8, 2, 3, 20, 40, 30])

    return events, durations, total_duration


def local_1(local_in):
    # Unpack data
    events, durations, total_duration = local_in

    # Sort events by ascending duration
    idx = np.argsort(durations)
    events = events[idx]
    durations = durations[idx]

    # Split events into observed and non_observed groups
    durations_observed = np.array([d for d, e in zip(durations, events) if e == 1])
    durations_non_observed = np.array([total_duration + 1 for e in events if e == 0])

    # Remove some observations at random to allow grouping (see below)
    n_rem_o = len(durations_observed) % PRIVACY_MAGIC_NUMBER
    if n_rem_o:
        idx_rem = np.random.permutation(len(durations_observed))[:n_rem_o]
        durations_observed = np.delete(durations_observed, idx_rem)

    n_rem_n = len(durations_non_observed) % PRIVACY_MAGIC_NUMBER
    if n_rem_n:
        idx_rem = np.random.permutation(len(durations_non_observed))[:n_rem_n]
        durations_non_observed = np.delete(durations_non_observed, idx_rem)

    # Group observations by multiples of PRIVACY_MAGIC_NUMBER
    grouped_durations_observed = []
    for group in durations_observed.reshape(-1, PRIVACY_MAGIC_NUMBER):
        grouped_durations_observed += [group[-1]]
    grouped_durations_non_observed = []
    for group in durations_non_observed.reshape(-1, PRIVACY_MAGIC_NUMBER):
        grouped_durations_non_observed += [group[-1]]

    local_out = TransferAndAggregateData(grouped_durations_observed=(grouped_durations_observed, 'concat'),
                                         grouped_durations_non_observed=(grouped_durations_non_observed, 'concat'))

    return local_out


def global_1(global_in):
    data = global_in.get_data()
    grouped_durations_observed = data['grouped_durations_observed']
    grouped_durations_non_observed = data['grouped_durations_non_observed']

    # Expand observations groups
    durations = []
    events = []
    for d in grouped_durations_observed:
        durations += [d] * PRIVACY_MAGIC_NUMBER
        events += [1] * PRIVACY_MAGIC_NUMBER
    for d in grouped_durations_non_observed:
        durations += [d] * PRIVACY_MAGIC_NUMBER
        events += [0] * PRIVACY_MAGIC_NUMBER

    kmf = KaplanMeierFitter()
    kmf.fit(durations=durations, event_observed=events)

    kmf.plot()

    survival_function = kmf.survival_function_
    timeline = kmf.timeline

    # Pack results into corresponding object
    result = KaplanMeierResult(survival_function.values.flatten(), timeline)
    output = result.get_output()

    # Print output not allowing nans
    try:
        global_out = json.dumps(output, allow_nan=False, indent=4)
    except ValueError:
        raise ValueError('Result contains NaNs.')
    return global_out


class KaplanMeierResult(object):
    def __init__(self, survival_function, timeline):
        self.survival_function = survival_function
        self.timeline = timeline

    # This method returns a json object with all the algorithm results
    def get_json_raw(self):
        return make_json_raw(survival_function=list(zip(self.survival_function, self.timeline)))

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


def main():
    local_in = get_data(None)
    local_out = local_1(local_in=local_in)
    global_out = global_1(global_in=local_out)
    print(global_out)


if __name__ == '__main__':
    main()
