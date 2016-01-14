/**
 * Copyright MaDgIK Group 2010 - 2013.
 */
package madgik.exareme.utils.embedded;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author herald
 */
public class AdpEmbeddedDriver implements Driver {
  private static final Logger log = Logger.getLogger(AdpEmbeddedDriver.class.getName());

  static {
    try {
      DriverManager.registerDriver(new AdpEmbeddedDriver());
    } catch (SQLException e) {
      log.log(Level.SEVERE, "Cannot register Embedded ADP JDBC Driver: {0}", e.getSQLState());
    }
  }

  public AdpEmbeddedDriver() {
    // Nothing
  }

  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    if (!acceptsURL(url)) {
      throw new SQLException("invalid database address: " + url);
    }
    return new AdpEmbeddedConnection(url, info);
  }

  @Override
  public boolean acceptsURL(String url) throws SQLException {
    return url != null && url.toLowerCase().startsWith("jdbc:adp:");
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
      throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getMajorVersion() {
    return 1;
  }

  @Override
  public int getMinorVersion() {
    return 1;
  }

  @Override
  public boolean jdbcCompliant() {
    return true;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return log;
  }
}
