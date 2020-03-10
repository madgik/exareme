from __future__ import division
from __future__ import print_function

import json
import re
import sqlite3

import numpy as np
import pandas as pd
from lifelines import KaplanMeierFitter

from utils.algorithm_utils import make_json_raw, TransferAndAggregateData, PRIVACY_MAGIC_NUMBER, PrivacyError


def query_longitudinal(query, input_local_DB):
    # fixme The following is a hack for querying longitudinal data, should be done by Exareme
    _, sel_head, sel_tail = re.split(r'(select )', query)
    query = sel_head + 'subjectcode, subjectvisitdate, subjectage, ' + sel_tail
    con = sqlite3.connect(input_local_DB)
    data = pd.read_sql(query, con=con)
    data.replace('', np.nan, inplace=True)  # fixme remove when no empty str in dbs
    data = data.dropna()
    # Privacy check
    if len(data) < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError('Query results in illegal number of datapoints.')
    return data


def get_data(args):
    input_local_DB = args.input_local_DB
    bin_var = args.y.strip()
    outcome_pos = args.outcome_pos
    outcome_neg = args.outcome_neg
    max_age = args.max_age
    db_query = args.db_query

    # Get data and build timelines
    data = query_longitudinal(db_query, input_local_DB)
    data = data[(data[bin_var] == outcome_pos) | (data[bin_var] == outcome_neg)]
    timelines = build_timelines(data, time_axis='subjectage', var=bin_var)

    # Convert timelines to (events, durations)
    events = []
    durations = []
    for tl in timelines:
        if outcome_pos in tl[1]:
            event = 1
            event_idx = tl[1].index(outcome_pos)
            duration = tl[0][event_idx]
        else:
            event = 0
            duration = max_age
        events.append(event)
        durations.append(duration)

    events = np.array(events)
    durations = np.array(durations)

    return events, durations, max_age


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

    durations = np.array(durations, dtype=np.float)
    events = np.array(events, dtype=np.int)

    kmf = KaplanMeierFitter()
    kmf.fit(durations=durations, event_observed=events)

    survival_function = kmf.survival_function_
    confidence_interval = kmf.confidence_interval_
    confidence_interval = confidence_interval.iloc[1:, :].values.tolist()
    ci_tmp = [[1.0, 1.0]]
    ci_tmp.extend(confidence_interval)
    confidence_interval = ci_tmp
    timeline = kmf.timeline

    # Pack results into corresponding object
    result = KaplanMeierResult(survival_function.values.flatten(), confidence_interval, timeline)
    output = result.get_output()

    # Print output not allowing nans
    try:
        global_out = json.dumps(output, allow_nan=False, indent=4)
    except ValueError:
        raise ValueError('Result contains NaNs.')
    return global_out


class KaplanMeierResult(object):
    def __init__(self, survival_function, confidence_interval, timeline):
        self.survival_function = survival_function
        self.confidence_interval = confidence_interval
        self.timeline = timeline
        self.confidence_interval = [[x, y[0], y[1]] for x, y in zip(self.timeline, self.confidence_interval)]

    # This method returns a json object with all the algorithm results
    def get_json_raw(self):
        return make_json_raw(survival_function=list(zip(self.timeline, self.survival_function)),
                             confidence_interval=list(self.confidence_interval))

    def get_highchart(self):
        return {

            'chart'      : {
                'type'              : 'arearange',
                'zoomType'          : 'x',
                'scrollablePlotArea': {
                    'minWidth'       : 600,
                    'scrollPositionX': 1
                }
            },

            'title'      : {
                'text': 'Survival curve'
            },

            'xAxis'      : {
                'title': {
                    'text': 'Age'
                }
            },

            'plotOptions': {
                'arearange': {
                    'step': 'left'
                }
            },

            'yAxis'      : {
                'title': {
                    'text': 'Survival Probability'
                }
            },

            'tooltip'    : {
                'crosshairs': True,
                'shared'    : True,
            },

            'legend'     : {
                'enabled': False
            },

            'series'     : [{
                'data': list(self.confidence_interval)

            },
                {
                    'type': "line",
                    'step': True,
                    'data': zip(self.timeline, self.survival_function.tolist()),
                }]

        }

    # This method packs everything in one json object, to be output in the frontend.
    def get_output(self):
        result = {
            "result": [
                # Raw results
                {
                    "type": "application/json",
                    "data": self.get_json_raw()
                },
                # Highchart Survival curve
                {
                    "type": "application/vnd.highcharts+json",
                    "data": self.get_highchart()
                },
            ]
        }
        return result


def build_timelines(df, time_axis, var):
    timelines = []
    for g in df.groupby('subjectcode'):
        timeline = (g[1][time_axis].tolist(), g[1][var].tolist())
        timelines.append(timeline)
    return timelines


def main():
    class Args(object):
        def __init__(self):
            self.input_local_DB = '/Users/zazon/madgik/exareme/Exareme-Docker/src/mip-algorithms/KAPLAN_MEIER/km_fake.sqlite'
            self.y = 'alzheimerbroadcategory'
            self.outcome_pos = 'AD'
            self.outcome_neg = 'CN'
            self.max_age = 100
            self.db_query = 'select alzheimerbroadcategory from data;'

    args = Args()
    local_in = get_data(args)
    local_out = local_1(local_in=local_in)
    global_out = global_1(global_in=local_out)
    print(global_out)


if __name__ == '__main__':
    main()
