//package madgik.exareme.master.gateway;
//
//import madgik.exareme.master.app.cluster.ExaremeCluster;
//import madgik.exareme.master.app.cluster.ExaremeClusterFactory;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.junit.After;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
///**
// * @author alex
// */
//public class ExaremeGatewayJDBCTest {
//    private static final Logger log = Logger.getLogger(ExaremeGatewayJDBCTest.class);
//
//    private String dbname = null;
//
//    @org.junit.Before public void setUp() throws Exception {
//        log.info("---- SETUP ----");
//        log.info("---- SETUP ----");
//    }
//
//    @org.junit.Test public void testFederatedJDBC() throws Exception {
//        Logger.getRootLogger().setLevel(Level.ALL);
//
//        log.info("---- TEST ----");
//        ExaremeCluster miniCluster = ExaremeClusterFactory.createMiniCluster(1099, 8088, 0);
//        miniCluster.start();
//
//        // load driver
//        Class.forName("madgik.exareme.jdbc.federated.AdpDriver");
//
//        String url = "http://localhost:9090/query/" + dbname;
//        Connection conn = DriverManager.getConnection("jdbc:fedadp:" + url, "adp", "npd");
//        log.info("Connection created.");
//
//        // query
//        String tablename = "emp";
//        String q = "distributed create table test as select * from " + tablename + ";";
//        Statement st = conn.createStatement();
//        log.info("Statement created.");
//
//        ResultSet rs = st.executeQuery(q);
//        log.info("Query executed.");
//
//        log.info("Columns: " + rs.getMetaData().getColumnCount());
//        int count = 0;
//        int size = 0;
//        for (int c = 0; c < rs.getMetaData().getColumnCount(); ++c) {
//            log.info(rs.getMetaData().getColumnName(c + 1));
//        }
//        while (rs.next()) {
//            String[] next = new String[rs.getMetaData().getColumnCount()];
//            for (int c = 0; c < next.length; ++c) {
//                next[c] = "" + rs.getObject(c + 1);
//                size += next[c].length();
//            }
//            for (String v : next) {
//                log.info(v + "\t");
//            }
//            log.info("");
//            ++count;
//        }
//        log.info("Count: " + count + "\n\tSize: " + size);
//        rs.close();
//        st.close();
//
//        miniCluster.stop(false);
//        log.info("Manager stopped");
//
//        log.info("---- TEST ----");
//    }
//
//    @After public void tearDown() throws Exception {
//        log.info("---- CLEAN ----");
//        log.info("---- CLEAN ----");
//    }
//}
