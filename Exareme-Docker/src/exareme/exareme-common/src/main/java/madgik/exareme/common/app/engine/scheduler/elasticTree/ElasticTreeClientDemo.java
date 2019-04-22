///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.common.engine.elasticTree;
//
//import madgik.exareme.master.app.client.AdpDBClient;
//import madgik.exareme.master.app.client.AdpDBClientFactory;
//import madgik.exareme.master.app.client.AdpDBClientProperties;
//import madgik.exareme.master.app.engine.AdpDBManager;
//import madgik.exareme.master.app.engine.AdpDBManagerFactory;
//import madgik.exareme.master.app.client.SLA;
//import madgik.exareme.common.engine.elasticTree.logger.ElasticTreeLogger;
//import madgik.exareme.utils.file.FileUtil;
//import madgik.exareme.utils.units.Metrics;
//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//
//import java.io.File;
//
///**
// *
// * @author heraldkllapi
// */
//public class ElasticTreeClientDemo {
//
//  public static void main(String[] args) throws Exception {
//    BasicConfigurator.configure();
//    ElasticTreeLogger.init();
//    if (args.length == 4) {
//      Logger.getRootLogger().setLevel(Level.INFO);
//    } else {
//      Logger.getRootLogger().setLevel(Level.DEBUG);
//    }
//    long delay_sec = Long.parseLong(args[3]);
//    AdpDBManager manager = AdpDBManagerFactory.createRmiManager("127.0.0.1");
//    AdpDBClient client = AdpDBClientFactory.createDBClient(manager);
//    client.startElasticTreeMonitor();
//    AdpDBClientProperties props = new AdpDBClientProperties();
//    props.database = args[0];
//    props.elasticTree = true;
//    props.explain = true;
//    client.setUp(props);
//
//    String query = FileUtil.readFile(new File(args[1]));
//
//    for (int i = 0; i < Integer.parseInt(args[2]); ++i) {
//      SLA sla = TreeConstants.SETTINGS.SLAS[TreeConstants.SETTINGS.NORMAL_SLA];
//      AdpDBQueryResultStatus status = client.queryElasticTree(query, sla);
//      TreeQuery queryTree = new TreeQuery(status.getId(), sla, query);
//      ElasticTreeLogger.registerQuery(queryTree);
//      ElasticTreeLogger.queryIssued(status.getId(), queryTree);
//      Thread.sleep(delay_sec * Metrics.MiliSec);
//    }
//    // Wait for termination ...
//
////    manager.stopManager();
////    System.exit(0);
//    ElasticTreeLogger.close();
//  }
//}
