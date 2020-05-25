import sys
import os
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
    self.logger.debug("(MadisInstance::connectToDB) finished")
   
  def closeConnectionToDb(self):
    self.cursor.close()
    self.connection.close()
  
  def execute(self,query):
    self.logger.debug("(MadisInstance::execute) query={}".format(query))

    queries=query.split(';')
    
    results=""
    for q in queries:
      q=q+";"
      self.logger.debug("(MadisInstance::execute) in for. q={}".format(q))
  
      result=self.cursor.execute(q)

      for row in result:
        results+="{}\n".format(row)
      self.logger.debug("(MadisInstance::execute) result: {}".format(result))

    self.logger.debug("(MadisInstance::execute) finished")
    return results

  def __init__(self,logger):
    self.logger=logger


