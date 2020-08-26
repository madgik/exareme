import sys
import os
import traceback
sys.path.insert(1,'/root/exareme/lib/madis/src')
#sys.path.insert(1,'../exareme/exareme-tools/madis/src')

class MadisInstance:
  import madis
  
  def connectToDb(self,dbFilename):
    self.connection=self.madis.functions.Connection(dbFilename)
    self.cursor=self.connection.cursor()

#### not sure what this is for, but seems to be needed to deal with queries that contain the 'execnselect' udf...
    if dbFilename == '' or dbFilename == ':memory':
      self.madis.functions.variables.execdb = None
    else:
      self.madis.functions.variables.execdb = str(os.path.abspath(os.path.expandvars(os.path.expanduser(os.path.normcase(dbFilename)))))
####
    self.logger.debug("(MadisInstance::connectToDB) finished. Successfully connected to ->{}".format(dbFilename))


  def closeConnectionToDb(self):
    self.cursor.close()
    self.connection.close()
  
  def execute(self,query):
    self.logger.debug("(MadisInstance::execute) batch query={}".format(query))

    queries=query.split(';')
    
    results=""
    for q in queries:
      q=q+";"
      self.logger.debug("(MadisInstance::execute) executing line query={}".format(q))
      
      try:
        result=self.cursor.execute(q)
      except Exception:
        raise QueryExecutionException(traceback.format_exc())
        
      for row in result:
        results+="{}\n".format(row)
      
      self.logger.debug("(MadisInstance::execute) result: {}".format(result))

    self.logger.debug("(MadisInstance::execute) finished succesfully.")
    return results

  def __init__(self,logger):
    self.logger=logger


class QueryExecutionException(Exception):
  def __init__(self,message):
    super(QueryExecutionException,self).__init__(message)

#class ConnectionToDbException(Exception):
#  def __init__(self,message):
#    super(QueryExecutionException,self).__init__(message)


