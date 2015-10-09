package madgik.exareme.master.client;

import madgik.exareme.master.app.cluster.ExaremeCluster;
import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author alex
 */
public class TestRemoteQueries {
    private static final Logger log = Logger.getLogger(TestRemoteQueries.class);


    @Before public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.DEBUG);

    }


    @Test public void testRemoteQueries() throws Exception {

        String database = "/tmp/db/demo";
        String remoteQueryScript = "distributed create temporary table demo1 as external \n" +
            "select * \n" +
            "from file('http://sites.google.com/site/stats202/data/test_data.csv?attredirects=0', header:t) limit 10;\n"
            +
            "distributed create temporary table demo2 as external \n" +
            "select * \n" +
            "from file('http://sites.google.com/site/stats202/data/test_data.csv?attredirects=0', header:t) limit 10;\n"
            +
            "distributed create temporary table result as direct \n" +
            "select * \n" +
            "from demo1 d1, demo2 d2\n" +
            "where d1.Age = d2.Age;";
        log.info(remoteQueryScript);
        ExaremeCluster miniCluster = ExaremeClusterFactory.createMiniCluster(9090, 8090, 0);
        miniCluster.start();
        AdpDBClientProperties properties =
            new AdpDBClientProperties(database, "", "", false, false, -1, 10);
        AdpDBClient dbClient = miniCluster.getExaremeClusterClient(properties);
        String explain = dbClient.explain(remoteQueryScript, "json");
        log.info(explain);

        miniCluster.stop(true);
    }

    @After public void tearDown() throws Exception {


    }
}
