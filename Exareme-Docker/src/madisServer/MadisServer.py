import tornado.web
from tornado import gen 
from tornado.log import enable_pretty_logging
from tornado.options import define, options
import logging
import os

PROCESSES_PER_CPU = 2
WEB_SERVER_PORT=8888
define("port", default=WEB_SERVER_PORT, help="run on the given port", type=int)

import MadisInstance
from MadisInstance import QueryExecutionException

class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r"/", MainHandler)
        ]
        tornado.web.Application.__init__(self, handlers)

class BaseHandler(tornado.web.RequestHandler):
    def __init__(self, *args):
        tornado.web.RequestHandler.__init__(self, *args)


class MainHandler(BaseHandler):
  #logging stuff..
  enable_pretty_logging()
  logger = logging.getLogger('MainHandler')
  hdlr = logging.FileHandler('/var/log/MadisServer.log','w+')
  formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
  hdlr.setFormatter(formatter)
  logger.addHandler(hdlr)
  if os.environ['LOG_LEVEL'] == "DEBUG":
    logger.setLevel(logging.DEBUG)
  else:
    logger.setLevel(logging.INFO)

  access_log = logging.getLogger("tornado.access")
  app_log = logging.getLogger("tornado.application")
  gen_log = logging.getLogger("tornado.general")
  access_log.addHandler(hdlr)
  app_log.addHandler(hdlr)
  gen_log.addHandler(hdlr)  
  madisInstance=MadisInstance.MadisInstance(logger)
  
  def execQuery(self,dbFilename,query):

    self.logger.debug("(MadisServer::execQuery) will call madisInstance.connectToDb({})".format(dbFilename))
    self.madisInstance.connectToDb(dbFilename)

    try:
      self.logger.debug("(MadisServer::execQuery) will call madisInstance.execute({})".format(query))
      result= self.madisInstance.execute(query)
    finally:
      self.madisInstance.closeConnectionToDb()

    return result

  @tornado.gen.coroutine
  def post(self):
    
    dbFilename=self.get_argument("dbfilename")
    query=self.get_argument("query")
    
    self.logger.debug("(MadisServer::post) dbfilename={}  query={}".format(dbFilename,query))
   
    try:
      str_result=self.execQuery(dbFilename,query)
    except QueryExecutionException as e:
      #raise tornado.web.HTTPError(status_code=500,log_message="...the log message??")
      self.logger.error("(MadisServer::post) QueryExecutionException: {}".format(str(e)))
      #print "QueryExecutionException ->{}".format(str(e))
      self.set_status(500)
      self.write(str(e))
      self.finish()
      return
    
    self.logger.debug("(MadisServer::post) str_result-> {}".format(str_result))
    self.write("{}".format(str_result))
    
    self.finish()

def main():
    sockets = tornado.netutil.bind_sockets(options.port)
    tornado.process.fork_processes(tornado.process.cpu_count() * PROCESSES_PER_CPU)
    server = tornado.httpserver.HTTPServer(Application())
    server.add_sockets(sockets)
    tornado.ioloop.IOLoop.instance().start()


if __name__ == "__main__":
    main()

