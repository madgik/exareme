package madgik.exareme.master.engine;//package madgik.exareme.master.engine;
//
//
//import madgik.exareme.master.client.AdpDBClient;
//import madgik.exareme.master.client.AdpDBClientProperties;
//import madgik.exareme.master.client.AdpDBClientQueryStatus;
//import madgik.exareme.master.client.TestAdpDBClient;
//import madgik.exareme.master.app.cluster.ExaremeCluster;
//import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.Charset;
//import java.rmi.RemoteException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by vagos on 3/27/15.
// */
//
//public class StressEngineClientTest {
//  private static final Logger log = Logger.getLogger(StressEngineClientTest.class);
//  private int registryPort = 1099;
//  private int dtPort = 8088;
//  private int nclients = 1;
//  private int nworkers = 5;
//  private String dbPathName;
//  private String[] load_script;
//  private String[] query_script;
//
//  private class RunnableAdpDBClient implements Runnable {
//    private final int id;
//    private AdpDBClient client;
//
//
//    public RunnableAdpDBClient(final int id, final AdpDBClient client) {
//      this.id = id;
//      this.client = client;
//    }
//
//    public void run() {
//      try {
//        AdpDBClientQueryStatus queryStatus =
//            client.query("load_" + String.valueOf(id), load_script[id]);
//        if (queryStatus.hasError()) {
//          log.error("Exception occured..." + queryStatus.getError());
//        }
//        assertTrue(queryStatus != null);
//        assertFalse(queryStatus.hasError());
//
//        log.debug("Explain query script...");
//        String explain = client.explain(query_script[id]);
//        log.debug("Query plan :\n" + explain);
//        assertNotNull(explain);
//
//        queryStatus = client.query("query_" + String.valueOf(id), query_script[id]);
//        if (queryStatus.hasError()) {
//          log.error("Exception occured..." + queryStatus.getError());
//        }
//        assertTrue(queryStatus != null);
//        assertFalse(queryStatus.hasError());
//
//        InputStream inputStream = client.readTable("emp_res_" + String.valueOf(id));
//        log.info(IOUtils.toString(inputStream, Charset.defaultCharset()));
//
//      } catch (RemoteException e) {
//        log.error("Error occurred ( " + String.valueOf(id) + ")!", e);
//      } catch (IOException e) {
//        log.error("Error occurred while reading results", e);
//      }
//    }
//  }
//
//
//  private class RunnableAdpDBInsertClient implements Runnable {
//    private final int id;
//    private AdpDBClient client;
//
//    public RunnableAdpDBInsertClient(final int id, final AdpDBClient client) {
//      this.id = id;
//      this.client = client;
//    }
//
//    public void run() {
//      try {
//        AdpDBClientQueryStatus queryStatus =
//            client.query("load_" + String.valueOf(id), load_script[id]);
//        if (queryStatus.hasError()) {
//          log.error("Exception occured..." + queryStatus.getError());
//        }
//        assertTrue(queryStatus != null);
//        assertFalse(queryStatus.hasError());
//
//
////        InputStream inputStream = client.readTable("emp_" + String.valueOf(id));
////        log.info(IOUtils.toString(inputStream, Charset.defaultCharset()));
//
//      } catch (RemoteException e) {
//        log.error("Error occurred ( " + String.valueOf(id) + ")!", e);
//      } catch (IOException e) {
//        log.error("Error occurred while reading results", e);
//      }
//    }
//  }
//
//  private class RunnableAdpDBRepartitionClient implements Runnable {
//    private final int id;
//    private AdpDBClient client;
//
//
//    public RunnableAdpDBRepartitionClient(final int id, final AdpDBClient client) {
//      this.id = id;
//      this.client = client;
//    }
//
//    public void run() {
//      try {
//
//        AdpDBClientQueryStatus queryStatus = client.query("query_" + String.valueOf(id), query_script[id]);
//        if (queryStatus.hasError()) {
//          log.error("Exception occured..." + queryStatus.getError());
//        }
//        assertTrue(queryStatus != null);
//        assertFalse(queryStatus.hasError());
//
////        InputStream inputStream = client.readTable("emp_res_" + String.valueOf(id));
////        log.info(IOUtils.toString(inputStream, Charset.defaultCharset()));
//
//      } catch (RemoteException e) {
//        log.error("Error occurred ( " + String.valueOf(id) + ")!", e);
//      } catch (IOException e) {
//        log.error("Error occurred while reading results", e);
//      }
//    }
//  }
//
//
//  @Before
//  public void setUp() throws Exception {
//    Logger.getRootLogger().setLevel(Level.ALL);
//    log.debug("---- SETUP ----");
//
//    // load & format scripts
//    File loadFile =
//        new File(TestAdpDBClient.class.getResource("load_emp_template.sql").getFile());
//    load_script = new String[1];
//    query_script = new String[nclients];
//    this.load_script[0] = String.format(
//        FileUtils.readFileToString(loadFile),
//        "emp",
//        String.valueOf(nworkers + 1),
//        loadFile.getParentFile().getAbsolutePath() + "/emp.tsv");
//
//
//    for (int i = 0; i < nclients; i++) {
//
//      this.query_script[i] = String.format(
//          FileUtils.readFileToString(
//              new File(TestAdpDBClient.class.getResource("partition_emp_template.sql").getFile())),
//          "emp_res_" + String.valueOf(i),
//          String.valueOf(nworkers + 1),
//          "emp");
//    }
//    log.debug("Scripts successfully formatted.");
//
//    this.dbPathName = "/tmp/db/client-test-" + String.valueOf(System.currentTimeMillis());
//    new File(dbPathName).mkdirs();
//    log.debug("Database created.");
//
//    log.debug("---- SETUP ----");
//  }
//
//  @Test
//  public void testAdpDBClient() throws Exception {
//    log.debug("---- TEST ----");
//
//    AdpDBClientProperties properties = new AdpDBClientProperties(dbPathName);
//
//    ExaremeCluster miniCluster =
//        ExaremeClusterFactory.createMiniCluster(registryPort, dtPort, nworkers);
//    log.debug("Mini cluster created.");
//
//    miniCluster.start();
//    log.debug("Mini cluster started.");
//
//    ExecutorService executorService = Executors.newFixedThreadPool(nclients);
//    log.debug("Executor service created.");
//
//    RunnableAdpDBInsertClient insertClient = new RunnableAdpDBInsertClient(0,
//        miniCluster.getExaremeClusterClient(properties));
//    insertClient.run();
//
//    for (int i = 0; i < nclients; i++) {
//      RunnableAdpDBRepartitionClient run = new RunnableAdpDBRepartitionClient(i,
//          miniCluster.getExaremeClusterClient(properties));
//      run.run();
//      //executorService.execute(run);
//    }
//    log.debug("Clients created.(" + nclients + ")");
//
//    executorService.shutdown();
//    log.debug("Executor service shutdown.");
//
//    try {
//      executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//    } catch (InterruptedException e) {
//      log.error("Unable shutdown executor.", e);
//    }
//
//    miniCluster.stop(true);
//    log.debug("Mini cluster stopped.");
//
//    log.debug("---- TEST ----");
//  }
//
//  @After
//  public void tearDown() throws Exception {
//    log.debug("---- CLEAN ----");
//    FileUtils.deleteDirectory(new File(dbPathName));
//    log.debug("---- CLEAN ----");
//  }
//
//}
