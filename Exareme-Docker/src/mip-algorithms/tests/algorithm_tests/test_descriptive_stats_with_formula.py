import json

import numpy as np  # type: ignore    XXX needed for patsy to know how to take logs and exps

from mipframework.testutils import get_algorithm_result
from DESCRIPTIVE_STATS.descriptive_stats import DescriptiveStats


def test_descriptive_stats_formula_nop_no_interaction():
    test_input = [
        {"name": "y", "value": "lefthippocampus,righthippocampus"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "edsd,ppmi"},
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

    single_results = result["single"]
    assert "Left Hippocampus" in single_results
    assert "Right Hippocampus" in single_results

    model_results = result["model"]
    assert "Left Hippocampus" in model_results["ppmi"]["data"].keys()
    assert "Right Hippocampus" in model_results["ppmi"]["data"].keys()
    assert "Left Hippocampus" in model_results["edsd"]["data"].keys()
    assert "Right Hippocampus" in model_results["edsd"]["data"].keys()


def test_descriptive_stats_formula_with_categorical_not_in_formula():
    test_input = [
        {"name": "y", "value": "lefthippocampus,alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "log",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
    ]
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)

    single_results = result["single"]
    assert "Left Hippocampus" in single_results
    assert "Alzheimer Broad Category" in single_results

    model_results = result["model"]
    assert "log(lefthippocampus)" in model_results["ppmi"]["data"].keys()
    assert len(model_results["ppmi"]["data"]) == 1


def test_descriptive_stats_formula_no_interaction_center_standardize_should_be_excluded():
    test_input = [
        {"name": "y", "value": "lefthippocampus,righthippocampus"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "center",
                        },
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "standardize",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
    ]
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)

    single_results = result["single"]
    assert "Left Hippocampus" in single_results
    assert "Right Hippocampus" in single_results

    model_results = result["model"]
    assert "center(lefthippocampus)" in model_results["ppmi"]["data"].keys()
    assert "standardize(righthippocampus)" in model_results["ppmi"]["data"].keys()
    assert "center(lefthippocampus)" in model_results["edsd"]["data"].keys()
    assert "standardize(righthippocampus)" in model_results["edsd"]["data"].keys()


def test_descriptive_stats_formula_no_interaction_log_exp():
    test_input = [
        {"name": "y", "value": "lefthippocampus,righthippocampus"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "log",
                        },
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "exp",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
    ]
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)

    single_results = result["single"]
    assert "Left Hippocampus" in single_results
    assert "Right Hippocampus" in single_results

    model_results = result["model"]
    assert "log(lefthippocampus)" in model_results["ppmi"]["data"].keys()
    assert "exp(righthippocampus)" in model_results["ppmi"]["data"].keys()
    assert "log(lefthippocampus)" in model_results["edsd"]["data"].keys()
    assert "exp(righthippocampus)" in model_results["edsd"]["data"].keys()


def test_descriptive_stats_formula_no_interaction_arithmetic():
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
                            "binary_operation": "div",
                            "operand": 10,
                        },
                        {
                            "var_name": "righthippocampus",
                            "binary_operation": "mul",
                            "operand": 10,
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
    ]
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)

    single_results = result["single"]
    assert "Left Hippocampus" in single_results
    assert "Right Hippocampus" in single_results

    model_results_ppmi = result["model"]["ppmi"]["data"]
    assert "(lefthippocampus / 10)" in model_results_ppmi
    assert "(righthippocampus * 10)" in model_results_ppmi
    model_results_edsd = result["model"]["edsd"]["data"]
    assert "(lefthippocampus / 10)" in model_results_edsd
    assert "(righthippocampus * 10)" in model_results_edsd


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

    assert "Left Hippocampus" in result["model"]["ppmi"]["data"].keys()
    assert "Right Hippocampus" in result["model"]["ppmi"]["data"].keys()
    assert "lefthippocampus:righthippocampus" in result["model"]["ppmi"]["data"].keys()


def test_descriptive_stats_formula_all_features():
    test_input = [
        {
            "name": "y",
            "value": ",".join(
                [
                    "_3rdventricle",
                    "_4thventricle",
                    "righthippocampus",
                    "lefthippocampus",
                    "leftputamen",
                    "leftcuncuneus",
                    "leftpallidum",
                ]
            ),
        },
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "ppmi,edsd"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "_3rdventricle",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "_4thventricle",
                            "unary_operation": "log",
                        },
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "exp",
                        },
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "center",
                        },
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "standardize",
                        },
                        {
                            "var_name": "leftputamen",
                            "binary_operation": "mul",
                            "operand": 10,
                        },
                        {
                            "var_name": "leftcuncuneus",
                            "binary_operation": "div",
                            "operand": 10,
                        },
                    ],
                    "interactions": [
                        {
                            "var1": "_3rdventricle",
                            "var2": "_4thventricle",
                        },
                        {
                            "var1": "leftputamen",
                            "var2": "leftcuncuneus",
                            "var3": "lefthippocampus",
                        },
                    ],
                }
            ),
        },
    ]
    result = get_algorithm_result(DescriptiveStats, test_input, num_workers=1)
    single_results = result["single"]
    for varname in [
        u"3rd Ventricle",
        u"Left Hippocampus",
        u"Left Pallidum",
        u"Left cuneus",
        u"4th Ventricle",
        u"Right Hippocampus",
        u"Left Putamen",
    ]:
        assert varname in single_results

    model_results_ppmi = result["model"]["ppmi"]["data"]
    for varname in [
        u"3rd Ventricle",
        u"log(_4thventricle)",
        u"leftputamen:lefthippocampus:leftcuncuneus",
        u"(leftcuncuneus / 10)",
        u"(leftputamen * 10)",
        u"exp(righthippocampus)",
        u"center(lefthippocampus)",
        u"standardize(lefthippocampus)",
        u"_3rdventricle:_4thventricle",
    ]:
        assert varname in model_results_ppmi
    model_results_edsd = result["model"]["edsd"]["data"]
    for varname in [
        u"3rd Ventricle",
        u"log(_4thventricle)",
        u"leftputamen:lefthippocampus:leftcuncuneus",
        u"(leftcuncuneus / 10)",
        u"(leftputamen * 10)",
        u"exp(righthippocampus)",
        u"center(lefthippocampus)",
        u"standardize(lefthippocampus)",
        u"_3rdventricle:_4thventricle",
    ]:
        assert varname in model_results_edsd
