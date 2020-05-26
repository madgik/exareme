import os
import re
import os.path
import re
#import tornado.httpserver
#import tornado.autoreload
import tornado.ioloop
import tornado.options
import tornado.web
import tornado.escape as escape
import random
import csv
import itertools
import email.utils
import json
import StringIO
from tornado.options import define, options
import copy
from collections import OrderedDict
import MadisInstance
import logging
WEB_SERVER_PORT=8888

FLOW_PATH=''

define("port", default=WEB_SERVER_PORT, help="run on the given port", type=int)


class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r"/", HomeHandler)
        ]
        settings = dict(
            xsrf_cookies=False,
        )

        tornado.web.Application.__init__(self, handlers, **settings)

class BaseHandler(tornado.web.RequestHandler):
    def __init__(self, *args):
        tornado.web.RequestHandler.__init__(self, *args)


class HomeHandler(BaseHandler):

    logger = logging.getLogger('MainHandler')
    hdlr = logging.FileHandler('/var/log/MadisServer.log','w+')
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    hdlr.setFormatter(formatter)
    logger.addHandler(hdlr)
    logger.setLevel(logging.DEBUG)

    access_log = logging.getLogger("tornado.access")
    app_log = logging.getLogger("tornado.application")
    gen_log = logging.getLogger("tornado.general")
    access_log.addHandler(hdlr)
    app_log.addHandler(hdlr)
    gen_log.addHandler(hdlr)
    
    NUMBER_OF_MADIS_INSTANCES=1
    madisInstances=[MadisInstance.MadisInstance(logger) for i in range(NUMBER_OF_MADIS_INSTANCES)]
    currentMadisInstanceIndex=0;
    
    def execQuery(self,dbFilename,query):
        madisInstance=self.madisInstances[self.currentMadisInstanceIndex]

        self.logger.debug("(MadisServer::execQuery) will call madisInstance.connectToDb({})".format(dbFilename))
        madisInstance.connectToDb(dbFilename)

        self.logger.debug("(MadisServer::execQuery) will call madisInstance.execute({})".format(query))
        result= madisInstance.execute(query)
    
        madisInstance.closeConnectionToDb()

        return result
        
    def post(self):
           
        dbFilename = self.get_argument("dbfilename")
        query = self.get_argument("query")
             
        self.logger.debug("(MadisServer::post) dbfilename={}  query={}".format(dbFilename,query))
        str_result = self.execQuery(dbFilename,query)
        self.logger.debug("(MadisServer::post) str_result-> {}".format(str_result))
        self.write("{}".format(str_result))
             
        self.finish() #is this needed??
        #return str_result

def main():
    sockets = tornado.netutil.bind_sockets(options.port)
    tornado.process.fork_processes(0)
    server = tornado.httpserver.HTTPServer(Application())
    server.add_sockets(sockets)
    tornado.ioloop.IOLoop.instance().start()
   

if __name__ == "__main__":
    main()
