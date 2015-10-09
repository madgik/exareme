"""
.. function:: exaquery(host, port, db, query:None)

:host: remote ip address,
:port: listening port
:db: remote db
:query: DataFlow Language

"""

import functions
import json
import urllib
import urllib2

registered = True
external_query = True


default_dict = {
    'host': 'localhost',
    'port': '9090',
    'api': 'query/',
    'db': None,  # required
    'query': None  # required
}


class ExaQuery(functions.vtable.vtbase.VT):
    engine_status = False

    def VTiter(self, *parsedArgs, **envars):

        # get site properties
        site_list, site_dict = self.full_parse(parsedArgs)

        # get global properties
        global_dict = functions.variables.__dict__

        # combine default, global and site properties
        for key, value in default_dict.iteritems():
            if key in site_dict:
                setattr(self, key, site_dict[key])
            elif key in global_dict:
                setattr(self, key, global_dict[key])
            elif value is not None:
                setattr(self, key, default_dict[key])
            else:
                raise functions.OperatorError(__name__.rsplit('.')[-1],
                                              "Provide %s property." % key)


        # set http request
        url = "http://{0}:{1}/{2}".format(self.host, self.port, self.api)
        self.query += ';\n'
        data = urllib.urlencode(self.__dict__)

        # get http response
        try:
            response = urllib2.urlopen(url=url, data=data, timeout=150)
            # schema
            description = next(response)
            yield json.loads(description)['schema']
            # rows
            for line in response:
                # print line
                yield json.loads(line)
        except (urllib2.URLError, urllib2.HTTPError, IOError) as e:
            raise functions.OperatorError(__name__.rsplit('.')[-1], "%s." % e)


def Source():
    return functions.vtable.vtbase.VTGenerator(ExaQuery)


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
