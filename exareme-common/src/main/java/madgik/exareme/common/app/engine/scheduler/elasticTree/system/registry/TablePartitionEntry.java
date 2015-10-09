/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.data.Table;

/**
 * @author heraldkllapi
 */
public class TablePartitionEntry implements Entry {
    private final Table table;
    private final int partId;
    private final String id;
    private final double size;

    public TablePartitionEntry(Table table, int partId, double size) {
        this.table = table;
        this.partId = partId;
        this.id = Registry.createId(table.getName(), partId);
        this.size = size;
    }

    public Table getTable() {
        return table;
    }

    public String getTableName() {
        return table.getName();
    }

    public int getPartId() {
        return partId;
    }

    @Override public double getSize_MB() {
        return size;
    }

    @Override public String getId() {
        return id;
    }

    @Override public String toString() {
        return "" + getId();
    }
}
