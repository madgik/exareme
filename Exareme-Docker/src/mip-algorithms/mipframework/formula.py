def generate_formula(formula_data):
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
    return formula_expr


def get_term_unary_op(var_name, op):
    if op not in ("nop", "log", "exp", "center", "standardize"):
        raise FormulaInvalidOperator("Invalid operator: {}".format(op))
    if op == "nop":
        return var_name
    return "{0}({1})".format(op, var_name)


def get_term_binary_op(var_name, op, operand):
    if op == "mul":
        return "{0}*{1}".format(operand, var_name)
    if op == "div":
        return "{0}/{1}".format(var_name, operand)
    raise FormulaInvalidOperator("Invalid operator: {}".format(op))


class FormulaInvalidOperator(Exception):
    """Raise when an invalid operator is encountered."""
