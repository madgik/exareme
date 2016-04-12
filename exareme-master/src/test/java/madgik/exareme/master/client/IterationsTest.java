package madgik.exareme.master.client;

import madgik.exareme.master.app.cluster.ExaremeCluster;
import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Karampelas
 */
public class IterationsTest {
    private static final Logger log = Logger.getLogger(IterationsTest.class);
    private int registryPort = 1099;
    private int dtPort = 8088;
    private int nclients = 1;
    private int nworkers = 1;
    private String dbPathName;

    private String runPageRank;
    private String loadRanks;
    private String loadConnections;
    private String pageRanksScriptPath;
    private String pageConnectionsScriptPath;

    public IterationsTest() {

    }

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);
        String pageRankScripts = "pagerank.sql";
        String ranksFile = "page_rank_table.sql";
        String connectionsFile = "page_connection_table.sql";

        //String loadPageConnectionsQuery = "page_connection_table.sql";
//        Thread.sleep(1000*20);
        log.debug("---- SETUP ----");
        log.debug(IterationsTest.class.getResource(ranksFile) == null);
        log.debug(IterationsTest.class.getResource(connectionsFile) == null);
        log.debug(IterationsTest.class.getResource(pageRankScripts) == null);

        // In order for page rank algorithm to run, it needs two types of data as input
        // (page labels and page connections)
        // The aforementioned data exist in the following two files
        this.pageRanksScriptPath = IterationsTest.class.getResource("page_labels.csv").getPath();
        this.pageConnectionsScriptPath = IterationsTest.class.getResource("connection_graph.csv").getPath();

        // Load sql script that stores current pages' ranks from ranks file to the db
        this.loadRanks= FileUtils.readFileToString(new
                File(IterationsTest.class.getResource(ranksFile).getFile()));

        // Load sql script that stores the connections among the pages from connections file to the db
        this.loadConnections = FileUtils.readFileToString(new
                File(IterationsTest.class.getResource(connectionsFile).getFile()));

        // Load sql script that given the above two tables calculates the pagerank.
        this.runPageRank = FileUtils.readFileToString(new
                File(IterationsTest.class.getResource(pageRankScripts).getFile()));

        log.info(runPageRank);
        log.info(pageRanksScriptPath);
        log.debug("Script successfully formatted.");

        log.info(pageConnectionsScriptPath);
        log.debug("Script successfully formatted.");


        this.dbPathName = "/tmp/pagerank_db";
        new File(dbPathName).mkdirs();
        log.debug("Database created.");

        log.debug("---- SETUP ----");
    }


    @Test
    public void iterationsTest() throws Exception {
        AdpDBClientProperties properties = new AdpDBClientProperties(dbPathName + "/" + System.currentTimeMillis());
        log.debug("---- TEST ----");


        ExaremeCluster miniCluster =
                ExaremeClusterFactory.createMiniCluster(registryPort, dtPort, nworkers - 1);
        log.debug("Mini cluster created.");

        miniCluster.start();
        log.debug("Mini cluster started.");

        System.out.println(miniCluster.getContainers());
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        log.debug("Executor service created.");

        executorService.execute(new RunnableAdpDBClient(0,
                miniCluster.getExaremeClusterClient(properties)));

        executorService.shutdown();
        log.debug("Executor service shutdown.");

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e){
            log.error("Unable shutdown executor.",e);
        }

        miniCluster.stop(true);
        log.debug("Mini cluster stopped.");

        log.debug("---- TEST ----");
    }


    @After
    public void tearDown() throws Exception {
        log.debug("---- CLEAN ----");
//        FileUtils.deleteDirectory(new File(dbPathName));
        log.debug("---- CLEAN ----");
//        Thread.sleep(1000 * 20);

    }


    private class RunnableAdpDBClient implements Runnable {
        private final int id;
        private AdpDBClient client;

        public RunnableAdpDBClient(final int id, final AdpDBClient client) {
            this.id = id;
            this.client = client;
        }
        public void run() {
            try {

                // load pages' ranks from file to db
                AdpDBClientQueryStatus queryStatus =
                        client.iquery("loadRanks" ,  String.format(loadRanks,
                                nworkers, "'" + pageRanksScriptPath + "'"));

                while (queryStatus.hasFinished() == false && queryStatus.hasError() == false) {
                    Thread.sleep(10 * 1000);
                }

                if (queryStatus.hasError()) {
                    log.error("Exception occured..." + queryStatus.getError());
                }

                Assert.assertTrue(queryStatus != null);
                Assert.assertFalse(queryStatus.hasError());

                // load pages' connections from file to db
                queryStatus = client.iquery("loadConnections" ,  String.format(loadConnections,
                        nworkers, "'" + pageConnectionsScriptPath + "'"));
                while (queryStatus.hasFinished() == false && queryStatus.hasError() == false) {
                    Thread.sleep(10 * 1000);
                }
                if (queryStatus.hasError()) {
                    log.error("Exception occured..." + queryStatus.getError());
                }
                Assert.assertTrue(queryStatus != null);
                Assert.assertFalse(queryStatus.hasError());
//
                queryStatus = client.iquery("pagerank" ,  String.format(runPageRank,
                        nworkers, nworkers));

                while (queryStatus.hasFinished() == false && queryStatus.hasError() == false) {
                    Thread.sleep(10 * 1000);
                }
                // run the main algorithm

                if (queryStatus.hasError()) {
                    log.error("Exception occured..." + queryStatus.getError());
                }
                Assert.assertTrue(queryStatus != null);
                Assert.assertFalse(queryStatus.hasError());

                log.info("Client " + id + " finished.");
            } catch (RemoteException e){
                log.error("Error occurred ( "+ String.valueOf(id) + ")!", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
