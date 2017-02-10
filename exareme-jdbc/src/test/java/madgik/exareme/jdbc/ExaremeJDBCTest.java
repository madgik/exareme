//package madgik.exareme.jdbc;
//
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.sql.*;
//
///**
// * @author alex
// */
//public class ExaremeJDBCTest {
//    private static final Logger log = Logger.getLogger(ExaremeJDBCTest.class);
//
//    public static void printResultset(ResultSet rs) throws SQLException {
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
//        System.out.println("Count: " + count + "\n\tSize: " + size);
//    }
//
//    @Before public void setUp() throws Exception {
//        Logger.getRootLogger().setLevel(Level.ALL);
//
//    }
//
//    @Test public void testJDBC() throws Exception {
//        log.info("--------- TEST -------------");
//        // Load the driver
//        Class.forName("madgik.exareme.jdbc.federated.AdpDriver");
//
//        String database = "jdbc:fedadp:http://10.254.11.23:9090/home/adp/database/demo/";
//        Connection conn = DriverManager.getConnection(database);
//        log.info("Connections created.");
//
//        String tablename = "wellbore";
//        //        String q="select * from "+tablename;
//        String q = "select * from demo  ";
//        Statement st = conn.createStatement();
//        log.info("Statement created.");
//
//        ResultSet rs = st.executeQuery(q);
//        log.info("Query executed.");
//        printResultset(rs);
//        rs.close();
//        st.close();
//        log.info("--------- TEST -------------");
//
//    }
//}
