import json

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
