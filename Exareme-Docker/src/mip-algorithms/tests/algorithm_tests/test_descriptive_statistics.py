from numbers import Number
from collections import Counter

import DESCRIPTIVE_STATS
import numpy as np
import pytest
import json
from pathlib import Path

from mipframework.testutils import get_test_params, get_algorithm_result
from mipframework import create_runner
from DESCRIPTIVE_STATS import DescriptiveStats
from DESCRIPTIVE_STATS.descriptive_stats import (
    MonoidMapping,
    get_counts_and_percentages,
)


expected_file = Path(__file__).parent / "expected" / "descriptive_stats_expected.json"


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80))
)
def test_descriptive_stats_algorithm_local(test_input, expected):
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)

    recursive_isclose(result, expected)


@pytest.mark.parametrize(
    "test_input, expected", get_test_params(expected_file, slice(80, 95))
)
def test_descriptive_stats_algorithm_federated(test_input, expected):
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=10)

    recursive_isclose(result, expected)


def recursive_isclose(first, second):
    assert_same_type(first, second)
    if isinstance(first, dict):
        for key in second.keys():
            recursive_isclose(first[key], second[key])
    elif isinstance(first, list):
        assert np.isclose(first, second, rtol=1e-6).all()
    elif isinstance(first, Number):
        assert np.isclose(first, round(second, 2), rtol=1e-6)
    elif isinstance(first, basestring):
        assert first == second


def assert_same_type(first, second):
    assert (
        (isinstance(first, dict) and isinstance(second, dict))
        or (isinstance(first, list) and isinstance(second, list))
        or (isinstance(first, Number) and isinstance(second, Number))
    )


def test_monoid_mapping():
    m1 = MonoidMapping(a=1, b=2)
    m2 = MonoidMapping(a=10, b=20)
    result = m1 + m2
    assert result == MonoidMapping({"a": 11, "b": 22})
    assert isinstance(result, MonoidMapping)


def test_monoid_mapping_missing_values():
    m1 = MonoidMapping(a=1, b=2)
    m2 = MonoidMapping(b=20, c=30)
    result = m1 + m2
    assert result == MonoidMapping({"a": 1, "b": 22, "c": 30})
    assert isinstance(result, MonoidMapping)


def test_get_counts_and_percentages():
    lst = ["a", "a", "b", "c", "c", "c"]
    counter = Counter(lst)
    result = get_counts_and_percentages(counter)
    expected = {
        "a": {"count": 2, "percentage": 33.33},
        "b": {"count": 1, "percentage": 16.67},
        "c": {"count": 3, "percentage": 50.00},
    }
    assert result == expected


def test_result_format(capsys):
    args = [
        "-y",
        "leftaccumbensarea, apoe4",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        "",
    ]
    runner = create_runner(
        DescriptiveStats,
        algorithm_args=args,
        num_workers=1,
    )
    runner.run()
    captured = capsys.readouterr()
    result = json.loads(captured.out)
    result_single = result["result"][0]["data"]["single"]
    result_model = result["result"][0]["data"]["model"]
    expected_single = {
        "ApoE4": {
            "adni": {
                "num_total": 1066,
                "num_datapoints": 1061,
                "num_nulls": 5,
                "data": {
                    "0": {"count": 542, "percentage": 51.08},
                    "1": {"count": 395, "percentage": 37.23},
                    "2": {"count": 124, "percentage": 11.69},
                },
            }
        },
        "Left Accumbens Area": {
            "adni": {
                "num_total": 1066,
                "num_datapoints": 1066,
                "num_nulls": 0,
                "data": {
                    "max": 0.60,
                    "mean": 0.41,
                    "min": 0.20,
                    "std": 0.05,
                },
            }
        },
    }
    assert result_single == expected_single

    expected_model = {
        "adni": {
            "num_total": 1066,
            "num_datapoints": 1061,
            "num_nulls": 5,
            "data": {
                "ApoE4": {
                    "0": {"count": 542, "percentage": 51.08},
                    "1": {"count": 395, "percentage": 37.23},
                    "2": {"count": 124, "percentage": 11.69},
                },
                "Left Accumbens Area": {
                    "max": 0.60,
                    "mean": 0.41,
                    "min": 0.20,
                    "std": 0.05,
                },
            },
        }
    }
    assert result_model == expected_model


