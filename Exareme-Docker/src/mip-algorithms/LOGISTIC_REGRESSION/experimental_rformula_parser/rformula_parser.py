from itertools import chain, combinations


def powerset_from2(iterable):
    "powerset_from2([1,2,3]) --> (1,2) (1,3) (2,3) (1,2,3)"
    s = list(iterable)
    return chain.from_iterable(combinations(s, r) for r in range(2, len(s) + 1))


def formula2query(formula, table_name):
    terms = list(
            map(lambda col: col.strip(), formula.split('+'))
    )
    query_terms = []
    if '0' not in terms and '-1' not in terms:
        query_terms.append('1 AS Intercept')
    for term in terms:
        if ':' in term:
            term_factors = list(
                    map(lambda t: t.strip(), term.split(':'))
            )
            prod = ' * '.join(term_factors)
            query_terms.append(prod)
        elif '*' in term:
            term_factors = list(
                    map(lambda t: t.strip(), term.split('*'))
            )
            for factor in term_factors:
                query_terms.append(factor)
            interaction_terms = powerset_from2(term_factors)
            for prod in interaction_terms:
                prod = ' * '.join(prod)
                query_terms.append(prod)
        else:
            if term != '0' and term != '-1':
                query_terms.append(term)
    used = set()
    query_terms = [x for x in query_terms if x not in used and (used.add(x) or True)]  # remove duplicates
    query = 'SELECT ' + ', '.join(query_terms) + ' FROM ' + table_name + ';'
    return query, len(query_terms)



query, n_cols = formula2query('col1*col2*col3*col4', 'adni')
# print(n_cols)
print(query)