from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from typing import List

import numpy as np
from lifelines import KaplanMeierFitter

from mipframework import Algorithm, AlgorithmResult
from mipframework.exceptions import PrivacyError
from mipframework.constants import PRIVACY_MAGIC_NUMBER
from mipframework.highcharts import SurvivalCurves

from utils.algorithm_utils import make_json_raw, ExaremeError

TIMEPOINTS = [
    "BL",
    "FU1",
    "FU2",
]  # TODO replace with universal ordinal variable [0, 1, 2, ...]


class KaplanMeier(Algorithm):
    def __init__(self, cli_args):
        super(KaplanMeier, self).__init__(
            __file__, cli_args, intercept=False, privacy=False
        )

    def local_(self):
        bin_var = self.parameters.y[0]
        control_variable = self.parameters.x[0]
        outcome_pos = self.parameters.outcome_pos
        outcome_neg = self.parameters.outcome_neg
        total_duration = len(TIMEPOINTS) - 1

        data = self.data.db.read_data_from_db(
            self._args, "subjectcode", "participant_group", "timepoint",
        )
        # data = self.data.db.read_longitudinal_data_from_db(self._args)
        data.replace("", np.nan, inplace=True)
        data = data.dropna()
        data = data[(data[bin_var] == outcome_pos) | (data[bin_var] == outcome_neg)]
        data = data[data["participant_group"] == "compliant"]
        if len(data) < PRIVACY_MAGIC_NUMBER:
            raise PrivacyError("Query results in illegal number of datapoints.")
        levels = list(set(data[control_variable]))
        data_dict = {level: data[data[control_variable] == level] for level in levels}

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

        grouped_durations_observed_dict = {}
        grouped_durations_non_observed_dict = {}
        for key, events in events_dict.items():
            durations = durations_dict[key]
            # Sort events by ascending duration
            idx = np.argsort(durations)
            events = events[idx]
            durations = durations[idx]

            # Split events into observed and non_observed groups
            durations_observed = np.array(
                [d for d, e in zip(durations, events) if e == 1]
            )
            durations_non_observed = np.array(
                [total_duration for e in events if e == 0]
            )

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

        if all(not val for val in grouped_durations_observed_dict.values()):
            msg = (
                "There are not enough transitions from {neg} to {pos} in the data. "
                "Please try with different values".format(
                    pos=outcome_pos, neg=outcome_neg
                )
            )
            raise ExaremeError(msg)

        self.push_and_concat(
            grouped_durations_observed_dict=grouped_durations_observed_dict
        )
        self.push_and_concat(
            grouped_durations_non_observed_dict=grouped_durations_non_observed_dict
        )
        self.push_and_agree(control_variable=control_variable)

    def global_(self):
        grouped_durations_observed_dict = self.fetch("grouped_durations_observed_dict")
        grouped_durations_non_observed_dict = self.fetch(
            "grouped_durations_non_observed_dict"
        )
        control_variable = self.fetch("control_variable")

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

            survival_function_dict[key] = list(kmf.survival_function_.iloc[:, 0])
            confidence_interval_dict[key] = kmf.confidence_interval_.iloc[
                1:, :
            ].values.tolist()
            ci_tmp = [[1.0, 1.0]]
            ci_tmp.extend(confidence_interval_dict[key])
            confidence_interval_dict[key] = ci_tmp
            timeline_dict[key] = kmf.timeline.tolist()

            # Remove nans from result
            confidence_interval_dict[key] = replace_nans_with_nones(
                confidence_interval_dict[key]
            )
            timeline_dict[key] = replace_nans_with_nones(timeline_dict[key])

        # Yet another temp hack for mapping visit numbers to labels
        for key, tl in timeline_dict.items():
            timeline_dict[key] = [TIMEPOINTS[int(v)] for v in tl]

        self.result = AlgorithmResult(
            make_json_raw(
                survival_functions=survival_function_dict,
                confidence_intervals=confidence_interval_dict,
                timelines=timeline_dict,
            ),
            highcharts=[
                SurvivalCurves(
                    timeline_dict,
                    survival_function_dict,
                    confidence_interval_dict,
                    control_variable,
                    TIMEPOINTS,
                )
            ],
        )


def build_timelines(df, time_axis, var):
    timelines = []
    for g in df.groupby("subjectcode"):
        timeline = (
            list(range(len(TIMEPOINTS))),
            [g[1][g[1]["timepoint"] == tp][var].iloc[0] for tp in TIMEPOINTS],
        )
        timelines.append(timeline)

    return timelines


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


def replace_nans_with_nones(lst):
    # import pdb; pdb.set_trace()
    if isinstance(lst[0], List):
        return [replace_nans_with_nones(item) for item in lst]
    else:
        return [None if np.isnan(item) else item for item in lst]


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-x",
        "apoe4",
        "-y",
        "alzheimerbroadcategory",
        "-pathology",
        "dementia",
        "-dataset",
        # "alzheimer_fake_cohort",
        "aachen_longitudinal",
        "-filter",
        """
        {
            "condition":"OR",
            "rules":[
                {
                    "id":"alzheimerbroadcategory",
                    "field":"alzheimerbroadcategory",
                    "type":"string",
                    "input":"select",
                    "operator":"equal",
                    "value":"AD"
                },
                {
                    "id":"alzheimerbroadcategory",
                    "field":"alzheimerbroadcategory",
                    "type":"string",
                    "input":"select",
                    "operator":"equal",
                    "value":"MCI"
                }
            ],
            "valid":true
        }
        """,
        "-outcome_pos",
        "AD",
        "-outcome_neg",
        "MCI",
        "-total_duration",
        "1100",
    ]
    runner = create_runner(KaplanMeier, algorithm_args=algorithm_args, num_workers=1,)
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
