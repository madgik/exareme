///**
//* Copyright MaDgIK Group 2010 - 2015.
//*/
//package madgik.exareme.common.engine.elasticTree.experiments;
//
//import madgik.exareme.master.app.client.AdpDBClient;
//import madgik.exareme.master.app.client.AdpDBClientFactory;
//
//import madgik.exareme.master.app.client.AdpDBClientProperties;
//import madgik.exareme.master.app.client.AdpDBQueryResultStatus;
//import madgik.exareme.master.app.engine.AdpDBManager;
//import madgik.exareme.master.app.engine.AdpDBManagerFactory;
//import madgik.exareme.common.engine.elasticTree.TreeConstants;
//import madgik.exareme.common.engine.elasticTree.TreeQuery;
//import madgik.exareme.common.engine.elasticTree.generator.DataflowGenerator;
//import madgik.exareme.common.engine.elasticTree.generator.TreeDataflowGenerator;
//import madgik.exareme.common.engine.elasticTree.generator.TreeDataflowGeneratorPhase;
//import madgik.exareme.common.engine.elasticTree.generator.TreeDataflowMixedGenerator;
//import madgik.exareme.common.engine.elasticTree.logger.ElasticTreeLogger;
//import madgik.exareme.common.engine.elasticTree.system.GlobalTime;
//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//
//import java.io.File;
//import java.io.IOException;
//
///**
//*
//* @author heraldkllapi
//*/
//public class ElasticTreeClientMain {
//
//  public static void main(String[] args) throws Exception {
//    Logger.getRootLogger().setLevel(Level.INFO);
//    BasicConfigurator.configure();
//
//    // Input params
//    String database = args[0];
//    File q1 = new File(args[1]);
//    File q2 = new File(args[2]);
//    File q3 = new File(args[3]);
//    double totalTime_sec = Double.parseDouble(args[4]);
//    double meanQueryTime_sec = Double.parseDouble(args[5]);
//    String genName = args[6];
//    int exp = Integer.parseInt(args[7]);
//    boolean run = Boolean.parseBoolean(args[8]);
//
//    TreeConstants.SETTINGS.TOTAL_TIME_SEC = totalTime_sec;
//    TreeConstants.SETTINGS.QUERY_MEAN_ARRIVAL_TIME_SEC = meanQueryTime_sec;
//    TreeConstants.SETTINGS.PHASE_TRANSITION_TIME = totalTime_sec / 3;
//
//    System.out.println("TOTAL TIME: " + TreeConstants.SETTINGS.TOTAL_TIME_SEC);
//    System.out.println("MEAN QUERY TIME: " + TreeConstants.SETTINGS.QUERY_MEAN_ARRIVAL_TIME_SEC);
//
//    ElasticTreeLogger.init();
//
//    DataflowGenerator generator = null;
//    if (genName.equalsIgnoreCase("simple")) {
//      generator = createDataflowGen(q1);
//      System.out.println("Normal generator");
//    } else if (genName.equalsIgnoreCase("phase")) {
//      generator = createPhaseDataflowGen(q1, q2, exp);
//      System.out.println("Phase generator");
//    } else if (genName.equalsIgnoreCase("mixed")) {
//      generator = createMixedDataflowGen(q1, q2, q3);
//      System.out.println("Mixed generator");
//    } else {
//      throw new Exception("Generator not known!");
//    }
//
//    // Create db client
//    AdpDBManager manager = AdpDBManagerFactory.createRmiManager("127.0.0.1");
//    AdpDBClient client = AdpDBClientFactory.createDBClient(manager);
//    AdpDBClientProperties props = new AdpDBClientProperties(database);
//    props.elasticTree = true;
//    props.explain = true;
//    client.setUp(props);
//
//    if (run) {
//      client.startElasticTreeMonitor();
//    }
//
//    while (GlobalTime.getCurrentSec() < TreeConstants.SETTINGS.TOTAL_TIME_SEC) {
//      // Generate next dataflow
//      TreeQuery query = generator.getNextDataflow();
//      // Issue
//      System.out.println(GlobalTime.getCurrentSec() +
//                         " > NEW QUERY: " + query.id +
//                         " SLA: " + query.getSLA().getId() +
//                         " QUERY: " + query.getQuery().hashCode());
//      if (run) {
//        AdpDBQueryResultStatus status = client.queryElasticTree(query.getQuery(), query.getSLA());
//        ElasticTreeLogger.registerQuery(query);
//        ElasticTreeLogger.queryIssued(status.getId(), query);
//      }
//    }
//
//    System.out.println("Terminating ...");
//
//    // Force quit
//    System.exit(0);
//  }
//
//  private static DataflowGenerator createDataflowGen(File q1) throws IOException {
//    return new TreeDataflowGenerator(q1);
//  }
//
//  private static DataflowGenerator createPhaseDataflowGen(
//      File q1, File q2, int exp) throws IOException {
//    return new TreeDataflowGeneratorPhase(q1, q2, exp);
//  }
//
//  private static DataflowGenerator createMixedDataflowGen(
//      File q1, File q2, File q3) throws IOException {
//    return new TreeDataflowMixedGenerator(new File[]{q1, q2, q3});
//  }
//}
