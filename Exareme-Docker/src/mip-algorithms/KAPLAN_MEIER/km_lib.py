from __future__ import division
from __future__ import print_function

import json
import math
import sys
from os import path

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
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


def get_data(timelines, event_val, max_duration):
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

    events = []
    durations = []
    for tl in timelines:
        if event_val in tl[1]:
            event = 1
            event_idx = tl[1].index(event_val)
            duration = tl[0][event_idx]
        else:
            event = 0
            duration = max_duration
        events.append(event)
        durations.append(duration)

    events = np.array(events)
    durations = np.array(durations)

    return events, durations, max_duration


def local_1(local_in):
    # Unpack data
    events, durations, max_duration = local_in

    # Sort events by ascending duration
    idx = np.argsort(durations)
    events = events[idx]
    durations = durations[idx]

    # Split events into observed and non_observed groups
    durations_observed = np.array([d for d, e in zip(durations, events) if e == 1])
    durations_non_observed = np.array([max_duration for e in events if e == 0])

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

    plt.xlim(40, 95)  # TODO Remove these lines
    plt.show()

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


def generate_fake_data(n_patients):
    from collections import OrderedDict
    from faker import Faker
    import uuid
    from datetime import timedelta, date

    faker = Faker()
    records = OrderedDict()
    records['subjectcode'] = []
    records['subjectvisitdate'] = []
    records['subjectage'] = []
    records['alzheimerbroadcategory'] = []
    for _ in range(n_patients):
        subjectcode = uuid.uuid4().hex
        birth_date = faker.date_between(start_date='-120y', end_date='-60y')
        lifetime = math.ceil(np.random.normal(365 * 77, 365 * 12))
        death_date = birth_date + timedelta(lifetime)

        while True:
            first_visit_delta = math.ceil(np.random.normal(365 * 67, 365 * 7))
            first_visit = birth_date + timedelta(first_visit_delta)
            if first_visit < death_date:
                break
        num_visits = np.random.randint(1, 10)
        visits = [first_visit]
        for i in range(num_visits):
            lam_visits = np.random.randint(30, 600)
            visit_delta = math.ceil(np.random.poisson(lam_visits))
            visits.append(visits[i - 1] + timedelta(visit_delta))
        visits = [v for v in visits if v < death_date]
        visits = [v for v in visits if v < date.today()]
        visits = sorted(visits)

        if visits != []:
            alz_coin = np.random.binomial(1, 0.6)
            alz_categories = ['CN'] * len(visits)
            if alz_coin:
                alz_diag_visit = np.random.randint(0, len(visits))
                alz_categories[alz_diag_visit:] = ['AD'] * (len(visits) - alz_diag_visit)

            for visit, alz_cat in zip(visits, alz_categories):
                records['subjectcode'].append(subjectcode)
                records['subjectvisitdate'].append(visit.strftime("%m-%d-%Y") + ' 0:00')
                age = visit - birth_date
                records['subjectage'].append(age.days / 365)
                records['alzheimerbroadcategory'].append(alz_cat)

    records = pd.DataFrame.from_dict(records)
    records.set_index('subjectcode', inplace=True)
    return records


def build_timelines(df, time_axis, var):
    timelines = []
    for g in df.groupby('subjectcode'):
        timeline = (g[1][time_axis].tolist(), g[1][var].tolist())
        timelines.append(timeline)
    return timelines


def main():
    pd.set_option('display.max_columns', 3)
    pd.set_option('display.width', 100)

    fake_data = generate_fake_data(1000)
    print(fake_data)

    timelines = build_timelines(fake_data, time_axis='subjectage', var='alzheimerbroadcategory')
    print(timelines)

    local_in = get_data(timelines, 'AD', 100)
    local_out = local_1(local_in=local_in)
    global_out = global_1(global_in=local_out)
    print(global_out)


if __name__ == '__main__':
    main()
