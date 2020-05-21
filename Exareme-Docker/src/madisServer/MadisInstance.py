import sys
sys.path.insert(1,'/root/exareme/lib/madis/src')
#sys.path.insert(1,'/home/sskoull/Desktop/AthinaInstitute/tornadoServer/madis')

class MadisInstance:
  import madis
  
  def connectToDb(self,dbFilename):
    self.connection=self.madis.functions.Connection(dbFilename)
    self.cursor=self.connection.cursor()
    
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


