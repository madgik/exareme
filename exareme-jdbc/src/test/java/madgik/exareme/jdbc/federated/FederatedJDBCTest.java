package madgik.exareme.jdbc.federated;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

public class FederatedJDBCTest {
    private static final Logger log = Logger.getLogger(FederatedJDBCTest.class);

    public static void printResultset(ResultSet rs) throws SQLException {
        log.info("Columns: " + rs.getMetaData().getColumnCount());
        int count = 0;
        int size = 0;
        for (int c = 0; c < rs.getMetaData().getColumnCount(); ++c) {
            log.info(rs.getMetaData().getColumnName(c + 1));
        }
        while (rs.next()) {
            String[] next = new String[rs.getMetaData().getColumnCount()];
            for (int c = 0; c < next.length; ++c) {
                next[c] = "" + rs.getObject(c + 1);
                size += next[c].length();
            }
            for (String v : next) {
                log.info(v + "\t");
            }
            log.info("");
            ++count;
        }
        System.out.println("Count: " + count + "\n\tSize: " + size);
    }

    @Before public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.ALL);

    }

    @Test public void testJDBC() throws Exception {
        log.info("--------- TEST -------------");
        // Load the driver
        Class.forName("madgik.exareme.jdbc.federated.AdpDriver");

        String conString =
            "jdbc:fedadp:http://127.0.0.1:9090/tmp/-fedDB-npd-next-jdbc:mysql://10.240.0.10:3306/npd-next-com.mysql.jdbc.Driver-next-benchmark-next-gray769watt724!@#-next-npd";

        Connection conn = DriverManager.getConnection(conString, "adp", "adp");


        String q1 = "select * from npd_wellbore_core";
        String q2 = "SELECT \n" +
            "  QVIEW5.`wlbTotalCoreLength`  AS `year`\n" +
            " FROM \n" +
            "npd_wellbore_core QVIEW5,\n" +
            "npd_wellbore_shallow_all QVIEW6 \n" +
            "WHERE \n" +
            "(QVIEW5.`wlbNpdidWellbore` = QVIEW6.`wlbNpdidWellbore`)";
        Statement st = conn.createStatement();
        log.info("Statement created.");

        ResultSet rs = st.executeQuery(q1);
        log.info("Query executed.");
        printResultset(rs);
        rs.close();
        st.close();
        log.info("--------- TEST -------------");

    }

}
