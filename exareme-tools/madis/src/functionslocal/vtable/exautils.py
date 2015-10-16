import logging
import urllib
import urllib2


def check_engine(host='localhost', port='9090'):
    status = False
    response = None

    try:
        url = 'http://{0}:{1}'.format (host, port)
        request = urllib2.Request(url=url)
        response = urllib2.urlopen(request, timeout=1)
        logging.debug("Response code : " + str(response.code))

        if response.code is 200: status = True
        else : status = False

    except urllib2.URLError, e:
        if hasattr(e, 'reason'):
            logging.debug('Reason: %s' % str(e.reason))
        elif hasattr(e, 'code'):
            logging.debug('Error code: %s' % str(e.code))
        status = False
    finally:
        if response is not None:
            response.close()
    return status

def exa_request(host, port, api, **kwargs):
    '''
     e.g. exa_request("localhost", 9090, "/query", db="", query="")
          exa_request("localhost", 9090, "/result", db="", table="")
          exa_request("localhost", 9090, "/history", db="", table="")
    '''
    # header
    url = 'http://{0}:{1}'.format (host, port)
    headers = dict()

    # body
    data = dict()
    for key, value in kwargs:
        data[key] = value
    body = urllib.urlencode(data)

    # request
    return urllib2.Request(url, body, headers)

def get_response(request):
    return urllib2.urlopen(request)

default_dict = {
    'host': 'localhost',
    'port' : '9090',
    'db' : None,    # required
    'query' : None  # required
}

class ExaError(Exception):
    pass

def combine_properties(site_list, site_dict, global_dict):

    # copy default properties
    properties = dict(default_dict)

    # combine site list and dict properties
    for key in site_list:
        site_dict[key] = True

    # combine default, global and site properties
    for key, value in default_dict.iteritems():
        if key in site_dict:
            properties[key] = site_dict[key]
        elif key in global_dict:
            properties[key] = global_dict[key]
        elif value is not None:
            properties[key] = default_dict[key]
        else:
            raise ExaError("Please provide %s property." % key)
    return properties

def query(db=None, query=None):
    if db is None or query is None:
        raise ExaError("Please provide db, query.")
    yield [('status', ),]
    yield ["testing"]

def result(db, table):
    result = {'schema' : [['eid','int'], ['ename', 'text'], ['age', 'int'], ['salary','real']],
              'errors': 'null'}
    yield result['schema']             # schema
    yield [0, "John", 40, 40000.0]     # row

def stats(db, table):
    yield [('status', 'text')]  # schema
    yield [""]                  # row


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
# if self.flow is True:
#     connection=envars['db']
#     cursor=connection.cursor()
#     dfl_queries=str()
#     for row in  cursor.execute("flow " + self.query):
#         dfl_queries+=str(row[0])
#         dfl_queries+="\n"
#     self.query=dfl_queries
#     print "**Warn <> DFL queries \n%s" % self.query
# else:
#     self.query += ';\n'
#
#
# if Exa.engine_status is False:
#     print "**Warn <> Engine is up?"
#     engine_status = exautils.check_engine()
#     if engine_status is False:
#         raise functions.OperatorError(__name__.rsplit('.')[-1],
#                                       "Engine is stopped.")
#         print "**Warn <> Engine status : OK"
#
# # set http request
# data = urllib.urlencode(self.__dict__)
# print "**Warn <> request data\n%s" % str(data)
# # get http response
# try:
#     response = urllib2.urlopen(url=self.url, data=data, timeout=10000)
#     # schema
#     description = next(response)
#     # print description
#     yield json.loads(description)['schema']
#     # rows
#     for line in response:
#         # print line
#         yield json.loads(line)
# except (urllib2.URLError, urllib2.HTTPError, IOError) as e:
#     raise functions.OperatorError(__name__.rsplit('.')[-1], "%s." % e)
