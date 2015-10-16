/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine;

import madgik.exareme.common.schema.Select;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * @author herald
 */
public class AdpDBSelectOperator implements Serializable {
    private static final long serialVersionUID = 1L;
    private final static Logger log = Logger.getLogger(AdpDBSelectOperator.class);
    private AdpDBOperatorType type = null;
    private Select query = null;
    private int serialNumber = -1;
    private int totalInputs = 0;
    // <table, <part, count>>
    private HashMap<String, HashMap<Integer, Integer>> inputs = null;
    private int totalOutputs = 0;
    // <table, <part, count>>
    private HashMap<String, HashMap<Integer, Integer>> outputs = null;

    public AdpDBSelectOperator(AdpDBOperatorType t, Select q, int serialNumber) {
        this.type = t;
        this.query = q;
        this.serialNumber = serialNumber;
        this.inputs = new HashMap<String, HashMap<Integer, Integer>>();
        this.outputs = new HashMap<String, HashMap<Integer, Integer>>();
    }

    public static BitSet findCommonPartitions(AdpDBSelectOperator from, AdpDBSelectOperator to,
        String table) {
        HashMap<Integer, Integer> out = from.outputs.get(table);
        HashMap<Integer, Integer> in = to.inputs.get(table);
        // Compute intersection
        BitSet result = new BitSet();
        for (Map.Entry<Integer, Integer> outEntry : out.entrySet()) {
            if (outEntry.getValue() == 0) {
                continue;
            }
            Integer inCount = in.get(outEntry.getKey());
            if (inCount == null || inCount == 0) {
                continue;
            }
            result.set(outEntry.getKey());
        }
        return result;
    }

    private static List<Integer> convert(HashMap<Integer, Integer> set) {
        List<Integer> result = new ArrayList<Integer>();
        for (Map.Entry<Integer, Integer> entry : set.entrySet()) {
            for (int c = 0; c < entry.getValue(); c++) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void addInput(String table, int partition) {
        addPartition(table, partition, inputs);
        totalInputs++;
    }

    public void addOutput(String table, int partition) {
        addPartition(table, partition, outputs);
        totalOutputs++;
    }

    public int addToInputsAllInputsOf(AdpDBSelectOperator other) {
        int added = 0;
        for (String table : other.inputs.keySet()) {
            HashMap<Integer, Integer> add = other.inputs.get(table);
            HashMap<Integer, Integer> parts = getOrCreate(table, inputs);
            for (Map.Entry<Integer, Integer> entry : add.entrySet()) {
                Integer current = parts.get(entry.getKey());
                if (current == null) {
                    current = 0;
                }
                parts.put(entry.getKey(), current + entry.getValue());
            }
            int addedCount = count(add);
            added += addedCount;
            totalInputs += addedCount;
        }
        return added;
    }

    public int addToInputsAllOutputsOf(AdpDBSelectOperator other) {
        int added = 0;
        for (String table : other.outputs.keySet()) {
            HashMap<Integer, Integer> add = other.outputs.get(table);
            HashMap<Integer, Integer> parts = getOrCreate(table, inputs);

            for (Map.Entry<Integer, Integer> entry : add.entrySet()) {
                Integer current = parts.get(entry.getKey());
                if (current == null) {
                    current = 0;
                }
                parts.put(entry.getKey(), current + entry.getValue());
            }

            int addedCount = count(add);
            added += addedCount;
            totalInputs += addedCount;
        }
        return added;
    }

    public void clearInputs(String table, AdpDBSelectOperator from) {
        HashMap<Integer, Integer> in = inputs.get(table);

        HashMap<Integer, Integer> fromOut = from.outputs.get(table);
        for (Map.Entry<Integer, Integer> entry : fromOut.entrySet()) {
            Integer count = in.get(entry.getKey());
            if (count == null) {
                count = 0;
            }
            if (count <= entry.getValue()) {
                totalInputs -= count;
                in.remove(entry.getKey());
            } else {
                totalInputs -= entry.getValue();
                in.put(entry.getKey(), count - entry.getValue());
            }
        }
    }

    public void clearOutputs(String table) {
        HashMap<Integer, Integer> out = outputs.remove(table);
        if (out != null) {
            for (Integer count : out.values()) {
                totalOutputs -= count;
            }
        }
    }

    public Collection<String> getInputTables() {
        return inputs.keySet();
    }

    public Collection<String> getOutputTables() {
        return outputs.keySet();
    }

    public List<Integer> getInputPartitions(String table) {
        return convert(inputs.get(table));
    }

    public int getInputNumOfPartitions(String table) {
        return count(inputs.get(table));
    }

    public List<Integer> getOutputPartitions(String table) {
        return convert(outputs.get(table));
    }

    public int getOutputNumOfPartitions(String table) {
        return count(outputs.get(table));
    }

    public int getTotalInputs() {
        return totalInputs;
    }

    public int getTotalOutputs() {
        return totalOutputs;
    }

    public Select getQuery() {
        return query;
    }

    public void setQuery(Select query) {
        this.query = query;
    }

    public AdpDBOperatorType getType() {
        return type;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void printStatistics(String operatorName) {
        log.debug("Operator : " + operatorName);
        log.debug("Type     : " + type);
        log.debug("Inputs   : " + inputs);
        log.debug("Outputs  : " + outputs);
        log.debug(query.toString());
    }

    private void addPartition(String table, int partition,
        HashMap<String, HashMap<Integer, Integer>> tablesIO) {
        HashMap<Integer, Integer> parts = getOrCreate(table, tablesIO);
        Integer currentCount = parts.get(partition);
        if (currentCount == null) {
            currentCount = 0;
        }
        parts.put(partition, currentCount + 1);
    }

    private int count(HashMap<Integer, Integer> parts) {
        if (parts == null) {
            return 0;
        }
        int count = 0;
        for (Integer c : parts.values()) {
            count += c;
        }
        return count;
    }

    private HashMap<Integer, Integer> getOrCreate(String table,
        HashMap<String, HashMap<Integer, Integer>> tablesIO) {
        HashMap<Integer, Integer> parts = tablesIO.get(table);
        if (parts == null) {
            parts = new HashMap<Integer, Integer>();
            tablesIO.put(table, parts);
        }
        return parts;
    }

    @Override public String toString() {
        return "AdpDBSelectOperator{" +
            "type=" + type +
            ", query=" + query +
            ", serialNumber=" + serialNumber +
            ", totalInputs=" + totalInputs +
            ", inputs=" + inputs +
            ", totalOutputs=" + totalOutputs +
            ", outputs=" + outputs +
            '}';
    }
}
