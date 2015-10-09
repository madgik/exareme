/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.supplier;


import madgik.exareme.common.app.engine.scheduler.elasticTree.client.SLA;

/**
 * @author heraldkllapi
 */
public class TreeStats {
    public final SLA[] sla;
    public final double[] queriesPerSLA;
    public final double[] CPULoadPerLevel;
    public final double[] dataLoadPerLevel;
    public final double concurentQueries;
    public final int currentDataContainers;
    public final double[] currentContainersPerLevel;
    public final int totalDataParts;
    public final double partitionSize;
    public final double historyWindow;
    public final int replication;

    public TreeStats(SLA[] sla, double[] queriesPerSLA, double[] CPULoadPerLevel,
        double[] dataLoadPerLevel, double concurentQueries, int currentDataContainers,
        double[] currentContainersPerLevel, int totalDataParts, double partitionSize,
        int replication, double historyWindow) {
        this.sla = sla;
        this.queriesPerSLA = queriesPerSLA;
        this.CPULoadPerLevel = CPULoadPerLevel;
        this.dataLoadPerLevel = dataLoadPerLevel;
        this.concurentQueries = concurentQueries;
        this.currentDataContainers = currentDataContainers;
        this.currentContainersPerLevel = currentContainersPerLevel;
        this.totalDataParts = totalDataParts;
        this.partitionSize = partitionSize;
        this.historyWindow = historyWindow;
        this.replication = replication;
    }
}
