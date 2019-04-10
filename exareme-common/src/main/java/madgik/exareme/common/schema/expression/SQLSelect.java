/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.schema.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author herald
 */
public class SQLSelect extends SQLQuery {
    private static final long serialVersionUID = 1L;

    private String resultTable = null;
    private boolean isTemporary = false;
    private boolean isScript = false;
    private DataPattern inputDataPattern = null;
    private DataPattern outputDataPattern = null;
    private int numberOfOutputPartitions = -1;
    private ArrayList<String> partitionColumns = null;
    private List<String> usingTBLs = null;

    public SQLSelect() {
        partitionColumns = new ArrayList<>();
        usingTBLs = new ArrayList<>();
    }

    public void setResultTable(String resultTable, boolean isTemporary, boolean isScript) {
        this.resultTable = resultTable;
        this.isTemporary = isTemporary;
        this.isScript = isScript;
    }

    public String getResultTable() {
        return resultTable;
    }

    public DataPattern getInputDataPattern() {
        return inputDataPattern;
    }

    public void setInputDataPattern(DataPattern inputDataPattern) {
        this.inputDataPattern = inputDataPattern;
    }

    public DataPattern getOutputDataPattern() {
        return outputDataPattern;
    }

    public void setOutputDataPattern(DataPattern outputDataPattern) {
        this.outputDataPattern = outputDataPattern;
    }

    public void addPartitionColumn(String name) {
        this.partitionColumns.add(name);
    }

    public List<String> getPartitionColumns() {
        return Collections.unmodifiableList(partitionColumns);
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public boolean isScript() {
        return isScript;
    }

    public int getNumberOfOutputPartitions() {
        return numberOfOutputPartitions;
    }

    public void setNumberOfOutputPartitions(int numberOfOutputPartitions) {
        this.numberOfOutputPartitions = numberOfOutputPartitions;
    }

    public List<String> getUsingTBLs() {
        return Collections.unmodifiableList(usingTBLs);
    }

    public void setUsingTBL(String usingTBL) {
        this.usingTBLs.add(usingTBL);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getComments().toString() + "---\n");
        sb.append("Result         : " + resultTable + "\n");
        sb.append("Temp           : " + isTemporary + "\n");
        sb.append("Input Pattern  : " + inputDataPattern + "\n");
        sb.append("Output Pattern : " + outputDataPattern + "\n");
        sb.append("Parts          : " + numberOfOutputPartitions + "\n");

        if (partitionColumns.size() == 0) {
            sb.append("Names        : - \n");
        } else {
            sb.append("Names        : " + partitionColumns.size() + "\n");
            for (String name : partitionColumns) {
                sb.append(" -> " + name + "\n");
            }
        }

        sb.append("---\n" + getSql() + "\n");

        return sb.toString();
    }
}
