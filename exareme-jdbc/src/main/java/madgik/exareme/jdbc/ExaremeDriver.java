package madgik.exareme.jdbc;

import java.net.URI;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 */
public class ExaremeDriver implements Driver {
    private static final Logger log = Logger.getLogger(ExaremeDriver.class.getName());
    private static final String URL_PREFIX = "jdbc:exareme://";

    static {
        try {
            DriverManager.registerDriver(new ExaremeDriver());
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public ExaremeDriver() {
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (info != null && info.size() > 0)
            throw new UnsupportedOperationException("Properties not supported yet.");
        if (!acceptsURL(url))
            throw new SQLException("Please provide valid url.");
        URI dbURL = URI.create(url.substring(5));
        return new ExaremeConnection(dbURL.getHost(), dbURL.getPort(), dbURL.getPath());
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.toLowerCase().startsWith(ExaremeDriver.URL_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        throw new UnsupportedOperationException("Properties not supported yet.");
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return log;
    }

}