ntot_consistency_test_data = [
    # Categorical vars, no overlap
    [
        "-y",
        "gender, car_doors",
        "-pathology",
        "dementia",
        "-dataset",
        "car, adni",
        "-filter",
        "",
    ],
    # Mixed vars, no overlap
    [
        "-y",
        "gender, lefthippocampus",
        "-pathology",
        "dementia",
        "-dataset",
        "adni, car",
        "-filter",
        "",
    ],
    # Mixed vars, one dataset
    [
        "-y",
        "gender, lefthippocampus",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        "",
    ],
    # Categorical vars, one datasets, one var has missing values
    [
        "-y",
        "gender, apoe4",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        "",
    ],
    # Dataset in variables
    [
        "-y",
        "dataset, apoe4",
        "-pathology",
        "dementia",
        "-dataset",
        "adni, ppmi",
        "-filter",
        "",
    ],
]

DATASET2NROWS = {
    "ANOVA_Balanced_with_inter_V1V2": 180,
    "ANOVA_UnBalanced_with_inter_V1V2": 200,
    "ANOVA_dataset1": 225,
    "ANOVA_dataset2": 201,
    "ANOVA_dataset3": 292,
    "Iris": 150,
    "adni": 1066,
    "adni_9rows": 9,
    "car": 1728,
    "cb_data": 1000,
    "cb_test_data": 1851,
    "concrete_data": 1030,
    "concrete_data_testing": 309,
    "concrete_data_training": 721,
    "contact-lenses": 24,
    "data_logisticRegression": 490,
    "data_pr1": 718,
    "desd-synthdata": 1000,
    "diabetes": 768,
    "diabetes_testing": 231,
    "diabetes_training": 537,
    "edsd": 474,
    "ppmi": 714,
}


@pytest.mark.parametrize("num_workers", [1, 10])
@pytest.mark.parametrize("algorithm_args", ntot_consistency_test_data)
def test_descriptive_stats_consistent_counts_single(
    algorithm_args,
    num_workers,
    capsys,
    monkeypatch,
):
    monkeypatch.setattr(DESCRIPTIVE_STATS.descriptive_stats, "PRIVACY_THRESHOLD", 0)
    runner = create_runner(
        DescriptiveStats,
        algorithm_args=algorithm_args,
        num_workers=num_workers,
    )
    runner.run()
    captured = capsys.readouterr()
    result = json.loads(captured.out)
    result_partial = result["result"][0]["data"]["single"]

    for var in result_partial.keys():
        for dataset, data in result_partial[var].items():
            n_tot = data["num_datapoints"] + data["num_nulls"]
            expected_ntot = DATASET2NROWS[dataset]
            assert n_tot == expected_ntot


@pytest.mark.parametrize("num_workers", [1, 10])
@pytest.mark.parametrize("algorithm_args", ntot_consistency_test_data)
def test_descriptive_stats_consistent_counts_model(
    algorithm_args,
    num_workers,
    capsys,
    monkeypatch,
):
    monkeypatch.setattr(DESCRIPTIVE_STATS.descriptive_stats, "PRIVACY_THRESHOLD", 0)
    runner = create_runner(
        DescriptiveStats,
        algorithm_args=algorithm_args,
        num_workers=num_workers,
    )
    runner.run()
    captured = capsys.readouterr()
    result = json.loads(captured.out)
    result_partial = result["result"][0]["data"]["model"]
    for dataset, data in result_partial.items():
        n_tot = data["num_datapoints"] + data["num_nulls"]
        expected_ntot = DATASET2NROWS[dataset]
        assert n_tot == expected_ntot
