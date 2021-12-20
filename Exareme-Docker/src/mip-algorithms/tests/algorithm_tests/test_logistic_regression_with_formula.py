import json

import pytest

from mipframework.testutils import get_algorithm_result
from LOGISTIC_REGRESSION import LogisticRegression


def test_logistic_regression_formula_no_interaction():
    test_input = [
        {"name": "x", "value": "rightgregyrusrectus,leftventraldc"},
        {"name": "y", "value": "rs17125944_c"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": '{ "single": [ {"var_name": "rightgregyrusrectus", "unary_operation": "nop"}, {"var_name": "leftventraldc", "unary_operation": "nop"}], "interactions": []}',
        },
        {"name": "positive_level", "value": "2"},
        {"name": "negative_level", "value": "1"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "rightgregyrusrectus",
        "leftventraldc",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


def test_logistic_regression_formula_2interaction():
    test_input = [
        {"name": "x", "value": "rightgregyrusrectus,leftventraldc"},
        {"name": "y", "value": "rs17125944_c"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": '{ "single": [ {"var_name": "rightgregyrusrectus", "unary_operation": "nop"}, {"var_name": "leftventraldc", "unary_operation": "nop"}], "interactions": [ {"var1": "rightgregyrusrectus", "var2": "leftventraldc"}]}',
        },
        {"name": "positive_level", "value": "2"},
        {"name": "negative_level", "value": "1"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "rightgregyrusrectus",
        "leftventraldc",
        "rightgregyrusrectus:leftventraldc",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


def test_logistic_regression_formula_3interaction():
    test_input = [
        {
            "name": "x",
            "value": "leftlorglateralorbitalgyrus,leftsplsuperiorparietallobule,rightprgprecentralgyrus,leftaorganteriororbitalgyrus,minimentalstate,leftaccumbensarea,leftmorgmedialorbitalgyrus,leftmfgmiddlefrontalgyrus,leftsogsuperioroccipitalgyrus",
        },
        {"name": "y", "value": "rs610932_a"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "ppmi,edsd,adni"},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "leftlorglateralorbitalgyrus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "leftsplsuperiorparietallobule",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "rightprgprecentralgyrus",
                            "unary_operation": "nop",
                        },
                    ],
                    "interactions": [
                        {
                            "var1": "leftlorglateralorbitalgyrus",
                            "var2": "leftsplsuperiorparietallobule",
                            "var3": "rightprgprecentralgyrus",
                        }
                    ],
                }
            ),
        },
        {"name": "filter", "value": ""},
        {"name": "positive_level", "value": "2"},
        {"name": "negative_level", "value": "1"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "leftlorglateralorbitalgyrus",
        "leftsplsuperiorparietallobule",
        "rightprgprecentralgyrus",
        "leftlorglateralorbitalgyrus:rightprgprecentralgyrus:leftsplsuperiorparietallobule",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


def test_logistic_regression_formula_log():
    test_input = [
        {"name": "x", "value": "righthippocampus,lefthippocampus"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "log",
                        },
                    ],
                    "interactions": [
                        {
                            "var1": "righthippocampus",
                            "var2": "lefthippocampus",
                        }
                    ],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "righthippocampus",
        "log(lefthippocampus)",
        "righthippocampus:lefthippocampus",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


def test_logistic_regression_formula_exp():
    test_input = [
        {"name": "x", "value": "righthippocampus,lefthippocampus"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "exp",
                        },
                    ],
                    "interactions": [
                        {
                            "var1": "righthippocampus",
                            "var2": "lefthippocampus",
                        }
                    ],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "righthippocampus",
        "exp(lefthippocampus)",
        "righthippocampus:lefthippocampus",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


def test_logistic_regression_formula_center():
    test_input = [
        {"name": "x", "value": "righthippocampus,lefthippocampus"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "center",
                        },
                    ],
                    "interactions": [
                        {
                            "var1": "righthippocampus",
                            "var2": "lefthippocampus",
                        }
                    ],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "righthippocampus",
        "center(lefthippocampus)",
        "righthippocampus:lefthippocampus",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


def test_logistic_regression_formula_standardize():
    test_input = [
        {"name": "x", "value": "righthippocampus,lefthippocampus"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "lefthippocampus",
                            "unary_operation": "standardize",
                        },
                    ],
                    "interactions": [
                        {
                            "var1": "righthippocampus",
                            "var2": "lefthippocampus",
                        }
                    ],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "righthippocampus",
        "standardize(lefthippocampus)",
        "righthippocampus:lefthippocampus",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


@pytest.mark.xfail(reason="Formula doesn't work with categorical vars")
def test_logistic_regression_formula_categorical_covariate():
    test_input = [
        {"name": "x", "value": "righthippocampus,gender"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "gender",
                            "unary_operation": "dummy",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "C(gender, Treatment)[T.M]",
        "righthippocampus",
        "C(gender, Treatment)[T.F]",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


@pytest.mark.xfail(reason="Formula doesn't work with categorical vars")
def test_logistic_regression_formula_categorical_covariate_with_interaction():
    test_input = [
        {"name": "x", "value": "righthippocampus,gender"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "gender",
                            "unary_operation": "dummy",
                        },
                    ],
                    "interactions": [
                        {
                            "var1": "righthippocampus",
                            "var2": "gender",
                        }
                    ],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "C(gender, Treatment)[T.M]",
        "righthippocampus",
        "righthippocampus:gender[T.M]",
        "C(gender, Treatment)[T.F]",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


@pytest.mark.xfail(reason="Formula doesn't work with categorical vars")
def test_logistic_regression_formula_categorical_covariate_diff():
    test_input = [
        {"name": "x", "value": "righthippocampus,gender"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "gender",
                            "unary_operation": "diff",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "C(gender, Diff)[D.F]",
        "righthippocampus",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


@pytest.mark.xfail(reason="Formula doesn't work with categorical vars")
def test_logistic_regression_formula_categorical_covariate_poly():
    test_input = [
        {"name": "x", "value": "righthippocampus,gender"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "gender",
                            "unary_operation": "poly",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "C(gender, Poly).Linear",
        "righthippocampus",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


@pytest.mark.xfail(reason="Formula doesn't work with categorical vars")
def test_logistic_regression_formula_categorical_covariate_sum():
    test_input = [
        {"name": "x", "value": "righthippocampus,gender"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "gender",
                            "unary_operation": "sum",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "C(gender, Sum)[S.F]",
        "righthippocampus",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])


@pytest.mark.xfail(reason="Formula doesn't work with categorical vars")
def test_logistic_regression_formula_categorical_covariate_helmert():
    test_input = [
        {"name": "x", "value": "righthippocampus,gender"},
        {"name": "y", "value": "alzheimerbroadcategory"},
        {"name": "pathology", "value": "dementia"},
        {"name": "dataset", "value": "adni,edsd,ppmi"},
        {"name": "filter", "value": ""},
        {
            "name": "formula",
            "value": json.dumps(
                {
                    "single": [
                        {
                            "var_name": "righthippocampus",
                            "unary_operation": "nop",
                        },
                        {
                            "var_name": "gender",
                            "unary_operation": "Helmert",
                        },
                    ],
                    "interactions": [],
                }
            ),
        },
        {"name": "positive_level", "value": "AD"},
        {"name": "negative_level", "value": "CN"},
    ]
    result = get_algorithm_result(LogisticRegression, test_input, num_workers=1)

    assert result["Names"] == [
        "Intercept",
        "C(gender, Helmert)[H.M]",
        "righthippocampus",
    ]
    assert len(result["Names"]) == len(result["z score"]) == len(result["Coefficients"])
