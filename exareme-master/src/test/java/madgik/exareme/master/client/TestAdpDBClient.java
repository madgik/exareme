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
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



/**
 * Testing multiple clients on Pseudo-distributed cluster :
 * + Initialize cluster ( registrPort, nworkers)
 * + Initialize clients ( nclients)
 * + Each client :
 * 1. Formats templates scripts.
 * 2. Runs load and index scripts.
 * 3. Explains query script.
 * 4. Runs query script.
 * + TODO validates results. (looks ok).
 *
 * @author alex
 */
public class TestAdpDBClient {
    private static final Logger log = Logger.getLogger(TestAdpDBClient.class);
    private int registryPort = 1099;
    private int dtPort = 8088;
    private int nclients = 1;
    private int nworkers = 0; //should not change
    private String dbPathName;
    private String[] load_script;
    private String[] index_script;
    private String[] query_script;
    private Boolean problem = false;


    public TestAdpDBClient() {

    }

    @Before public void setUp() throws Exception {

        Logger.getRootLogger().setLevel(Level.DEBUG);
        //        Thread.sleep(1000*20);
        log.debug("---- SETUP ----");
//        log.debug(TestAdpDBClient.class.getResource("load_emp_template.sql") == null);
        // load & format scripts
        File loadFile =
            new File(TestAdpDBClient.class.getResource("load_emp_template.sql").getFile());
        load_script = new String[nclients];
        index_script = new String[nclients];
        query_script = new String[nclients];

        for (int i = 0; i < nclients; i++) {
            this.load_script[i] = String
                .format(FileUtils.readFileToString(loadFile), "emp_"+String.valueOf(i),
                    String.valueOf(nworkers + 1),
                    loadFile.getParentFile().getAbsolutePath() + "/emp.tsv");
            this.index_script[i] = String.format(FileUtils.readFileToString(new File(
                    TestAdpDBClient.class.getResource("index_emp_template.sql").getFile())),
                "emp_eid_" + String.valueOf(i), "emp_" + String.valueOf(i));
            this.query_script[i] = String.format(FileUtils.readFileToString(new File(
                    TestAdpDBClient.class.getResource("query_emp_template.sql").getFile())),
                "emp_20000_" + String.valueOf(i), "emp_" + String.valueOf(i));
        }
        log.debug("Scripts successfully formatted.");


        this.dbPathName = "/tmp/db/client-test"+System.currentTimeMillis()+"-" + String.valueOf(System.currentTimeMillis());
        new File(dbPathName).mkdirs();
        log.debug("Database created. "+dbPathName);

        log.debug("---- SETUP ----");
    }

    @Test public void testAdpDBClient() throws Exception {
        log.debug("---- TEST ----");

        AdpDBClientProperties properties =
            new AdpDBClientProperties(dbPathName, null, null, false, false, -1, 10);

        ExaremeCluster miniCluster =
            ExaremeClusterFactory.createMiniCluster(registryPort, dtPort, nworkers);
        log.debug("Mini cluster created.");

        miniCluster.start();
        log.debug("Mini cluster started.");

        ExecutorService executorService = Executors.newFixedThreadPool(nclients);
        log.debug("Executor service created.");

        for (int i = 0; i < nclients; i++) {
            executorService.execute(
                new RunnableAdpDBClient(i, miniCluster.getExaremeClusterClient(properties)));
        }
        log.debug("Clients created.(" + nclients + ")");

        executorService.shutdown();

        log.debug("Executor service shutdown.");

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("Unable shutdown executor.", e);
        }

        Assert.assertFalse(problem);

        log.debug("Mini cluster stopped1.");


        miniCluster.stop(false);
        log.debug("Mini cluster stopped2.");

        Thread.sleep(12*1000);
        log.debug("Mini cluster stopped3.");

        miniCluster.stop(true);
        Thread.sleep(5*1000);
        log.debug("Mini cluster stopped4.");

        log.debug("Mini cluster stopped.");

        log.debug("---- TEST ----");
    }

    @After public void tearDown() throws Exception {
        log.debug("---- CLEAN ----");
                FileUtils.deleteDirectory(new File(dbPathName));
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
                AdpDBClientQueryStatus queryStatus =
                    client.query("load_" + String.valueOf(id), load_script[id]);
                while (queryStatus.hasFinished() == false && queryStatus.hasError() == false) {
                    Thread.sleep(1000 * 2);
                }
                if (queryStatus.hasError() || queryStatus==null) {
                    log.error("Exception occured..." + queryStatus.getError());
                    problem = true;
                }



                log.info("Client " + id + " finished.");
            } catch (RemoteException e) {
                log.error("Error occurred ( " + String.valueOf(id) + ")!", e);

            } catch (IOException e) {
                log.error("Error occurred while reading results", e);

            } catch (Exception e) {
                log.error("Error");
            }
        }
    }

}
