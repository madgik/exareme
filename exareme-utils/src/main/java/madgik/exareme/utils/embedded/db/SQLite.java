/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package madgik.exareme.utils.embedded.db;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author herald
 */
public class SQLite {

    private static Logger log = Logger.getLogger(SQLite.class);

    private SQLite() {
    }

    public static Connection createConnection(String database) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            throw new SQLException("SQLite driver not found", e);
        }
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + database);
        return conn;
    }
}
