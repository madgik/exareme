import re


def generate_formula(formula_data, dependent_var=""):
    single_terms = []
    for term in formula_data["single"]:
        var_name = term["var_name"]
        if "unary_operation" in term:
            single_terms.append(get_term_unary_op(var_name, term["unary_operation"]))
        elif "binary_operation" in term:
            single_terms.append(
                get_term_binary_op(var_name, term["binary_operation"], term["operand"])
            )

    interactions = []
    for term in formula_data["interactions"]:
        var_names = list(term.values())
        interactions.append(":".join(var_names))

    formula_expr = " + ".join(single_terms)
    if interactions:
        formula_expr += " + " + " + ".join(interactions)
    if dependent_var:
        return dependent_var + "~" + formula_expr
    return formula_expr


def get_term_unary_op(var_name, op):
    op_to_formula_term = {
        "nop": "{}",
        "log": "np.log({})",
        "exp": "np.exp({})",
        "center": "patsy.center({})",
        "standardize": "patsy.standardize({})",
        "dummy": "C({}, Treatment)",
        "diff": "C({}, Diff)",
        "poly": "C({}, Poly)",
        "sum": "C({}, Sum)",
        "Helmert": "C({}, Helmert)",
    }
    if op in op_to_formula_term.keys():
        return op_to_formula_term[op].format(var_name)
    raise FormulaInvalidOperator("Invalid operator: {}".format(op))


def get_term_binary_op(var_name, op, operand):
    if op == "mul":
        return "{0}*{1}".format(operand, var_name)
    if op == "div":
        return "{0}/{1}".format(var_name, operand)
    raise FormulaInvalidOperator("Invalid operator: {}".format(op))


class FormulaInvalidOperator(Exception):
    """Raise when an invalid operator is encountered."""


def generate_formula_from_variable_lists(args):
    if hasattr(args, "x") and args.x:
        formula = "+".join(args.y) + "~" + "+".join(args.x)
        if not args.intercept:
            formula += "-1"
    else:
        formula = "+".join(args.y) + "-1"
    return formula


def insert_explicit_coding_for_categorical_vars(formula, var_names, is_categorical):
    for var in var_names:
        if is_categorical[var]:
            formula = re.sub(
                r"\b({})\b".format(var),
                r"C(\g<0>, Treatment)",
                formula,
            )
    return formula
