import setpath
import functions
import json
import re
import itertools
registered=True


class readmetadatafile (functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'filename' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No filename argument ")
        filename = str(dictargs['filename'])

        with open(filename) as data_file:
            metadata = json.load(data_file)

        yield (['code'],['sqltype'],['categorical'],['enumerations'],['minValue'],['maxValue'])
        for variable in metadata['variables']:
            keys = [str(x) for x in variable.keys()]
            if 'code' in keys:
                code =str(variable['code'])
            else: code = None

            if 'type' in keys:
                type =str(variable['type'])
            else: type = None

            if 'sql_type' in keys:
                sqltype =str(variable['sql_type'])
            else: sqltype = None

            if 'categorical' in keys:
                categorical = str(variable['categorical'])
            else: categorical = None

            if 'enumerations' in keys:
                enumerations = ','.join([str(x['code']) for x in variable['enumerations']])
            else: enumerations = None

            if 'minValue' in keys:
                minValue = int(variable['minValue'])
            else: minValue = None
            if 'maxValue' in keys:
                maxValue = int(variable['maxValue'])
            else: maxValue = None

            yield code,sqltype,categorical,enumerations,minValue,maxValue




def Source():
    return functions.vtable.vtbase.VTGenerator(readmetadatafile)


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    import setpath
    from functions import *
    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest
        doctest.tes