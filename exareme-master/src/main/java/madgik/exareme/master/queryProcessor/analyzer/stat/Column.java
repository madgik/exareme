/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package madgik.exareme.master.queryProcessor.analyzer.stat;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author jim
 */
public class Column {
    // public static final int TEXT_LENGTH = 200; //text length in bytes for
    // estimation
    // public static final int NUM_LENGTH = 8; //num lenth in bytes for
    // estimaiton
    // public static final int VARCHAR_LENGTH = 45;

    private final String columnName;
    private final int columnType;
    private final int columnLength;
    private final int numberOfDiffValues;
    private final String minValue;
    private final String maxValue;
    private Map<String, Integer> diffValFreqMap;
    private Map<String, Double> hashStringMap = null;

    public Column(String columnName, int columnType, int columnSize, int numberOfDiffValues,
        String minValue, String maxValue, Map<String, Integer> diffValFreqMap) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnLength = columnSize;
        this.numberOfDiffValues = numberOfDiffValues;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.diffValFreqMap = diffValFreqMap;

        if (StatUtils.isTextType(this.columnType)) {
            this.hashStringMap = new HashMap<String, Double>();
            for (String s : this.diffValFreqMap.keySet()) {
                this.hashStringMap.put(s, StatUtils.hashString(s));
            }

            Map<String, Integer> tempDiffFreqMap = new HashMap<String, Integer>();

            for (Entry<String, Double> e : this.hashStringMap.entrySet()) {
                if (!tempDiffFreqMap.containsKey(e.getValue().toString())) {
                    tempDiffFreqMap.put(e.getValue().toString(), diffValFreqMap.get(e.getKey()));
                } else {
                    int f = tempDiffFreqMap.get(e.getValue().toString());
                    tempDiffFreqMap
                        .put(e.getValue().toString(), f + diffValFreqMap.get(e.getKey()));
                }
            }

            // for(String s : this.diffValFreqMap.keySet())
            // tempDiffFreqMap.put(this.hashStringMap.get(s).toString(),
            // this.diffValFreqMap.get(s));

            this.diffValFreqMap = tempDiffFreqMap;
            // tempDiffFreqMap = null;
            this.hashStringMap = null; // free space
        }

    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnType() {
        return columnType;
    }

    public int getColumnLength() {
        return columnLength;
    }

    public int getNumberOfDiffValues() {
        return numberOfDiffValues;
    }

    public String getMinValue() {
        if (StatUtils.isTextType(this.columnType))
            return Double.toString(StatUtils.hashString(minValue));
        else
            return minValue;
    }

    public String getMaxValue() {
        if (StatUtils.isTextType(this.columnType))
            return Double.toString(StatUtils.hashString(maxValue));
        else
            return maxValue;
    }

    public Map<String, Integer> getDiffValFreqMap() {

        return diffValFreqMap;

    }

    @Override public String toString() {
        return "Column{" + "columnName=" + columnName + ", columnType=" + columnType
            + ", columnSize=" + columnLength + ", numberOfDiffValues=" + numberOfDiffValues
            + ", minValue=" + minValue + ", maxValue=" + maxValue + ", diffValFreqMap="
            + diffValFreqMap + '}';
    }

    public double getMinDouble() {
        if (this.columnType == Types.VARCHAR) {
            return StatUtils.hashString(minValue);
        } else
            return Double.parseDouble(minValue);
    }

    public double getMaxDouble() {
        if (this.columnType == Types.VARCHAR) {
            return StatUtils.hashString(maxValue);
        } else
            return Double.parseDouble(maxValue);
    }
}
