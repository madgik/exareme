/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.embedded.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author herald
 */
public class DBUtils {


    private DBUtils() {
    }

    public static SQLDatabase createEmbeddedSqliteDB() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Cannot find sqlite jdbc driver", e);
        }

        Connection conn = DriverManager.getConnection("jdbc:sqlite:");
        return new EmbeddedDB(conn);
    }

    public static SQLDatabase createEmbeddedMadisDB(String engine) throws SQLException {
        try {
            Class.forName("madgik.exareme.utils.embedded.AdpEmbeddedDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Cannot find exareme jdbc driver", e);
        }
        try {
            Properties prop = new Properties();
            prop.setProperty("MADIS_PATH", engine);
            Connection conn = DriverManager.getConnection("jdbc:adp:", prop);
            return new EmbeddedDB(conn);
        } catch (Exception e) {
            throw new SQLException("Unable to create db!", e);
        }
    }

    public static SQLDatabase createEmbeddedSqliteDB(String database) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Cannot find sqlite driver", e);
        }

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + database);
        return new EmbeddedDB(conn);
    }
}
