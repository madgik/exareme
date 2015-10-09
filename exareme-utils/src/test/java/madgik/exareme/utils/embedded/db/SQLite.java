/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.embedded.db;

import org.apache.log4j.Logger;

import java.sql.*;

/**
 * @author herald
 */
public class SQLite {

    private static Logger log = Logger.getLogger(madgik.exareme.utils.embedded.db.SQLite.class);

    public static Connection createConnection(String database) throws SQLException {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + database);

        return conn;
    }

    public static void main(String[] args) throws Exception {
        Connection conn = madgik.exareme.utils.embedded.db.SQLite.createConnection("");
        Statement stat = conn.createStatement();
        stat.executeUpdate(
            "drop table if exists people; " + "create table people (name, occupation);");

        PreparedStatement prep = conn.prepareStatement("insert into people values (?, ?);");

        conn.setAutoCommit(false);

        prep.setString(1, "Gandhi");
        prep.setString(2, "politics");
        prep.addBatch();
        prep.setString(1, "Turing");
        prep.setString(2, "computers");
        prep.addBatch();
        prep.setString(1, "Wittgenstein");
        prep.setString(2, "smartypants");
        prep.addBatch();

        prep.executeBatch();
        conn.setAutoCommit(true);

        ResultSet rs = stat.executeQuery("select * from people;");
        while (rs.next()) {
            System.out.println("name = " + rs.getString("name"));
            System.out.println("job = " + rs.getString("occupation"));
        }
        rs.close();
        conn.close();
    }
}
