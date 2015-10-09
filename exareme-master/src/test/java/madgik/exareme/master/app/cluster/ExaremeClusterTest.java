package madgik.exareme.master.app.cluster;

import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.client.AdpDBClientQueryStatus;
import madgik.exareme.utils.file.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

/**
 * @author alex
 */
public class ExaremeClusterTest {
    private static final Logger log = Logger.getLogger(ExaremeClusterTest.class);

    @Test public void testMiniCluster() throws Exception {
        Logger.getRootLogger().setLevel(Level.INFO);
        int port = 1099;
        int dtport = 8088;
        int nworkers = 1;

        ExaremeCluster cluster = ExaremeClusterFactory.createMiniCluster(port, dtport, nworkers);
        cluster.start();
        log.info("Cluster started.");
        log.info("IsUp : " + cluster.isUp());

        AdpDBClientProperties clientProperties = new AdpDBClientProperties("/tmp/demo-db");
        AdpDBClient dbClient = cluster.getExaremeClusterClient(clientProperties);
        log.info("Client created");

        String tableName = "emp_parted_" + String.valueOf(System.currentTimeMillis());
        File loadFile =
            new File(ExaremeCluster.class.getResource("load_emp_template.sql").getFile());
        String queryScript = String
            .format(FileUtils.readFileToString(loadFile), tableName, String.valueOf(nworkers + 1),
                loadFile.getParentFile().getAbsolutePath() + "/emp.tsv");
        log.info("Query script loaded.");


        AdpDBClientQueryStatus queryResult = dbClient.query("", queryScript);
        if (queryResult.hasError()) {
            log.error("Error occured : " + queryResult.getError());
        }
        log.info("Query successfully executed.");

        log.info(FileUtil.consume(dbClient.readTable(tableName)));
        cluster.stop(true);
        log.info("Cluster stopped.");

    }
}
