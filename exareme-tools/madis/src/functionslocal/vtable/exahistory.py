import json
import urllib
import urllib2

import functions

registered = True
external_query = True

# default properties
dict_default = {
    'uri': 'http://localhost:9090/table/',
    # 'db'   : '/database/demo/',   # database path.
    # 'query' : 'select * from emp' # target query script.
}


class ExaHistory(functions.vtable.vtbase.VT):
    # TODO split client & server properties
    __properties__ = ('uri', 'db', 'query')

    def VTiter(self, *parsedArgs, **envars):

        # get site properties, list_site not used!
        list_site, dict_site = self.full_parse(parsedArgs)

        # get global properties
        dict_global = functions.variables.__dict__

        # combine properties
        for key in self.__properties__:
            if key in dict_site:
                setattr(self, key, dict_site[key])
            elif key in dict_global:
                setattr(self, key, dict_global[key])
            elif key in dict_default:
                setattr(self, key, dict_default[key])
            else:
                raise functions.OperatorError(__name__.rsplit('.')[-1],
                                              "Please provide %s property." % key)
                # print "Property : ", key, " = ", getattr(self, key)

        # set http request
        self.query += ';\n'
        data = urllib.urlencode(self.__dict__)

        # get http response
        try:
            response = urllib2.urlopen(url=self.uri, data=data, timeout=150)
            # schema
            desc = next(response)
            description = json.loads(desc)
            print description
            if description['errors'] == "":
                # description
                yield description['schema']

                # rows
                for line in response:
                    # print line
                    yield json.loads(line)
            else:
                yield "{schema:[[error, null]]}"
                yield description['errors']

        except (urllib2.URLError, urllib2.HTTPError, IOError) as e:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "%s." % e)


def Source():
    return functions.vtable.vtbase.VTGenerator(ExaHistory)


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    from functions import *

    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest

        doctest.testmod()
