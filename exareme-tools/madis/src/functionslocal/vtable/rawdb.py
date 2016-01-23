import json
import urllib2
import base64

import functions

registered = True
external_query = True

# default properties
default_dict = {
    'host': 'localhost',
    'port' : '54321',
    'api': 'query',
    'resultsPerPage' : 'all',   # if #results provided then uses pagination
    'username' : None,          # required
    'password' : None,          # required
    'query' : None              # required
}

class RAWDB(functions.vtable.vtbase.VT):

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

        if (self.resultsPerPage == "all"):
            # set http request
            url="http://{0}:{1}/{2}".format(self.host, self.port, self.api)
            data =  json.dumps({ 'query' : self.query.encode("utf-8")})
            base64string = base64.encodestring('%s:%s' % (self.username, self.password)).replace('\n', '')

            # get http response
            try:
                r = urllib2.Request(url=url,data=data)
                r.add_header("Authorization", "Basic %s" % base64string)
                r.add_header("Content-Type", "application/json")
                response = urllib2.urlopen(r, timeout=150)
                records = json.load(response)['output']
                yield [(k,type(v).__name__) for k,v in records[0].iteritems()]

                for r in records:
                    yield r.values()

            except (urllib2.URLError, urllib2.HTTPError) as e:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "%s." % e)
        else:
            # set http request
            url="http://{0}:{1}/{2}".format(self.host, self.port, "query-start")
            data =  json.dumps({ 'query' : self.query.encode("utf-8"), 'resultsPerPage' : self.resultsPerPage})
            base64string = base64.encodestring('%s:%s' % (self.username, self.password)).replace('\n', '')

            # get http response
            try:
                r = urllib2.Request(url=url,data=data)
                r.add_header("Authorization", "Basic %s" % base64string)
                r.add_header("Content-Type", "application/json")
                response = urllib2.urlopen(r, timeout=150)
                jsonresponse = json.load(response)
                records = jsonresponse['data']
                yield [(k,type(v).__name__) for k,v in records[0].iteritems()]

                for r in records:
                    yield r.values()

                while (jsonresponse['hasMore']):
                    token = jsonresponse['token']
                    url="http://{0}:{1}/{2}".format(self.host, self.port, "query-next")
                    data =  json.dumps({ 'token' : token})

                    r = urllib2.Request(url=url,data=data)
                    r.add_header("Authorization", "Basic %s" % base64string)
                    r.add_header("Content-Type", "application/json")
                    response = urllib2.urlopen(r, timeout=150)

                    jsonresponse = json.load(response)
                    for r in records:
                        yield r.values()

            except (urllib2.URLError, urllib2.HTTPError) as e:
                raise functions.OperatorError(__name__.rsplit('.')[-1], "%s." % e)

def Source():
    return functions.vtable.vtbase.VTGenerator(RAWDB)


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
