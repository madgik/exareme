/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.jdbc.federated;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author dimitris
 */
public class AdpDriver implements Driver {
    private static Logger log = Logger.getLogger(AdpDriver.class.getName());

    static {
        try {
            DriverManager.registerDriver(new AdpDriver());
        } catch (Exception e) {
            log.warning("Cannot register ADP JDBC Driver.");
        }
    }

    public AdpDriver() {
    }

    @Override public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            throw new SQLException("invalid database address: " + url);
        }
        return new AdpConnection(url, info);
    }

    @Override public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.toLowerCase().startsWith("jdbc:fedadp:");
    }

    @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
        throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public int getMajorVersion() {
        return 1;
    }

    @Override public int getMinorVersion() {
        return 1;
    }

    @Override public boolean jdbcCompliant() {
        return true;
    }

    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return log;
    }
}
