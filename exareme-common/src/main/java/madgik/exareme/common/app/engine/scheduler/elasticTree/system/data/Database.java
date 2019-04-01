/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.data;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author heraldkllapi
 */
public class Database {
    private static final DecimalFormat df = new DecimalFormat("#.##");
    // Tables
    private final ArrayList<Table> tables = new ArrayList<>();
    private final HashMap<String, Table> tableNameMap = new HashMap<>();
    private double totalTableSize = 0.0;
    private int maxNumOfParts = 0;

    public Database() {

    }

    public void addTable(Table table) {
        tables.add(table);
        tableNameMap.put(table.getName(), table);
        totalTableSize += table.getTotalSize();
        // Register table to registry
        table.registerTable();
        if (maxNumOfParts < table.getNumParts()) {
            maxNumOfParts = table.getNumParts();
        }
    }

    public int getNumTables() {
        return tables.size();
    }

    public List<Table> getTables() {
        return tables;
    }

    public Table getTable(String name) {
        return tableNameMap.get(name);
    }

    public double getTotalTableSize() {
        return totalTableSize;
    }

    public int getMaxNumOfParts() {
        return maxNumOfParts;
    }

    @Override
    public String toString() {
        return "Num Tables:       " + tables.size() + "\n" +
                "Total Size:       " + df.format(totalTableSize);
    }
}
