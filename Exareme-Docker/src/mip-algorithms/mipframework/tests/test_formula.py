import pytest

from mipframework.formula import (
    generate_formula,
    get_term_unary_op,
    FormulaInvalidOperator,
    get_term_binary_op,
)


def test_unparse_formula_one_single_nop():
    formula_data = {
        "single": [{"var_name": "var1", "unary_operation": "nop"}],
        "interactions": [],
    }
    expected = "var1"
    result = generate_formula(formula_data)
    assert expected == result


def test_unparse_formula_one_single_log():
    formula_data = {
        "single": [{"var_name": "var1", "unary_operation": "log"}],
        "interactions": [],
    }
    expected = "log(var1)"
    result = generate_formula(formula_data)
    assert expected == result


def test_unparse_formula_two_singles():
    formula_data = {
        "single": [
            {"var_name": "var1", "unary_operation": "nop"},
            {"var_name": "var2", "unary_operation": "log"},
        ],
        "interactions": [],
    }
    expected = "var1 + log(var2)"
    result = generate_formula(formula_data)
    assert expected == result


def test_unparse_formula_one_single_binop():
    formula_data = {
        "single": [
            {"var_name": "var1", "unary_operation": "nop"},
            {"var_name": "var2", "binary_operation": "mul", "operand": 2},
        ],
        "interactions": [],
    }
    expected = "var1 + 2*var2"
    result = generate_formula(formula_data)
    assert expected == result


def test_unparse_formula_multiple_singles():
    formula_data = {
        "single": [
            {"var_name": "var1", "unary_operation": "nop"},
            {"var_name": "var2", "unary_operation": "log"},
            {"var_name": "var3", "unary_operation": "center"},
            {"var_name": "var4", "binary_operation": "mul", "operand": 2},
            {"var_name": "var5", "binary_operation": "div", "operand": 2},
        ],
        "interactions": [],
    }
    expected = "var1 + log(var2) + center(var3) + 2*var4 + var5/2"
    result = generate_formula(formula_data)
    assert expected == result


def test_unparse_formula_one_interaction():
    formula_data = {
        "single": [
            {"var_name": "var1", "unary_operation": "nop"},
            {"var_name": "var2", "unary_operation": "nop"},
        ],
        "interactions": [{"var1": "var1", "var2": "var2"}],
    }
    expected = "var1 + var2 + var1:var2"
    result = generate_formula(formula_data)
    assert expected == result


def test_unparse_formula_two_interactions():
    formula_data = {
        "single": [
            {"var_name": "var1", "unary_operation": "nop"},
            {"var_name": "var2", "unary_operation": "nop"},
            {"var_name": "var3", "unary_operation": "nop"},
        ],
        "interactions": [
            {"var1": "var1", "var2": "var2"},
            {"var1": "var2", "var2": "var3"},
        ],
    }
    expected = "var1 + var2 + var3 + var1:var2 + var2:var3"
    result = generate_formula(formula_data)
    assert expected == result


def test_unparse_formula_one_triple_interaction():
    formula_data = {
        "single": [
            {"var_name": "var1", "unary_operation": "nop"},
            {"var_name": "var2", "unary_operation": "nop"},
            {"var_name": "var3", "unary_operation": "nop"},
        ],
        "interactions": [
            {"var1": "var1", "var2": "var2", "var3": "var3"},
        ],
    }
    expected = "var1 + var2 + var3 + var1:var2:var3"
    result = generate_formula(formula_data)
    assert expected == result


def test_get_term_unary_op():
    var_name = "var1"
    op = "nop"
    expected = "var1"
    result = get_term_unary_op(var_name, op)
    assert expected == result


def test_get_term_unary_op_invalid_op():
    var_name = "var1"
    op = "invalid_op"
    with pytest.raises(FormulaInvalidOperator):
        get_term_unary_op(var_name, op)


def test_get_term_binary_op_mul():
    var_name = "var1"
    op = "mul"
    operand = 2
    expected = "2*var1"
    result = get_term_binary_op(var_name, op, operand)
    assert result == expected


def test_get_term_binary_op_div():
    var_name = "var1"
    op = "div"
    operand = 2
    expected = "var1/2"
    result = get_term_binary_op(var_name, op, operand)
    assert result == expected


def test_get_term_binary_op_invalid():
    with pytest.raises(FormulaInvalidOperator):
        get_term_binary_op("", "invalid_op", 0)
