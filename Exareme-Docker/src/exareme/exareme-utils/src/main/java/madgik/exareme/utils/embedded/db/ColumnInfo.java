/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package madgik.exareme.utils.embedded.db;

import java.io.Serializable;

/**
 * @author herald
 */
public class ColumnInfo implements Serializable, Comparable<ColumnInfo> {
    private static final long serialVersionUID = 1L;

    private String tableName = null;
    private String columnName = null;
    private boolean filtered = false;
    private boolean joined = false;

    public ColumnInfo(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public int compareTo(ColumnInfo o) {
        int tableNameComp = tableName.compareTo(o.tableName);
        if (tableNameComp != 0) {
            return tableNameComp;
        }

        int columnNameComp = columnName.compareTo(o.columnName);
        if (columnNameComp != 0) {
            return columnNameComp;
        }

        if (filtered != o.filtered) {
            if (filtered == false) {
                return -1;
            }

            return 1;
        }

        if (joined != o.joined) {
            if (joined == false) {
                return -1;
            }

            return 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ColumnInfo == false) {
            throw new IllegalArgumentException("Object not of type 'ColumnInfo'");
        }

        return compareTo((ColumnInfo) obj) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.tableName != null ? this.tableName.hashCode() : 0);
        hash = 79 * hash + (this.columnName != null ? this.columnName.hashCode() : 0);
        hash = 79 * hash + (this.filtered ? 1 : 0);
        hash = 79 * hash + (this.joined ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return tableName + "." + columnName + "(" + filtered + "," + joined + ")";
    }
}
