package madgik.exareme.master.client2;

import madgik.exareme.master.app.cluster.ExaremeCluster;
import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;


/**
 * Testing 2 remote queries use cases (import, pipeline).
 * @author alex
 */
public class TestRemoteQueries {
    private static final Logger log = Logger.getLogger(TestRemoteQueries.class);
    private static String dbPathName;
    private Boolean problem = false;


    @Before public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);
        log.debug("---- SETUP ----");

        dbPathName = "/tmp/db/client-test2-" + String.valueOf(System.currentTimeMillis());
        new File(dbPathName).mkdirs();
        log.debug("Database created.");

        log.debug("---- SETUP ----");
    }

    @Test public void testImportRemoteQueries() throws Exception {
        log.debug("---- TEST ----");
        String remoteQueryScript =
            "distributed create temporary table demo1 to 8 on key as remote \n"
                + "select t1.C1 as key\n"
                + "from range(40) t1;\n"
            + "distributed create temporary table demo2 to 8 on key as remote \n"
                + "select t1.C1 as key, t2.C1 as value\n"
                + "from range(40) t1, range(10) t2;\n"
            + "distributed create temporary table demo3 to 1 as direct \n"
                + "select value, count(*) as partial_count\n"
                + "from (\n"
                    + "select d2.value as value\n"
                    + "from demo1 d1, demo2 d2\n"
                    + "where d1.key = d2.key\n"
                + ")\n"
                + "group by value;\n"
            + "distributed create table results as tree\n"
                + "select value, sum(partial_count) as count\n"
                + "from demo3\n"
                + "group by value\n"
                + "order by count desc;\n";

        log.info(remoteQueryScript);

        ExaremeCluster miniCluster = ExaremeClusterFactory.createMiniCluster(9090, 8090, 3);
        miniCluster.start();
        log.debug("Mini cluster started.");

        AdpDBClientProperties properties =
            new AdpDBClientProperties(dbPathName, "", "", false, false, -1, 10);
        AdpDBClient client = miniCluster.getExaremeClusterClient(properties);

        //        String explain = dbClient.explain(remoteQueryScript, "json");
        //        log.info(explain);

        AdpDBClientQueryStatus queryStatus =
            client.query("load_", remoteQueryScript);
        while (queryStatus.hasFinished() == false && queryStatus.hasError() == false) {
            Thread.sleep(2 * 1000);
        }
        if (queryStatus.hasError()) {
            log.error("Exception occured..." + queryStatus.getError());
        }
        Assert.assertTrue(queryStatus != null);
        Assert.assertFalse(queryStatus.hasError());

        InputStream inputStream = client.readTable("results");
        log.info(IOUtils.toString(inputStream, Charset.defaultCharset()));

//        miniCluster.stop(ture);
        miniCluster.stop(true);
        Thread.sleep(12*1000);
        log.debug("Mini cluster stopped.");
        log.debug("---- TEST ----");
        log.debug("---- TEST ----");
    }

//    @Test public void testPipelineRemoteQueries() throws Exception {
//        log.debug("---- TEST ----");
//
//        String remoteQueryScript =
//            "distributed create temporary table demo1 as remote \n"
//            + "select count(C1) as counter  \n"
//            + "from range(10);\n"
//            + "using demo1 distributed create temporary table demo2 as remote \n"
//            + "select ( count(C1) + (select * from demo1) ) as counter \n"
//            + "from range(10);\n"
//            + "using demo2 distributed create table results as remote \n"
//            + "select (count(C1) + (select * from demo2)) as counter\n"
//            + "from range(10);\n";
//
//        log.info(remoteQueryScript);
//
//        ExaremeCluster miniCluster = ExaremeClusterFactory.createMiniCluster(9090, 8090, 3);
//        miniCluster.start();
//        log.debug("Mini cluster started.");
//
//        AdpDBClientProperties properties =
//            new AdpDBClientProperties(dbPathName, "", "", false, false, -1, 10);
//        AdpDBClient client = miniCluster.getExaremeClusterClient(properties);
//
////        String explain = dbClient.explain(remoteQueryScript, "json");
////        log.info(explain);
//
//        AdpDBClientQueryStatus queryStatus =
//            client.query("load_", remoteQueryScript);
//        while (queryStatus.hasFinished() == false && queryStatus.hasError() == false) {
//            Thread.sleep(10 * 1000);
//        }
//        if (queryStatus.hasError()) {
//            log.error("Exception occured..." + queryStatus.getError());
//        }
//        Assert.assertTrue(queryStatus != null);
//        Assert.assertFalse(queryStatus.hasError());
//
//        InputStream inputStream = client.readTable("results");
//        log.info(IOUtils.toString(inputStream, Charset.defaultCharset()));
//
//        miniCluster.stop(true);
//        log.debug("Mini cluster stopped.");
//        log.debug("---- TEST ----");
//
//    }

    @After public void tearDown() throws Exception {
        log.debug("---- CLEAN ----");
        FileUtils.deleteDirectory(new File(dbPathName));
        log.debug("---- CLEAN ----");
    }
}
