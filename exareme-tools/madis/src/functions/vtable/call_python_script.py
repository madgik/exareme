"""

.. function:: pipe(query:None[,lines:t])

Executes *query* as a shell command and returns the standard output lines as rows of one column table.
Setting *lines* parameter to *f* the command output will be returned in one table row.

:Returned table schema:
    - *output* text
        Output of shell command execution

Examples::

.. doctest::

    >>> sql("pipe 'ls ./testing/*.csv' ")
    C1
    ---------------------
    ./testing/colpref.csv

    >>> sql("pipe wc ./testing/colpref.csv")
    C1
    ---------------------------------
     19  20 463 ./testing/colpref.csv

.. doctest::
    :hide:

    >>> sql("pipe wc nonexistingfile") #doctest:+ELLIPSIS +NORMALIZE_WHITESPACE
    Traceback (most recent call last):
    ...
    OperatorError: Madis SQLError:
    Operator PIPE: Command 'wc nonexistingfile' failed to execute because:
    wc: nonexistingfile: No such file or directory
"""

import functions
import subprocess

import vtbase

registered = True
#external_stream = True


class CallPythonScriptVT(vtbase.VT):
    def VTiter(self, *parsedArgs, **envars):
        largs, dictargs = self.full_parse(parsedArgs)

        # Execute the query
        if 'query' in dictargs:
            query = dictargs['query']
            cur = envars['db'].cursor()
            c = cur.execute(query)

        command = None
        if len(largs) > 0:
            command = largs[-1]
        if command is None:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No command argument found")

        yield [('results', 'text')]

        child = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        output, error = child.communicate()
        yield [output]

        if child.returncode != 0:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "Command '%s' failed to execute because:\n%s" % (
                command, error.rstrip('\n\t ')))


def Source():
    return vtbase.VTGenerator(CallPythonScriptVT)


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    from functions import *

    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest

        doctest.testmod()
