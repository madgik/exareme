/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.utils.collections.ListUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * @author herald
 * @since 1.0
 */
public class ConcreteOperator implements Serializable, Comparable<ConcreteOperator> {

    private static final long serialVersionUID = 1L;
    public int opID = -1;
    public String operatorName = null;
    public double runTime_SEC;
    public double cpuUtilization;
    public int memory_MB;
    public OperatorBehavior behavior = null;
    public ArrayList<LinkData> inputDataArray = null;
    public ArrayList<LinkData> outputDataArray = null;
    // Files
    public ArrayList<LocalFileData> inputFileDataArray = null;
    public ArrayList<LocalFileData> outputFileDataArray = null;
    // Files that are read from the storage service
    public ArrayList<LocalFileData> dataflowInputFiles = null;
    // Inputs
    LinkedHashSet<Link> inputLinks = null;
    // Outputs
    LinkedHashSet<Link> outputLinks = null;

    private ConcreteOperator() {
    }

    public ConcreteOperator(String operatorName, double runTime_SEC, double cpuUtilization,
        int memory_MB, OperatorBehavior behavior) {
        this.operatorName = operatorName;

        if (runTime_SEC < 0.0) {
            throw new IllegalArgumentException("runTime_SEC should not be negative");
        } else if (cpuUtilization < 0.0 && cpuUtilization > 1.0) {
            throw new IllegalArgumentException("cpuUtilization should be in the range [0, 1]");
        } else if (memory_MB < 0.0) {
            throw new IllegalArgumentException("memory_MB should not be negative");
        }

        this.runTime_SEC = runTime_SEC;
        this.cpuUtilization = cpuUtilization;
        this.memory_MB = memory_MB;
        this.behavior = behavior;

        this.inputLinks = new LinkedHashSet<>();
        this.inputDataArray = new ArrayList<>();

        this.outputLinks = new LinkedHashSet<>();
        this.outputDataArray = new ArrayList<>();

        this.inputFileDataArray = new ArrayList<>();
        this.outputFileDataArray = new ArrayList<>();

        this.dataflowInputFiles = new ArrayList<>();
    }

    public void addInputData(LinkData data) {
        inputDataArray.add(data);
    }

    public void addInputData(LinkData data, int index) {
        ListUtil.setItem(inputDataArray, index, data);
    }

    public LinkData getInputData(int index) {
        return inputDataArray.get(index);
    }

    public void addOutputData(LinkData data) {
        outputDataArray.add(data);
    }

    public void addOutputData(LinkData data, int index) {
        ListUtil.setItem(outputDataArray, index, data);
    }

    public LinkData getOutputData(int index) {
        return outputDataArray.get(index);
    }

    public void addFileInputData(LocalFileData data) {
        inputFileDataArray.add(data);
    }

    public void removeFileInputData(String name) {
        for (int i = 0; i < inputFileDataArray.size(); ++i) {
            if (inputFileDataArray.get(i).name.equals(name)) {
                inputFileDataArray.remove(i);
                return;
            }
        }
        throw new IllegalArgumentException("Input file not found: " + name);
    }

    public void addFileOutputData(LocalFileData data) {
        outputFileDataArray.add(data);
    }

    public void removeFileOutputData(String name) {
        for (int i = 0; i < outputFileDataArray.size(); ++i) {
            if (outputFileDataArray.get(i).name.equals(name)) {
                outputFileDataArray.remove(i);
                return;
            }
        }
        throw new IllegalArgumentException("Output file not found: " + name);
    }

    public void addDataflowInputFile(LocalFileData data) {
        dataflowInputFiles.add(data);
    }

    @Override public boolean equals(Object obj) {
        ConcreteOperator co = (ConcreteOperator) obj;
        return this.opID == co.opID;
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.opID;
        return hash;
    }

    public OperatorBehavior getBehavior() {
        return behavior;
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public int getMemory_MB() {
        return memory_MB;
    }

    public double getRunTime_SEC() {
        return runTime_SEC;
    }

    public String getName() {
        return this.operatorName;
    }

    @Override public int compareTo(ConcreteOperator other) {
        // Order by descending order
        return Double.compare(other.runTime_SEC, runTime_SEC);
    }

    @Override public String toString() {
        return opID + " - " + operatorName + " - " + runTime_SEC + " - " + memory_MB + " - "
            + behavior + " - ";
    }
}
