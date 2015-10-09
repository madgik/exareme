/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;


import madgik.exareme.common.app.engine.scheduler.elasticTree.TreeConstants;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author heraldkllapi
 */
public class ElasticTreeStatistics implements Serializable {

    // Global
    public int[] scheduledOpsPerLevel;
    public double totalCost;
    public double totalRevene;
    public int totalQueries;
    public int totalErrorQueries;

    // Window
    public double totalContainers;
    public double containersPerLevel[];
    public double[] cpuLoadVarPerLevel;
    public double cpuLoadPerLevel[];
    public double dataLoadPerLevel[];
    public double[] dataLoadVarPerLevel;
    public double windowCost;
    public double windowRevenue;

    // Maybe add over-comitted resources in different arrays

    // Current
    public int runningQueries;
    public int queuedQueries;

    public ElasticTreeStatistics() {
        scheduledOpsPerLevel = new int[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        containersPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        cpuLoadPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        cpuLoadVarPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        dataLoadPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
        dataLoadVarPerLevel = new double[TreeConstants.SETTINGS.MAX_TREE_HEIGHT];
    }

    @Override public boolean equals(Object obj) {
        if (obj instanceof ElasticTreeStatistics == false) {
            return false;
        }
        ElasticTreeStatistics other = (ElasticTreeStatistics) obj;
        if (Double.compare(totalContainers, other.totalContainers) != 0) {
            return false;
        }
        for (int level = 0; level < containersPerLevel.length; ++level) {
            if (Integer.compare(scheduledOpsPerLevel[level], other.scheduledOpsPerLevel[level]) != 0
                ||
                Double.compare(containersPerLevel[level], other.containersPerLevel[level]) != 0 ||
                Double.compare(cpuLoadPerLevel[level], other.cpuLoadPerLevel[level]) != 0 ||
                Double.compare(cpuLoadVarPerLevel[level], other.cpuLoadVarPerLevel[level]) != 0 ||
                Double.compare(dataLoadPerLevel[level], other.dataLoadPerLevel[level]) != 0 ||
                Double.compare(dataLoadVarPerLevel[level], other.dataLoadVarPerLevel[level]) != 0) {
                return false;
            }
        }
        return !(Double.compare(totalCost, other.totalCost) != 0 ||
            Double.compare(totalRevene, other.totalRevene) != 0 ||
            Double.compare(windowCost, other.windowCost) != 0 ||
            Double.compare(windowRevenue, other.windowRevenue) != 0 ||
            Integer.compare(totalQueries, other.totalQueries) != 0 ||
            Integer.compare(totalErrorQueries, other.totalErrorQueries) != 0 ||
            Integer.compare(runningQueries, other.runningQueries) != 0 ||
            Integer.compare(queuedQueries, other.queuedQueries) != 0);
    }

    @Override public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Arrays.hashCode(this.scheduledOpsPerLevel);
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.totalContainers) ^ (
            Double.doubleToLongBits(this.totalContainers) >>> 32));
        hash = 79 * hash + Arrays.hashCode(this.containersPerLevel);
        hash = 79 * hash + Arrays.hashCode(this.cpuLoadPerLevel);
        hash = 79 * hash + Arrays.hashCode(this.dataLoadPerLevel);
        return hash;
    }
}
