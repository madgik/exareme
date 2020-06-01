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
import importlib
import vtbase
import sys
registered = True
#external_stream = True
import re
from cStringIO import StringIO

from contextlib import contextmanager

@contextmanager
def stdout_redirector(stream):
    old_stdout = sys.stdout
    sys.stdout = stream
    try:
        yield
    finally:
        sys.stdout = old_stdout

def rchop(s, suffix):
    if suffix and s.endswith(suffix):
        return s[:-len(suffix)]
    return s

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
            command = rchop(largs[0],'.py')
            command = re.sub('/+','/',command)
        if command is None:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No command argument found")

        yield [('data', 'text')]
        command = command.split("/")
        mpackage = ""
        myimport = ""
        for i,directory in enumerate(command):
            if directory == "mip-algorithms":
                mpackage = command[i+1]
                myimport = '.'+'.'.join(command[i+2:])

        parentpackage = importlib.import_module(mpackage)
        algo = importlib.import_module(myimport,mpackage)

        f = StringIO()
        with stdout_redirector(f):
            algo.main(largs)
        yield [f.getvalue()]

        """
            // Old way, by opening subprocess
            command = "python " + ' '.join(largs)
            child = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

            output, error = child.communicate()
            print "call pythn script output --> "+output
            yield [output]

            if child.returncode != 0:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "Command '%s' failed to execute because:\n%s" % (
                    command, error.rstrip('\n\t ')))
        """

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
