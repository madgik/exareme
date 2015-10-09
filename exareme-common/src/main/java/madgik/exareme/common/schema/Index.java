/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import java.io.Serializable;
import java.util.BitSet;

/**
 * @author herald
 */
public class Index implements Serializable {
    private static final long serialVersionUID = 1L;

    private String indexName = null;
    private String tableName = null;
    private String columnName = null;
    private BitSet partitions = null;

    public Index(String tableName, String columnName, String indexName) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.indexName = indexName;
        this.partitions = new BitSet();
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void addPartition(int pNum) {
        partitions.set(pNum);
    }

    public boolean existsOnParition(int pNum) {
        return partitions.get(pNum);
    }

    public BitSet getParitions() {
        return partitions;
    }

    @Override public String toString() {
        return "build index " + indexName + " on " + tableName + "(" + columnName + ")";
    }
}
