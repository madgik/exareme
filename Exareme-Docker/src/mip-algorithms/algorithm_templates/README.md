## Creating algorithm templates

### Requirements
You will need `python>=3.6`.

You will also need `invoke`.
```bash
pip3 install invoke
```
### Basic commands
First, you can see all tasks by typing
```bash
$ invoke --list
Available tasks:

  create
  remove

```

Use `create` to build the folders for your next algorithm.
To obtain help on this tast just type
```console
$ invoke --help create
Usage: inv[oke] [--core-opts] create [--options] [other tasks here ...]

Docstring:
  none

Options:
  -a STRING, --alg-type=STRING   Type of algorithm to create. (local-global, multi-local-global, iterative)
  -n STRING, --name=STRING       Name of the algorithm to create.

```

The `name` parameter is just the algorithm's name (uppercase or lowercase) and
the `alg-type` is the algorithm's type. This can be `local-global`, `multi-local-global` or
`iterative`.

### An example

Let's create for example an `iterative` algorithm named `SGD`
```bash
invoke create --name=sgd --alg-type=iterative
```
Then, you will be prompted by a series of questions concerning the algorithm parameters, 
necessary for building the `properties.json` file.

The process is now over and you can find in the `output` folder a template for your
algorithm with the correct folder structure
```console
SGD
├── init
│   ├── 1
│   │   ├── __init__.py
│   │   ├── global.py
│   │   └── local.py
│   └── __init__.py
├── step
│   ├── 1
│   │   ├── __init__.py
│   │   ├── global.py
│   │   └── local.py
│   └── __init__.py
└── termination_condition
    ├── __init__.py
    └── global.py
├── finalize
│   ├── 1
│   │   ├── __init__.py
│   │   ├── global.py
│   │   └── local.py
│   └── __init__.py
├── __init__.py
├── sgd_lib.py
├── properties.json
```
Everything is now in place and **you only have to edit the `sgd_lib.py` file** where 
all the algorithm methods are found.

If something went wrong and you want to remove the algorithm folder just type
```bash
invoke remove sgd
```
