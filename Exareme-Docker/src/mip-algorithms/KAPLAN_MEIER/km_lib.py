from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import json
import re
import sqlite3
from datetime import datetime

# import matplotlib.pyplot as plt  XXX for local runs

import numpy as np
import pandas as pd
from lifelines import KaplanMeierFitter
from colour import Color

from utils.algorithm_utils import (
    make_json_raw,
    TransferAndAggregateData,
    PRIVACY_MAGIC_NUMBER,
    PrivacyError,
)


def query_longitudinal(
    query, input_local_DB, bin_var, outcome_pos, outcome_neg, control_variable
):
    # fixme The following is a hack for querying longitudinal data,
    #  should be done by Exareme
    _, sel_head, sel_tail = re.split(r"(select )", query)
    query = (
        sel_head + "subjectcode, subjectvisitdate, subjectage, "
        "subjectvisitid, dataset, " + sel_tail
    )
    con = sqlite3.connect(input_local_DB)
    data = pd.read_sql(query, con=con)
    data.replace("", np.nan, inplace=True)  # fixme remove when no empty str in dbs
    data = data.dropna()
    data = data[(data[bin_var] == outcome_pos) | (data[bin_var] == outcome_neg)]
    if len(data) < PRIVACY_MAGIC_NUMBER:
        raise PrivacyError("Query results in illegal number of datapoints.")
    levels = list(set(data[control_variable]))
    data_dict = {level: data[data[control_variable] == level] for level in levels}
    return data_dict


def get_data(args):
    input_local_DB = args.input_local_DB
    bin_var = args.y.strip()
    outcome_pos = args.outcome_pos
    outcome_neg = args.outcome_neg
    total_duration = args.total_duration
    db_query = args.db_query
    control_variable = args.x

    # Get data and build timelines
    data_dict = query_longitudinal(
        db_query, input_local_DB, bin_var, outcome_pos, outcome_neg, control_variable
    )
    # data_dict = read_csv(bin_var, outcome_pos, outcome_neg)  # XXX for local runs
    timelines_dict = {
        k: build_timelines(d, time_axis="subjectvisitdate", var=bin_var)
        for k, d in data_dict.items()
    }

    # Remove patients who tested positive on first visit
    for key, timelines in timelines_dict.items():
        timelines = [tl for tl in timelines if tl[1][0] != outcome_pos]
        timelines_dict[key] = timelines

    durations_dict = {}
    events_dict = {}
    for k, tl in timelines_dict.items():
        durations_dict[k], events_dict[k] = convert_timelines_to_events(
            total_duration, outcome_pos, tl
        )

    return events_dict, durations_dict, total_duration, control_variable


def convert_timelines_to_events(total_duration, outcome_pos, timelines):
    events = []
    durations = []
    for tl in timelines:
        if outcome_pos in tl[1]:
            event = 1
            event_idx = tl[1].index(outcome_pos)
            duration = tl[0][event_idx]
        else:
            event = 0
            duration = total_duration
        events.append(event)
        durations.append(duration)
    events = np.array(events)
    durations = np.array(durations)
    return durations, events


def local_1(local_in):
    # Unpack data
    events_dict, durations_dict, max_duration, control_variable = local_in

    grouped_durations_observed_dict = {}
    grouped_durations_non_observed_dict = {}
    for key, events in events_dict.items():
        durations = durations_dict[key]
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
        grouped_durations_observed_dict[key] = []
        for group in durations_observed.reshape(-1, PRIVACY_MAGIC_NUMBER):
            grouped_durations_observed_dict[key] += [group[-1]]
        grouped_durations_non_observed_dict[key] = []
        for group in durations_non_observed.reshape(-1, PRIVACY_MAGIC_NUMBER):
            grouped_durations_non_observed_dict[key] += [group[-1]]

    local_out = TransferAndAggregateData(
        grouped_durations_observed=(grouped_durations_observed_dict, "concat"),
        grouped_durations_non_observed=(grouped_durations_non_observed_dict, "concat"),
        control_variable=(control_variable, "do_nothing"),
    )

    return local_out


def global_1(global_in):
    data = global_in.get_data()
    grouped_durations_observed_dict = data["grouped_durations_observed"]
    grouped_durations_non_observed_dict = data["grouped_durations_non_observed"]
    control_variable = data["control_variable"]

    survival_function_dict = {}
    confidence_interval_dict = {}
    timeline_dict = {}
    for key in grouped_durations_observed_dict.keys():
        grouped_durations_observed = grouped_durations_observed_dict[key]
        grouped_durations_non_observed = grouped_durations_non_observed_dict[key]
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

        # *****************************************
        # kmf.plot()
        # plt.xlim(0, 3*365)  # XXX for local runs
        # plt.show()
        # *****************************************

        survival_function_dict[key] = list(kmf.survival_function_.iloc[:, 0])
        confidence_interval_dict[key] = kmf.confidence_interval_.iloc[
            1:, :
        ].values.tolist()
        ci_tmp = [[1.0, 1.0]]
        ci_tmp.extend(confidence_interval_dict[key])
        confidence_interval_dict[key] = ci_tmp
        timeline_dict[key] = kmf.timeline.tolist()

    # Pack results into corresponding object
    result = KaplanMeierResult(
        survival_function_dict,
        confidence_interval_dict,
        timeline_dict,
        control_variable,
    )
    output = result.get_output()

    # Print output not allowing nans
    try:
        global_out = json.dumps(output, allow_nan=False, indent=4)
    except ValueError:
        raise ValueError("Result contains NaNs.")
    return global_out


