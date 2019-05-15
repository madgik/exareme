import re
import ply.lex as lex
import ply.yacc as yacc

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

# Input formula
formula = input('Enter formula:\n')
# formula = 'c1+ c2 + x*y*z + s* t+ a:b '
print('Formula:')
print('  ' + formula)
# Format spaces
formula_fmt = formula.replace(' ', '')\
                     .replace('*', ' * ')\
                     .replace('+', ' + ')\
                     .replace(':', ' : ')
# Substitute x1 * x2 * x3 with _ter_star x1 x2 x3
regex_terstar = r'(?!\* )([a-zA-Z][a-zA-Z_0-9]*)' \
                r' \* ([a-zA-Z][a-zA-Z_0-9]*)' \
                r' \* ([a-zA-Z][a-zA-Z_0-9]*)(?! \*)'
formula_fmt = re.sub(regex_terstar, r'_ter_star \1 \2 \3', formula_fmt)
# Substitute x1 * x2 with _bin_star x1 x2
regex_binstar = r'(?!\* )([a-zA-Z][a-zA-Z_0-9]*)' \
                r' \* ([a-zA-Z][a-zA-Z_0-9]*)(?! \*)'
formula_fmt = re.sub(regex_binstar, r'_bin_star \1 \2', formula_fmt)

print('Intermediate Representation:')
print('  ' + formula_fmt)

# List of token names.
tokens = (
    'VAR',
    'NUMBER',
    'PLUS',
    'BIN_STAR',
    'TER_STAR',
    'COLON',
)

# Regular expression rules for simple tokens
t_VAR = r'[a-zA-Z][a-zA-Z_0-9]*'
t_NUMBER = r'(0|-1|-0)'
t_PLUS = r'\+'
t_BIN_STAR = r'_bin_star'
t_TER_STAR = r'_ter_star'
t_COLON = r':'

# A string containing ignored characters (spaces and tabs)
t_ignore  = ' \t'

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
    if tok.type == 'VAR' and tok.value not in VAR_NAMES:
        raise KeyError('No variable {}'.format(tok.value))
    print(tok)


# Yacc parser
def p_expression_plus(p):
    'expression : expression PLUS term'
    p[0] = p[1] + ', ' + p[3]


def p_expression_term(p):
    'expression : term'
    p[0] = p[1]


def p_term_colon(p):
    'term : term COLON factor'
    p[0] = p[1] + ' * ' + p[3]


def p_term_factor(p):
    'term : factor'
    p[0] = p[1]


def p_factor_var(p):
    'factor : VAR'
    p[0] = p[1]


def p_factor_number(p):
    'factor : NUMBER'
    p[0] = '(no_intercept)'


def p_term_binstar(p):
    'factor : BIN_STAR VAR VAR'
    p[0] = p[2] + ', ' + p[3] + ', ' + p[2] + ' * ' + p[3]

def p_term_terstar(p):
    'factor : TER_STAR VAR VAR VAR'
    p[0] = p[2] + ', ' + p[3] + ', ' + p[4] + ', '\
         + p[2] + ' * ' + p[3] + ', '\
         + p[2] + ' * ' + p[4] + ', '\
         + p[3] + ' * ' + p[4] + ', '\
         + p[2] + ' * ' + p[3] + ' * ' + p[4]

    # Error rule for syntax errors


def p_error(p):
    print("Syntax error in input!")

    # Build the parser


parser = yacc.yacc()

query = parser.parse(formula_fmt)

if '(no_intercept)' in query:
    query = query.replace('(no_intercept), ', '')
    query = 'SELECT ' + query + ' FROM TABLE;'
else:
    query = 'SELECT 1 AS (Intercept), ' + query + ' FROM TABLE;'

print(query)
