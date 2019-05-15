import re
import ply.lex as lex
import ply.yacc as yacc

import json

def dq(string):
    return '"' + string + '"'

class Tree(object):
    "Generic tree node."

    def __init__(self, name='root', children=None):
        self.name = name
        self.children = []
        if children is not None:
            for child in children:
                self.add_child(child)

    def __repr__(self):
        if self.children != []:
            return '{' + dq(self.name) + ': [' + repr(self.children[0]) + ', ' + repr(self.children[1]) + ' ] }'
        else:
            return dq(self.name)

    def add_child(self, node):
        assert isinstance(node, Tree)
        self.children.append(node)


# Input formula
# formula = input('Enter formula:\n')
formula = 'a : (b + c) * (f + g + h) '

# Format spaces
formula = formula.replace(' ', '') \
    .replace('+', ' + ') \
    .replace(':', ' : ') \
    .replace('*', ' * ')
print('Formula:')
print('  ' + formula)

# Expand exponents into products (using *)
regex_exp = r'((\([^\)]+?\))\^(\d+))'
exp_groups = re.findall(regex_exp, formula)
subs = []
for i, g in enumerate(exp_groups):
    try:
        reps = int(g[2])
    except:
        raise ValueError('Exponent must be an integer')
    sub = ' * '.join([g[1]] * reps)
    subs.append((g[0], sub))
for sub in subs:
    formula = formula.replace(sub[0], sub[1])

# List of token names.
tokens = (
    'VAR',
    'NUMBER',
    'PLUS',
    'COLON',
    'STAR',
    'LPAREN',
    'RPAREN',
)

# Regular expression rules for simple tokens
t_VAR = r'(?!\*)[a-zA-Z][a-zA-Z_0-9]*(?!\*)'
t_NUMBER = r'(1|-1|0|-0)'
t_PLUS = r'\+'
t_COLON = r':'
t_STAR = r'\*'
t_LPAREN = r'\('
t_RPAREN = r'\)'

# A string containing ignored characters (spaces and tabs)
t_ignore = ' \t'


# Error handling rule
def t_error(t):
    print("Illegal character '%s'" % t.value[0])
    t.lexer.skip(1)


# Build the lexer
lexer = lex.lex()

# Give the lexer some input
lexer.input(formula)

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
    p[0] = Tree('PLUS', [p[1], p[3]])


def p_expression_term(p):
    'expression : term'
    p[0] = p[1]


def p_term_colon(p):
    'term : term COLON factor'
    p[0] = Tree('COLON', [p[1], p[3]])


def p_term_star(p):
    'term : term STAR factor'
    p[0] = Tree('PLUS', [Tree('PLUS', [p[1], p[3]]),
                         Tree('COLON', [p[1], p[3]])])


def p_term_factor(p):
    'term : factor'
    p[0] = p[1]


def p_factor_expr(p):
    'factor : LPAREN expression RPAREN'
    p[0] = p[2]


def p_factor_var(p):
    'factor : VAR'
    p[0] = Tree(p[1], children=None)  # a VAR is a leaf


def p_factor_number(p):
    'factor : NUMBER'
    # if p[1] != '1':
    #     raise ValueError('NUMBER should only be 1 here')
    p[0] = Tree(p[1], children=None)  # a number is leaf


# Error rule for syntax errors
def p_error(p):
    print("Syntax error in input!")


# Build the parser
parser = yacc.yacc()

query = parser.parse(formula)

print(query)

tree_json = repr(query)

tree_dict = {}

def create_tree_dict(root, visited):
    nodeid = id(root)
    if nodeid not in visited:
        visited.add(nodeid)
        for child in root.children:
            create_tree_dict(child, visited)
    if root.children != []:
        tree_dict[root.name + '_' + str(id(root))] = [root.children[0], root.children[1]]
    else:
        tree_dict[root.name + '_' + str(id(root))] = ()
    return visited

create_tree_dict(query, set())
for key in tree_dict.keys():
    print(key + ' -> ' + str(tree_dict[key]))

def dfs(tree, node, visited):
    if node not in visited:
        visited.append(node)
        for child in tree[node]:
            child = child.name + '_' + str(id(child))
            dfs(tree, child, visited)
    return visited

visited = dfs(tree_dict, query.name + '_' + str(id(query)), [])
print(visited)

# def dfs(root, visited):
#      nodeid = id(root)
#      if nodeid not in visited:
#          visited.add(nodeid)
#          for child in root.children:
#              create_tree_dict(child, visited)
#      if root.name == 'COLON':
#          child0 = root.children[0]
#          child1 = root.children[1]
#          if child0.name == 'PLUS':
#              gc0 = child.children[0]
#              gc1 = child.children[1]
#              root = Tree('PLUS', [Tree('COLON', [gc0, child1]), Tree('COLON', [gc1, child1])])
#          if child1.name == 'PLUS':
#              gc0 = child.children[0]
#              gc1 = child.children[1]
#              root = Tree('PLUS', [Tree('COLON', [child0, gc0]), Tree('COLON', [child0, gc1])])
#      return visited




# with open('query.json', 'wb') as f:
#     f.write(json.dumps(tree_json))