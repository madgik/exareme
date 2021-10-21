import json

import pytest

from mipframework.testutils import get_algorithm_result
from DESCRIPTIVE_STATS.descriptive_stats import DescriptiveStats


def test_descriptive_stats_formula_no_interaction():
    test_input = [
        {"name": "y", "value": "lefthippocampus,righthippocampus"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
    ]
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)

    assert "Left Hippocampus" in result["single"].keys()
    assert "Right Hippocampus" in result["single"].keys()

    assert "Left Hippocampus" in result["model"]["ppmi"]["data"].keys()
    assert "Right Hippocampus" in result["model"]["ppmi"]["data"].keys()


@pytest.mark.xfail(reason="Formula not actually implemented yet in descriptive_stats")
def test_descriptive_stats_formula_with_interaction():
    test_input = [
        {"name": "y", "value": "lefthippocampus,righthippocampus"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                    ],
                    "interactions": [
                        {
                            "var1": "lefthippocampus",
                            "var2": "righthippocampus",
                        }
                    ],
                }
            ),
        },
    ]
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)

    assert "Left Hippocampus" in result["single"].keys()
    assert "Right Hippocampus" in result["single"].keys()
    assert "lefthippocampus:righthippocampus" in result["single"].keys()

    assert "Left Hippocampus" in result["model"]["ppmi"]["data"].keys()
    assert "Right Hippocampus" in result["model"]["ppmi"]["data"].keys()
    assert "lefthippocampus:righthippocampus" in result["model"]["ppmi"]["data"].keys()
