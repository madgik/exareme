/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package madgik.exareme.utils.embedded.db;

import java.io.Serializable;

/**
 * @author herald
 */
public class TableInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String tableName = null;
  private String sqlDefinition = null;

  public TableInfo(String tableName) {
    this.tableName = tableName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setSqlDefinition(String sqlDefinition) {
    this.sqlDefinition = sqlDefinition;
  }

  public String getSQLDefinition() {
    return sqlDefinition;
  }
}
