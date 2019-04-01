/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.stat;

import java.util.Map;

/**
 * @author jim
 */
public class Table {
    private final String tableName;
    private final int numberOfColumns;
    private final int toupleSize;
    private Map<String, Column> columnMap;
    private final int numberOfTuples;
    private final String primaryKey;

    public Table(String tableName, int numberOfColumns, int toupleSize,
                 Map<String, Column> columnMap, int not, String pk) {
        this.tableName = tableName;
        this.numberOfColumns = numberOfColumns;
        this.toupleSize = toupleSize;
        this.numberOfTuples = not;
        this.primaryKey = pk;
        this.columnMap = columnMap;
    }

    public int getNumberOfTuples() {
        return numberOfTuples;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public String getTableName() {
        return tableName;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public int getToupleSize() {
        return toupleSize;
    }

    public Map<String, Column> getColumnMap() {
        return columnMap;
    }

    @Override
    public String toString() {
        return "Table{" + "tableName=" + tableName + ", numberOfColumns=" + numberOfColumns
                + ", toupleSize=" + toupleSize + ", columnMap=" + columnMap + '}';
    }

}
