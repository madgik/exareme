/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema;

import madgik.exareme.utils.collections.ReadOnlyViewList;
import madgik.exareme.utils.embedded.db.TableInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author herald
 */
public class PhysicalTable implements Serializable {
    private static final long serialVersionUID = 1L;

    private Table table = null;
    private ReadOnlyViewList<Partition> partitions = null;
    private ReadOnlyViewList<Index> indexes = null;
    private Map<String, Index> columnNameIndexMap = null;

    public PhysicalTable(Table table) {
        this.table = table;
        this.partitions = new ReadOnlyViewList<Partition>(new ArrayList<Partition>());
        this.indexes = new ReadOnlyViewList<Index>(new ArrayList<Index>());
        this.columnNameIndexMap = new HashMap<String, Index>();
    }

    public PhysicalTable(TableInfo tableInfo) {
        this(new Table(tableInfo.getTableName(), tableInfo.getSQLDefinition()));
    }

    public String getName() {
        return table.getName();
    }

    public Table getTable() {
        return table;
    }

    public void addPartition(Partition p) {
        this.partitions.getList().add(p);
    }

    public Partition getPartition(int part) {
        return partitions.getList().get(part);
    }

    public Partition removePartition(int part) {
        return partitions.getList().remove(part);
    }

    public List<Partition> getPartitions() {
        return partitions.getReadOnlyView();
    }

    public int getNumberOfPartitions() {
        return partitions.getList().size();
    }

    public void addIndex(Index idx) {
        indexes.getList().add(idx);
        columnNameIndexMap.put(idx.getColumnName(), idx);
    }

    public Index getIndex(String columnName) {
        return columnNameIndexMap.get(columnName);
    }

    public List<Index> getIndexes() {
        return indexes.getReadOnlyView();
    }

    public int getNumberOfIndexes() {
        return indexes.getList().size();
    }
}
