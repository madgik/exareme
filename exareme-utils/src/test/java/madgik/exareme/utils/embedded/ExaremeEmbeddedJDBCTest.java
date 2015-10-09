/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.embedded;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author herald
 */
public class ExaremeEmbeddedJDBCTest {
    private static final Logger log = Logger.getLogger(ExaremeEmbeddedJDBCTest.class);

    private static void execQuery(String query) throws Exception {
        Properties prop = new Properties();
        prop.setProperty("MADIS_PATH", "/opt/madis/src/mterm.py");
        Connection conn = DriverManager.getConnection("jdbc:adp:", prop);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE works(`eid`,`did`,`pct_time`);");
        stmt.executeUpdate("CREATE TABLE xrs1(`eid`,`did`,`pct_time`);");
        stmt.executeUpdate("CREATE TABLE xrs2(`eid`,`did`,`pct_time`);");
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            String[] next = new String[rs.getMetaData().getColumnCount()];
            for (int c = 0; c < next.length; ++c) {
                next[c] = "" + rs.getObject(c + 1);
            }
            for (String v : next) {
                System.out.print(v + "\t");
            }
            System.out.println("");
        }
        log.info("Num of Columns: " + rs.getMetaData().getColumnCount());
        for (int c = 0; c < rs.getMetaData().getColumnCount(); ++c) {
            System.out.println("'" + rs.getMetaData().getColumnName(c + 1) + "' " +
                rs.getMetaData().getColumnTypeName(c + 1));
        }

        rs.close();
        stmt.close();
        conn.close();
    }

    @Before public void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        log.debug("---- SETUP ----");
        log.debug("---- SETUP ----");
    }

    //    public static void main(String[] args) throws Exception {
    //        String q = "  \n\n  .queryplan select\n" +
    //            "  cast(c1 as int) as eid,\n" +
    //            "  cast(c2 as text) as ename,\n" +
    //            "  cast(c3 as int) as age,\n" +
    //            "  cast(c4 as float) as salary\n" +
    //            "from file('/home/xrs/Code/IdeaProjects/exareme/exareme-worker/target/test-classes/madgik/exareme/db/app/client/emp.tsv')";
    //
    //        BasicConfigurator.configure();
    //        Logger.getRootLogger().setLevel(Level.OFF);
    //        Class.forName("madgik.exareme.utils.embedded.AdpEmbeddedDriver");
    //        long start = System.currentTimeMillis();
    //        execQuery(q);
    //        long end = System.currentTimeMillis();
    //        log.info("Time: " + (end - start));
    //    }

    @Test public void testEmbeddedJDBC() throws Exception {
        log.debug("---- TEST ----");
        String loadQuery = String.format(new String(Files.readAllBytes(Paths
                .get(ExaremeEmbeddedJDBCTest.class.getResource("sample_query.sql").getFile()))),
            ExaremeEmbeddedJDBCTest.class.getResource("emp.tsv").getFile());
        log.debug(loadQuery);

        Class.forName("madgik.exareme.utils.embedded.AdpEmbeddedDriver");

        Properties properties = new Properties();
        String madis_path = "../exareme-tools/madis/src/main/python/madis/src/mterm.py";

        properties.setProperty("MADIS_PATH", madis_path);
        Connection connection = DriverManager.getConnection("Jdbc:adp:", properties);
        Statement statement = connection.createStatement();
        statement.close();
        connection.close();
        log.debug("---- TEST ----");

    }
}
