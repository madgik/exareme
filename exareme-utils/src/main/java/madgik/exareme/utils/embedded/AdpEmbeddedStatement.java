/**
 * Copyright MaDgIK Group 2010 - 2013.
 */
package madgik.exareme.utils.embedded;

import madgik.exareme.utils.embedded.process.MadisProcess;
import madgik.exareme.utils.embedded.process.QueryResultStream;
import madgik.exareme.utils.embedded.utils.SqlParseUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.*;

/**
 * @author herald
 * @author Christoforos Svingos
 */
public class AdpEmbeddedStatement implements Statement {
  private static final Logger log = Logger.getLogger(AdpEmbeddedStatement.class);

  private final AdpEmbeddedConnection connection;
  private final MadisProcess process;
  private boolean closed = false;

  public AdpEmbeddedStatement(AdpEmbeddedConnection connection, MadisProcess process) {
    this.connection = connection;
    this.process = process;
  }

  @Override
  public void close() throws SQLException {
    if (closed) {
      return;
    }
    closed = true;
  }

  @Override
  public boolean isClosed() throws SQLException {
    return closed;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connection;
  }

  @Override
  public AdpEmbeddedResultSet executeQuery(String sql) throws SQLException {
    try {
      sql = sql.trim().endsWith(";") ? sql : sql + ";";
      int semicolonOccurences = SqlParseUtils.countOfqueries(sql);

      QueryResultStream result = process.execQuery(sql);
      return new AdpEmbeddedResultSet(result, this);
    } catch (IOException e) {
      throw new SQLException("Cannot execute query", e);
    }
  }

  @Override
  public int executeUpdate(String sql) throws SQLException {
    try {
      QueryResultStream stream = process.execQuery(sql);
      stream.close();
      return 0;
    } catch (IOException e) {
      throw new SQLException("Cannot execute update", e);
    }
  }

  @Override
  public void cancel() throws SQLException {
    try {
      process.cancel();
    } catch (Exception ex) {
      throw new SQLException(ex.getMessage());
    }
  }

  // NOT IMPLEMENTED BELOW

  @Override
  public ResultSet getResultSet() throws SQLException {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public int getMaxFieldSize() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setMaxFieldSize(int max) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getMaxRows() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setMaxRows(int max) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setQueryTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean execute(String sql) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getUpdateCount() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void clearWarnings() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setCursorName(String name) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setEscapeProcessing(boolean enable) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getFetchDirection() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getFetchSize() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getResultSetType() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addBatch(String sql) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void clearBatch() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int[] executeBatch() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean getMoreResults(int current) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isPoolable() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setPoolable(boolean poolable) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void closeOnCompletion() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
