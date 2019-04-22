package madgik.exareme.worker.arm;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author alex
 */
public class ClusterArmStorageClientTest {
    private static final Logger log = Logger.getLogger(ClusterArmStorageClientTest.class);
    private static Configuration configuration = null;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.INFO);
        log.info("----- SETUP -----");
        configuration = new Configuration();
        log.info("----- SETUP -----");
    }


    @Test
    public void testArmStorageClient() throws Exception {
        log.info("----- TEST -----");
        MiniDFSCluster.Builder dfsClusterBuilder = new MiniDFSCluster.Builder(configuration);
        dfsClusterBuilder.numDataNodes(2);
        MiniDFSCluster dfsCluster = dfsClusterBuilder.build();
        log.info("Mini DFS Cluster created.");

        log.info("NN :" + dfsCluster.getNameNode().getTokenServiceName());
        for (DataNode dataNode : dfsCluster.getDataNodes()) {
            log.info("DN : " + dataNode.getDisplayName());
        }
        DistributedFileSystem dfs = dfsCluster.getFileSystem();
        for (DatanodeInfo datanodeInfo : dfs.getDataNodeStats()) {
            log.info(datanodeInfo.toString());
        }

        if (dfsCluster != null) {
            dfsCluster.shutdown(true);
            dfsCluster = null;
        }
        log.info("Mini DFS Cluster shutdown.");
        log.info("----- TEST -----");
    }

    @After
    public void tearDown() throws Exception {

        log.info("----- CLEAN -----");

        log.info("----- CLEAN -----");
    }

}
