## Creating algorithm templates

### Requirements
You will need `python>=3.6`.

You will also need `invoke`.
```bash
pip3 install invoke
```
### Basic commands
First, you can see all tasks by typing
```console
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
  -a STRING, --alg-type=STRING     Type of algorithm to create. (local-global, multi-local-global, iterative)
  -e [STRING], --extras[=STRING]
  -n STRING, --name=STRING         Name of the algorithm to create.

```

The `name` parameter is just the algorithm's name (uppercase or lowercase, separate words with underscores) and
the `alg-type` is the algorithm's type. This can be `local-global`, `multi-local-global` or
`iterative`. The `extras` parameter is optional, is `True` by default and it's used to generate extras (see below).

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

If you want to restart no need to delete the generated folders. The scripts will 
take care of that.
If you want to manually remove the folders, just type
```bash
invoke remove sgd
```

### Extras
Along with the algorithm folder a second folder is created called `<ALGNAME>_extras`.
In there you will find a template for your unittests called `test_<ALGNAME>.py`. There is
no need to edit this file as it reads the input from `expected_<ALGNAME>.json`. This is the 
file you should edit. It has the following structure
```json
{
    "result": [
        {
            "input": [
                {
                    "name" : ...,
                    "value": ...    
                },
                ...
            ],
            "output": [
                {
                    "name" : ...,
                    "value": ...    
                },
                ...
            ]
        }
    ]
}
```
You create a list of many `{input, output}` pairs with input parameters for the algorithm 
and the output produced by some standard library (`scipy`, `sklearn`, ...). 

Then `test_<ALGNAME>.py` can be executed by typing `python -m test_<ALGNAME>.py` or
simply `pytest` to execute all unittests in a folder.

Finally, one more file is created to help you debug your algorithm. 
It's called `exec_<ALGNAME>.py`. Use this file to run your algorithm
on your local machine without the need to deploy `Exareme`. 
