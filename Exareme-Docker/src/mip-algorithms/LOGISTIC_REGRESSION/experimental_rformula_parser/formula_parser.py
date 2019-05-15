import re
import ply.lex as lex
import ply.yacc as yacc
from itertools import chain, combinations

VAR_NAMES = (
    'c1',
    'c2',
    'c3',
    'x',
    'y',
    'z',
    'w',
    's',
    't',
    'a',
    'b',
    'c'
)


def powerset(iterable, limit=None):
    """
    powerset([1,2,3], limit=None) --> (1,) (2,) (3,) (1,2) (1,3) (2,3) (1,2,3)
    powerset([1,2,3], limit=2) --> (1,) (2,) (3,) (1,2) (1,3) (2,3)
    :param iterable:
    :param limit:
    :return:
    """
    s = list(iterable)
    if limit is None:
        limit = len(s)
    return chain.from_iterable(combinations(s, r) for r in range(1, limit + 1))


# Input formula
formula = input('Enter formula:\n')
# formula = 'c1+ c2 + x*y*z + s* t+ a:b '
print('Formula:')
print('  ' + formula)
# Format spaces
formula_fmt = formula.replace(' ', '') \
    .replace('+', ' + ') \
    .replace(':', ' : ')
# Substitutes the patterns: x1*x2*... -> _star x1*x2*...
regex_star = r'(?!\*)((([a-zA-Z][a-zA-Z_0-9]*)\*)+([a-zA-Z][a-zA-Z_0-9]*))(?!\*)'
groups = re.findall(regex_star, formula_fmt)
for g in groups:
    formula_fmt = formula_fmt.replace(g[0], '_star ' + g[0])

print('Intermediate Representation:')
print('  ' + formula_fmt)

# List of token names.
tokens = (
    'VAR',
    'STARARG',
    'NUMBER',
    'PLUS',
    'STAR',
    'COLON',
    'LPAREN',
    'RPAREN',
    'HAT',
    'CROSS_LIMIT'
)

# Regular expression rules for simple tokens
t_VAR = r'(?!\*)[a-zA-Z][a-zA-Z_0-9]*(?!\*)'
t_STARARG = r'(?!\*)([a-zA-Z][a-zA-Z_0-9]*\*)+[a-zA-Z][a-zA-Z_0-9]*(?!\*)'
t_NUMBER = r'(0|-1|-0)'
t_PLUS = r'\+'
t_STAR = r'_star'
t_COLON = r':'
t_LPAREN = r'\('
t_RPAREN = r'\)'
t_HAT = r'\^'
t_CROSS_LIMIT = r'(?!\)\^)\d+'

# A string containing ignored characters (spaces and tabs)
t_ignore = ' \t'


# Error handling rule
def t_error(t):
    print("Illegal character '%s'" % t.value[0])
    t.lexer.skip(1)


# Build the lexer
lexer = lex.lex()

# Give the lexer some input
lexer.input(formula_fmt)

# Tokenize
while True:
    tok = lexer.token()
    if not tok:
        break  # No more input
    # if tok.type == 'VAR' and tok.value not in VAR_NAMES:
    #     raise KeyError('No variable {}'.format(tok.value))
    print(tok)


# Yacc parser
def p_expression_plus(p):
    'expression : expression PLUS term'
    p[0] = p[1] + ', ' + p[3]


def p_expression_term(p):
    'expression : term'
    p[0] = p[1]


def p_term_number(p):
    'term : NUMBER'
    p[0] = '(no_intercept)'


def p_term_var(p):
    'term : VAR'
    p[0] = p[1]


def p_term_star(p):
    'term : STAR STARARG'
    args = p[2].split('*')
    ps = list(powerset(args))
    res = []
    for t in ps:
        if len(t) == 1:
            res.append(t[0])
        elif len(t) > 1:
            res.append(' * '.join(t))
        else:
            raise ValueError('Invalid star arguments')
    p[0] = ', '.join(res)


def p_term_star_paren(p):
    'term : LPAREN STAR STARARG RPAREN'
    args = p[3].split('*')
    ps = list(powerset(args))
    res = []
    for t in ps:
        if len(t) == 1:
            res.append(t[0])
        elif len(t) > 1:
            res.append(' * '.join(t))
        else:
            raise ValueError('Invalid star arguments')
    p[0] = ', '.join(res)


def p_term_star_limit(p):
    'term : LPAREN STAR STARARG RPAREN HAT CROSS_LIMIT'
    args = p[3].split('*')
    try:
        limit = int(p[6])
    except ValueError:
        print('Illegal crossing limit', p[6])
    if limit < len(args):
        ps = list(powerset(args, limit=limit))
    else:
        ps = list(powerset(args))
    res = []
    for t in ps:
        if len(t) == 1:
            res.append(t[0])
        elif len(t) > 1:
            res.append(' * '.join(t))
        else:
            raise ValueError('Invalid star arguments')
    p[0] = ', '.join(res)


def p_term_factor(p):
    'term : factor COLON VAR'
    p[0] = p[1] + ' * ' + p[3]


def p_factor_facotr(p):
    'factor : factor COLON VAR'
    p[0] = p[1] + ' * ' + p[3]


def p_factor_var(p):
    'factor : VAR'
    p[0] = p[1]


# Error rule for syntax errors
def p_error(p):
    print("Syntax error in input!")


# Build the parser
parser = yacc.yacc()

query = parser.parse(formula_fmt)

if query is not None:
    if '(no_intercept)' in query:
        query = query.replace('(no_intercept), ', '')
        query = 'SELECT ' + query + ' FROM TABLE;'
    else:
        query = 'SELECT 1 AS (Intercept), ' + query + ' FROM TABLE;'

    print('Query:')
    print('  ' + query)
