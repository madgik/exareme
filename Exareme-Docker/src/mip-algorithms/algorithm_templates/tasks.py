import os
import errno
from collections import OrderedDict
import json
import glob
from string import Template

from invoke import task

_properties_types = {
    'local-global'      : 'python_local_global',
    'multi-local-global': 'python_multiple_local_global',
    'iterative'         : 'python_iterative'
}

_template_dirnames = {
    'local-global'      : 'TEMPLATE_LOCAL_GLOBAL',
    'multi-local-global': 'TEMPLATE_MULTIPLE_LOCAL_GLOBAL',
    'iterative'         : 'TEMPLATE_ITERATIVE'
}

properties_formula = [
    {
        "name"         : "formula",
        "desc"         : "Patsy formula  (R language syntax).",
        "type"         : "other",
        "value"        : "",
        "defaultValue" : "",
        "valueNotBlank": False,
        "valueMultiple": False,
        "valueType"    : "string"
    },
    {
        "name"         : "no_intercept",
        "desc"         : "Boolean flag for having no intercept by default.",
        "type"         : "other",
        "value"        : "true",
        "defaultValue" : "true",
        "valueNotBlank": False,
        "valueMultiple": False,
        "valueType"    : "string",
        "enumValues"   : [
            "true",
            "false"
        ]
    },
    {
        "name"         : "coding",
        "desc"         : "Coding method for categorical variables.",
        "type"         : "other",
        "value"        : "null",
        "defaultValue" : "null",
        "valueNotBlank": False,
        "valueMultiple": False,
        "valueType"    : "string",
        "enumValues"   : [
            "null",
            "Treatment",
            "Diff",
            "Poly",
            "Sum",
            "Helmert"
        ]
    }
]

properties_defaults = [
    {
        "name"         : "pathology",
        "desc"         : "The name of the pathology that the dataset belongs to.",
        "type"         : "pathology",
        "value"        : "dementia",
        "valueNotBlank": True,
        "valueMultiple": False,
        "valueType"    : "string"
    },
    {
        "name"         : "dataset",
        "desc"         : "It contains the names of one or more datasets, in which the algorithm will be executed. It cannot be empty",
        "type"         : "dataset",
        "value"        : "adni",
        "valueNotBlank": True,
        "valueMultiple": True,
        "valueType"    : "string"
    },
    {
        "name"         : "filter",
        "desc"         : "",
        "type"         : "filter",
        "value"        : "",
        "valueNotBlank": False,
        "valueMultiple": True,
        "valueType"    : "string"
    }
]


@task(help={
    'name'    : 'Name of the algorithm to create.',
    'alg-type': 'Type of algorithm to create. (local-global, multi-local-global, iterative)'
})
def create(c, name, alg_type):
    # Prepare names
    dirname = name.upper()
    alg_name = name.lower()
    class_prefix = to_camel_case(name)
    # Clean if exists
    if os.path.exists(os.path.join('output', dirname)):
        print('Cleaning previous output')
        remove(c, name)

    start_message = 'Creating a {alg_type} algorithm named {name}.'.format(alg_type=alg_type, name=name)
    print(start_message)
    print()

    properties = build_properties(alg_type, name)

    template_replace = {
        'template'                      : alg_name,
        'Template'                      : class_prefix,
        'TEMPLATE_LOCAL_GLOBAL'         : dirname,
        'TEMPLATE_MULTIPLE_LOCAL_GLOBAL': dirname,
        'TEMPLATE_ITERATIVE'            : dirname,
    }

    # Copy files
    dirname = os.path.join('output', dirname)
    os.mkdir('{dirname}'.format(dirname=dirname))
    templ_root = os.path.join('templates', _template_dirnames[alg_type])
    for filename in glob.iglob(os.path.join(templ_root, '**', '*.tmpl'), recursive=True):
        with open(filename, 'r') as f:
            content = f.read()
        new_content = Template(content).substitute(template_replace)
        path, fn = os.path.split(filename)
        path = os.path.relpath(path, templ_root)
        fn, ext = os.path.splitext(fn)
        new_filename = os.path.join(dirname, path)
        with safe_open_w(os.path.join('{new_fn}'.format(new_fn=new_filename), fn + '.py')) as f:
            f.write(new_content)
    c.run('mv {dirname}/template_lib.py {dirname}/{name}_lib.py'.format(dirname=dirname, name=name))
    # Create properties.json
    properties_path = '{dirname}/properties.json'.format(dirname=dirname)  # TODO use path.join
    with open(properties_path, 'w') as f:
        json.dump(properties, f, indent=4)
    # Call make_tests
    make_tests(c, name, properties)


