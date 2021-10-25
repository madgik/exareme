import pytest

from mipframework.formula import (
    generate_formula,
    get_term_unary_op,
    FormulaInvalidOperator,
    get_term_binary_op,
    generate_formula_from_variable_lists,
    insert_explicit_coding_for_categorical_vars,
)


@pytest.fixture
def args():
    class Args:
        pass

    return Args()


def test_unparse_formula_one_single_nop():
    formula_data = {
        "single": [{"var_name": "var1", "unary_operation": "nop"}],
        "interactions": [],
    }
    expected = "var1"
    result = generate_formula(formula_data)
    assert expected == result


def test_generate_formula_with_dependent_var():
    formula_data = {
        "single": [{"var_name": "var1", "unary_operation": "nop"}],
        "interactions": [],
    }
    expected = "y~var1"
    result = generate_formula(formula_data, "y")
    assert expected == result


def test_unparse_formula_one_single_log():
    formula_data = {
        "single": [{"var_name": "var1", "unary_operation": "log"}],
        "interactions": [],
    }
    expected = "np.log(var1)"
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
    expected = "var1 + np.log(var2)"
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
    expected = "var1 + np.log(var2) + patsy.center(var3) + 2*var4 + var5/2"
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
    expected = "var1 + var2 + var3 + var1:var3:var2"
    result = generate_formula(formula_data)
    assert expected == result


def test_get_term_unary_op_nop():
    var_name = "var1"
    op = "nop"
    expected = "var1"
    result = get_term_unary_op(var_name, op)
    assert expected == result


def test_get_term_unary_op_log():
    var_name = "var1"
    op = "log"
    expected = "np.log(var1)"
    result = get_term_unary_op(var_name, op)
    assert expected == result


def test_get_term_unary_op_exp():
    var_name = "var1"
    op = "exp"
    expected = "np.exp(var1)"
    result = get_term_unary_op(var_name, op)
    assert expected == result


def test_get_term_unary_op_center():
    var_name = "var1"
    op = "center"
    expected = "patsy.center(var1)"
    result = get_term_unary_op(var_name, op)
    assert expected == result


def test_get_term_unary_op_standardize():
    var_name = "var1"
    op = "standardize"
    expected = "patsy.standardize(var1)"
    result = get_term_unary_op(var_name, op)
    assert expected == result


@pytest.mark.skip
def test_get_term_unary_op_dummy():
    var_name = "var1"
    op = "dummy"
    expected = "C(var1, Treatment)"
    result = get_term_unary_op(var_name, op)
    assert expected == result


@pytest.mark.skip
def test_get_term_unary_op_diff():
    var_name = "var1"
    op = "diff"
    expected = "C(var1, Diff)"
    result = get_term_unary_op(var_name, op)
    assert expected == result


@pytest.mark.skip
def test_get_term_unary_op_helmert():
    var_name = "var1"
    op = "Helmert"
    expected = "C(var1, Helmert)"
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


def test_generate_formula_from_variable_list_only_y(args):
    args.y = ["a", "b", "c"]
    expected_formula = "a+b+c-1"
    formula = generate_formula_from_variable_lists(args)
    assert formula == expected_formula


def test_generate_formula_from_variable_list_xy_with_intercept(args):
    args.y = ["y"]
    args.x = ["a", "b", "c"]
    args.intercept = True
    expected_formula = "y~a+b+c"
    formula = generate_formula_from_variable_lists(args)
    assert formula == expected_formula


def test_generate_formula_from_variable_list_xy_without_intercept(args):
    args.y = ["y"]
    args.x = ["a", "b", "c"]
    args.intercept = False
    expected_formula = "y~a+b+c-1"
    formula = generate_formula_from_variable_lists(args)
    assert formula == expected_formula


@pytest.mark.skip
def test_insert_explicit_coding_for_categorical_vars_with_coding(args):
    args.var_names = ["a", "b", "c"]
    args.coding = "Helmert"
    formula = "a+b+c"
    is_categorical = {"a": False, "b": False, "c": True}
    expected_formula = "a+b+C(c, Helmert)"
    formula = insert_explicit_coding_for_categorical_vars(formula, args, is_categorical)
    assert formula == expected_formula
