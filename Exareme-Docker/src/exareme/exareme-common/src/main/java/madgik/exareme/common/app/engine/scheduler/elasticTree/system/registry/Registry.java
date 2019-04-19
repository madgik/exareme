/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author heraldkllapi
 */
public class Registry {
    private static final HashMap<String, TablePartitionEntry> tablePartMap = new HashMap<>();
    private static final HashMap<String, IntermediateResultEntry> interResultMap = new HashMap<>();
    // Mapping of table parts to indexes
    private static final HashMap<String, HashSet<String>> tableIndexMap = new HashMap<>();

    public static void register(TablePartitionEntry partition) {
        tablePartMap.put(partition.getId(), partition);
        tableIndexMap.put(partition.getId(), new HashSet<String>());
    }

    public static TablePartitionEntry getTablePart(String tableName, int part) {
        return tablePartMap.get(createId(tableName, part));
    }

    public static void register(IntermediateResultEntry entry) {
        interResultMap.put(entry.getId(), entry);
    }

    public static IntermediateResultEntry getInterResultEntry(int dataflowId, String name) {
        return interResultMap.get(createId(name, dataflowId));
    }

    public static String createId(String name, int id) {
        return id + ":" + name;
    }

    public void updateTablePart(String tableName, int part) {
        updateTablePart(createId(tableName, part));
    }

    public void updateTablePart(String id) {
    }
}