@task(optional=['properties'], help={
    'name': 'Name of the algorithm to create.'
})
def make_tests(c, name, properties=None):
    # Prepare names
    name_caps = name.upper()
    dirname = os.path.join('output', 'tests_' + name_caps)
    alg_name = name.lower()
    class_prefix = to_camel_case(name)
    # Clean if exists
    if os.path.exists(dirname):
        print('Cleaning previous output')
        # TODO
    # Start
    start_message = 'Creating tests for {name}.'.format(name=name)
    print(start_message)
    print()
    ip = input('Enter the ip of the machine where you will deploy (or leave blank for later):\n')
    if not ip:
        ip = '0.0.0.0'
    template_replace = {
        'Template': class_prefix,
        'TEMPLATE': name_caps,
        'ip'      : ip
    }
    # Copy template unittest
    os.mkdir('{dirname}'.format(dirname=dirname))
    fn_tests_templ = os.path.join('templates', 'TEMPLATE_UNITTESTS', 'test_Template.tmpl')
    with open(fn_tests_templ, 'r') as f:
        content = f.read()
    new_content = Template(content).substitute(template_replace)
    with safe_open_w(os.path.join(dirname, 'test_' + class_prefix + '.py')) as f:
        f.write(new_content)
    # Create expected results json
    if not properties:
        properties = build_properties('iterative', name)
    expected = OrderedDict()
    expected['result'] = [{
        'input' : [],
        'output': ['TODO: PUT ALGORITHM OUTPUT HERE']
    }]
    for p in properties['parameters']:
        expected['result'][0]['input'].append({
            'name' : p['name'],
            'value': None
        })
    expected_path = os.path.join(dirname, 'expected_' + class_prefix + '.json')
    with open(expected_path, 'w') as f:
        json.dump(expected, f, indent=4)


def build_properties(alg_type, name):
    properties = OrderedDict()
    properties['name'] = name.upper()
    properties['desc'] = input('Enter a short description for your algorithm:\n')
    properties['label'] = input('Enter a label for your algorithm:\n')
    properties['type'] = _properties_types[alg_type]
    properties['status'] = 'enabled'
    properties['parameters'] = []
    first = True
    while True:
        a = 'a' if first else 'another'
        column_choice = input("Do you want to add {a} column parameter? (y, n)\n".format(a=a))
        assert column_choice in {'y', 'n'}
        first = False
        if column_choice == 'y':
            param_name = input('Enter column parameter name:\n')
            param_sql_type = input('Enter column values SQL types, comma separated: (real, integer, text)\n')
            param_iscateg = input('Are the column values categorical, numerical or both? (c, n, b)\n')
            assert param_iscateg in {'c', 'n', 'b'}
            if param_iscateg == 'c':
                param_iscateg = 'true'
            elif param_iscateg == 'n':
                param_iscateg = 'false'
            elif param_iscateg == 'b':
                param_iscateg = ''
            param_blank = input('Can the parameter be blank? (y, n)\n')
            assert param_blank in {'y', 'n'}
            param_multi = input('Can it take multiple values? (y, n)\n')
            assert param_multi in {'y', 'n'}
            properties['parameters'].append({
                'name'                     : param_name,
                'desc'                     : '',
                'type'                     : 'column',
                'columnValuesSQLType'      : param_sql_type,
                'columnValuesIsCategorical': param_iscateg,
                'value'                    : '',
                'valueNotBlank'            : False if param_blank == 'y' else True,
                'valueMultiple'            : True if param_multi == 'y' else False,
                'valueType'                : 'string'
            })
        elif column_choice == 'n':
            break
    formula_choice = input('Do you want to include a formula in the user inputs? (y, n)\n')
    assert formula_choice in {'y', 'n'}
    if formula_choice == 'y':
        properties['parameters'].extend(properties_formula)
    properties['parameters'].extend(properties_defaults)
    return properties


def mkdir_p(path):
    # Make dirs if non existent
    # Taken from https://stackoverflow.com/a/23794010
    try:
        os.makedirs(path)
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            raise


def safe_open_w(path):
    # Open "path" for writing, creating any parent directories as needed.
    mkdir_p(os.path.dirname(path))
    return open(path, 'w')


def to_camel_case(string):
    return ''.join([w.capitalize() for w in string.split('_')])


@task
def remove(c, name):
    dirname = os.path.join('output', name.upper())
    c.run("rm -r {dirname}".format(dirname=dirname))
    tests_dirname = os.path.join('output', 'tests_' + name.upper())
    if os.path.exists(tests_dirname):
        c.run("rm -r {dirname}".format(dirname=tests_dirname))
