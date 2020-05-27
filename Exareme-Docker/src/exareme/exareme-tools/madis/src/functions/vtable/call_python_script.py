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

def rchop(s, suffix):
    if suffix and s.endswith(suffix):
        return s[:-len(suffix)]
    return s

from contextlib import contextmanager

@contextmanager
def stdout_redirector(stream):
    old_stdout = sys.stdout
    sys.stdout = stream
    try:
        yield
    finally:
        sys.stdout = old_stdout


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
            command.replace("\\\"","\"")
        if command is None:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "No command argument found")


        yield [('data', 'text')]
        if ('HEALTH' not in command) and ('LIST_VARIABLES' not in command) and ('LIST_DATASETS' not in command):
        #if ('DESCRIPTIVE_STATS' in command) or ('LOGISTIC_REGRESSION' in command):
            command = re.sub('\s+', ' ', command)
            arguments = command.split()
            get_import = re.sub('\.py$','',(re.sub('/','.',re.search("mip-algorithms/(.+)",arguments[1]).groups()[0])))
            mpackage = re.search("(^[^.]*)(.+)",get_import).group(1)
            sys.path.append("/root/mip-algorithms/" + mpackage)
            myimport = re.search("(^[^.]*)(.+)",get_import).group(2)[1:]
            algo = importlib.import_module(myimport)
            sys.path.remove("/root/mip-algorithms/" + mpackage)
            for i in xrange(len(arguments)):
                arguments[i] = re.sub('\"','',arguments[i])
            f = StringIO()
            with stdout_redirector(f):
                algo.main(arguments)
            yield [f.getvalue()]
        else:
            child = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

            output, error = child.communicate()
            print "call pythn script output --> "+output
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
