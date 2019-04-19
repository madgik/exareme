package madgik.exareme.common.schema;


import madgik.exareme.common.schema.expression.DataPattern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author herald
 */
public class TableView implements Serializable {
    private static final long serialVersionUID = 1L;

    private Table table = null;
    private DataPattern pattern = null;
    private ArrayList<String> patternColumnNames = null;
    private ArrayList<String> usedColumns = null;
    private int numOfPartitions = -1;

    public TableView(Table table) {
        this.table = table;
        this.usedColumns = new ArrayList<String>();
        this.patternColumnNames = new ArrayList<String>();
    }

    public Table getTable() {
        return table;
    }

    public String getName() {
        return table.getName();
    }

    public DataPattern getPattern() {
        return pattern;
    }

    public void setPattern(DataPattern pattern) {
        this.pattern = pattern;
    }

    public void addUsedColumn(String column) {
        usedColumns.add(column);
    }

    public void addPatternColumn(String column) {
        patternColumnNames.add(column);
    }

    public List<String> getPatternColumnNames() {
        return Collections.unmodifiableList(patternColumnNames);
    }

    public List<String> getUsedColumnNames() {
        return Collections.unmodifiableList(usedColumns);
    }

    public int getNumOfPartitions() {
        return numOfPartitions;
    }

    public void setNumOfPartitions(int numOfPartitions) {
        this.numOfPartitions = numOfPartitions;
    }

    @Override
    public String toString() {
        return "Partitions: " + numOfPartitions;
    }
}
