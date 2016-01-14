/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package madgik.exareme.utils.embedded.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author herald
 */
public interface SQLDatabase {

  /**
   * Execute an SQL script.
   *
   * @param sqlScript the script to be executed.
   * @throws SQLException
   */
  void execute(String sqlScript) throws SQLException;

  ResultSet executeAndGetResults(String query) throws SQLException;

  /**
   * Get information about an SQL query.
   *
   * @param query
   * @return the information of the query.
   * @throws SQLException
   */
  SQLQueryInfo getQueryInfo(String query) throws SQLException;

  /**
   * Get information about the table with the given name.
   *
   * @param tableName
   * @return table information.
   * @throws SQLException
   */
  TableInfo getTableInfo(String tableName) throws SQLException;

  /**
   * Close the connection to the database.
   *
   * @throws SQLException
   */
  void close() throws SQLException;
}
