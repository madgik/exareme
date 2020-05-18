import tornado.web
from tornado import gen 
from concurrent.futures import ThreadPoolExecutor
from tornado.concurrent import run_on_executor
from tornado.log import enable_pretty_logging

import logging

import MadisInstance

MAX_WORKERS=2
NUMBER_OF_MADIS_INSTANCES=1


class MainHandler(tornado.web.RequestHandler):
  #logging stuff..
  enable_pretty_logging()
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
  ###

  executor=ThreadPoolExecutor(max_workers=MAX_WORKERS)

  madisInstances=[MadisInstance.MadisInstance(logger) for i in range(NUMBER_OF_MADIS_INSTANCES)]
  currentMadisInstanceIndex=0; 
  
  @run_on_executor
  def execQuery(self,dbFilename,query):
    madisInstance=self.madisInstances[self.currentMadisInstanceIndex]
    #self.currentMadisInstanceIndex+=1

    self.logger.debug("(MadisServer::execQuery) will call madisInstance.connectToDb({})".format(dbFilename))
    madisInstance.connectToDb(dbFilename)

    self.logger.debug("(MadisServer::execQuery) will call madisInstance.execute({})".format(query))
    result= madisInstance.execute(query)
    
    madisInstance.closeConnectionToDb()

    return result

  @tornado.gen.coroutine
  def post(self):
    
    dbFilename=self.get_argument("dbfilename")
    query=self.get_argument("query")
    
    self.logger.debug("(MadisServer::post) dbfilename={}  query={}".format(dbFilename,query))
    str_result=yield self.execQuery(dbFilename,query)

    self.logger.debug("(MadisServer::post) str_result-> {}".format(str_result))
    self.write("{}".format(str_result))
    
    #self.finish() #is this needed??

application=tornado.web.Application([(r"/", MainHandler),])
application.listen(8888)
tornado.ioloop.IOLoop.instance().start()
