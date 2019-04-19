/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package madgik.exareme.utils.embedded.db;


import madgik.exareme.utils.collections.ReadOnlyViewList;
import madgik.exareme.utils.collections.ReadOnlyViewMap;

import java.io.Serializable;
import java.util.*;

/**
 * @author herald
 * @author Christoforos Svingos
 */
public class SQLQueryInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String query = null;
    private ReadOnlyViewList<String> inputTables = null;
    private ReadOnlyViewMap<String, Set<ColumnInfo>> usedColumns = null;

    public SQLQueryInfo(String query) {
        this.query = query;
        this.inputTables = new ReadOnlyViewList<String>(new ArrayList<String>());
        this.usedColumns =
                new ReadOnlyViewMap<String, Set<ColumnInfo>>(new HashMap<String, Set<ColumnInfo>>());
    }

    public String getQuery() {
        return query;
    }

    public List<String> getInputTables() {
        return inputTables.getReadOnlyView();
    }

    public void addInputTable(String tableName) {
        Set<ColumnInfo> columnsSet = usedColumns.getMap().get(tableName);
        if (columnsSet == null) {
            columnsSet = new HashSet<>();
            usedColumns.getMap().put(tableName, columnsSet);
            inputTables.getList().add(tableName);
        }
    }

    // Table Name -> List of column names
    public Map<String, Set<ColumnInfo>> getUsedColumns() {
        return usedColumns.getReadOnlyView();
    }

    public void addUsedColumn(String tableName, String columnName, boolean filtered,
                              boolean joined) {
        Set<ColumnInfo> columnsSet = usedColumns.getMap().get(tableName);
        if (columnsSet == null) {
            columnsSet = new HashSet<ColumnInfo>();
            usedColumns.getMap().put(tableName, columnsSet);
        }
        ColumnInfo cInfo = new ColumnInfo(tableName, columnName);
        cInfo.setFiltered(filtered);
        cInfo.setJoined(joined);
        columnsSet.add(cInfo);
    }
}