class KaplanMeierResult(object):
    def __init__(
        self,
        survival_function_dict,
        confidence_interval_dict,
        timeline_dict,
        control_variable,
    ):
        self.survival_function_dict = survival_function_dict
        self.timeline_dict = timeline_dict
        self.confidence_interval_dict = {}
        for key, ci in confidence_interval_dict.items():
            self.confidence_interval_dict[key] = [
                [x, y[0], y[1]] for x, y in zip(self.timeline_dict[key], ci)
            ]
        self.light_colors = {}
        for i, key in enumerate(self.timeline_dict.keys()):
            self.light_colors[key] = colors_light[i]
        self.dark_colors = {}
        for i, key in enumerate(self.timeline_dict.keys()):
            self.dark_colors[key] = colors_dark[i]
        self.control_variable = control_variable

    # This method returns a json object with all the algorithm results
    def get_json_raw(self):
        return make_json_raw(
            survival_functions=self.survival_function_dict,
            confidence_intervals=self.confidence_interval_dict,
            timelines=self.timeline_dict,
        )

    def get_highchart(self):
        return {
            "chart": {
                "type": "arearange",
                "zoomType": "x",
                "scrollablePlotArea": {"minWidth": 600, "scrollPositionX": 1},
            },
            "title": {"text": "Survival curve"},
            "xAxis": {"title": {"text": "Days since first visit"}},
            "plotOptions": {"arearange": {"step": "left"}},
            "yAxis": {"title": {"text": "Survival Probability"}},
            "tooltip": {"crosshairs": True, "shared": True,},
            "legend": {
                "align": "left",
                "verticalAlign": "middle",
                "layout": "vertical",
            },
            "series": [
                {
                    "data": list(self.confidence_interval_dict[key]),
                    "color": self.light_colors[key],
                    "marker": {"enabled": False},
                    "showInLegend": False,
                }
                for key in self.timeline_dict.keys()
            ]
            + [
                {
                    "type": "line",
                    "name": self.control_variable + ": " + str(key),
                    "step": True,
                    "data": zip(
                        self.timeline_dict[key], self.survival_function_dict[key]
                    ),
                    "color": self.dark_colors[key],
                    "marker": {"enabled": False},
                }
                for key in self.timeline_dict.keys()
            ],
        }

    # This method packs everything in one json object, to be output in the frontend.
    def get_output(self):
        result = {
            "result": [
                # Raw results
                {"type": "application/json", "data": self.get_json_raw()},
                # Highchart Survival curve
                {
                    "type": "application/vnd.highcharts+json",
                    "data": self.get_highchart(),
                },
            ]
        }
        return result


def build_timelines(df, time_axis, var):
    timelines = []
    for g in df.groupby("subjectcode"):
        dates = [
            datetime.strptime(s.replace(" 0:00", ""), "%Y-%m-%d")
            for s in g[1][time_axis].tolist()
        ]
        deltas_in_days = [(d - min(dates)).days for d in dates]
        timeline = (deltas_in_days, g[1][var].tolist())
        timelines.append(timeline)

    return timelines


colors_dark = [
    "#7cb5ec",
    "#434348",
    "#90ed7d",
    "#f7a35c",
    "#8085e9",
    "#f15c80",
    "#e4d354",
    "#2b908f",
    "#f45b5b",
    "#91e8e1",
]
colors_light = [Color(c) for c in colors_dark]
for c in colors_light:
    c.luminance = (1 + c.luminance) / 2
colors_light = [c.get_hex() for c in colors_light]


# XXX Below this point: not needed for exareme, it's for running locally only
def main():
    class Args(object):
        def __init__(self):
            self.input_local_DB = ""
            self.y = "alzheimerbroadcategory"
            self.outcome_pos = "AD"
            self.outcome_neg = "MCI"
            self.total_duration = 1100
            self.db_query = "select alzheimerbroadcategory from data;"
            self.x = "apoe4"

    args = Args()
    local_in = get_data(args)
    local_out = local_1(local_in=local_in)
    global_out = global_1(global_in=local_out)
    print(global_out)


def read_csv(bin_var, outcome_pos, outcome_neg):
    data = pd.read_csv("alzheimer_fake_cohort.csv")
    data.replace("", np.nan, inplace=True)
    data = data.dropna()
    data = data[(data[bin_var] == outcome_pos) | (data[bin_var] == outcome_neg)]
    levels = list(set(data["apoe4"]))
    data_dict = {level: data[data["apoe4"] == level] for level in levels}
    return data_dict


if __name__ == "__main__":
    main()
