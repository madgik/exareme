/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system;


import madgik.exareme.common.app.engine.scheduler.elasticTree.system.data.Database;
import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.chart.BarChart;
import madgik.exareme.utils.histogram.Bucket;
import madgik.exareme.utils.histogram.Histogram;
import madgik.exareme.utils.histogram.partitionRule.PartitionClass;
import madgik.exareme.utils.histogram.partitionRule.PartitionConstraint;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * @author heraldkllapi
 */
public class Statistics {
    // Reporting
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    private static final String SEP = "\t";
    private final Database db;
    private final FinancialProperties finProps;
    private final ArrayList<Double> computeTimes;
    private final ArrayList<Double> wastedTimes;
    private final ArrayList<Double> cacheSize;
    private final ArrayList<Double> revenuePerDataflow;
    private final ArrayList<Double> execTimePerDataflow;
    // Global
    private int numContainers = 0;
    // Dataflows
    private int runningDataflows = 0;
    private int finishedDataflows = 0;
    private int errorDataflows = 0;
    private int queuedDataflows = 0;
    // Compute
    private double computeTime = 0;
    private double totalComputeTime = 0;
    private double totalWastedTime = 0;
    private double totalDataflowComputeTime = 0;
    private long totalContainerQuantaAllocated = 0;
    // Jobs
    private int totalFinishedJobs = 0;
    private int totalKilledJobs = 0;
    private int totalQuantumEndKilledJobs = 0;
    // Storage
    private double totalIOData = 0;
    // Cache efficiency
    private int cacheTotalRequestCount = 0;
    private int cacheHitCount = 0;
    private int cacheIntermediateCount = 0;
    private double cacheTotalRequestMB = 0.0;
    private double cacheHitMB = 0;
    private double cacheIntermediateSizeMB = 0.0;
    // Financial
    private double totalRevenue = 0.0;
    private double totalCost = 0.0;

    public Statistics(Database db, FinancialProperties finProps) {
        this.finProps = finProps;
        this.db = db;
        this.computeTimes = new ArrayList<>();
        this.wastedTimes = new ArrayList<>();
        this.cacheSize = new ArrayList<>();
        this.revenuePerDataflow = new ArrayList<>();
        this.execTimePerDataflow = new ArrayList<>();
    }

    public int getNumContainers() {
        return numContainers;
    }

    public void setNumContainers(int numContainers) {
        this.numContainers = numContainers;
    }

    public void setComputeTime(double computeTime) {
        this.computeTime = computeTime;
    }

    public void setTotalComputeTime(double totalComputeTime) {
        this.totalComputeTime = totalComputeTime;
    }

    public void addComputeTime(double time) {
        this.computeTimes.add(time);
    }

    public void setTotalWastedTime(double totalWastedTime) {
        this.totalWastedTime = totalWastedTime;
    }

    public void addWastedTime(double time) {
        this.wastedTimes.add(time);
    }

    public void setRunningDataflows(int runningDataflows) {
        this.runningDataflows = runningDataflows;
    }

    public void setTotalContainerQuantaAllocated(long totalContainerQuantaAllocated) {
        this.totalContainerQuantaAllocated = totalContainerQuantaAllocated;
    }

    public void setQueuedDataflows(int queuedDataflows) {
        this.queuedDataflows = queuedDataflows;
    }

    public void setFinishedDataflows(int finishedDataflows) {
        this.finishedDataflows = finishedDataflows;
    }

    public void setErrorDataflows(int errorDataflows) {
        this.errorDataflows = errorDataflows;
    }

    public void setTotalDataflowComputeTime(double totalDataflowComputeTime) {
        this.totalDataflowComputeTime = totalDataflowComputeTime;
    }

    public void setTotalData(double totalIOData) {
        this.totalIOData = totalIOData;
    }

    public void addContainerCacheSize(double size) {
        cacheSize.add(size);
    }

    public void setFinishedJobs(int finishedJobs) {
        this.totalFinishedJobs = finishedJobs;
    }

    public void setKilledJobs(int killedJobs) {
        this.totalKilledJobs = killedJobs;
    }

    public void setTotalQuantumEndKilledJobs(int totalQuantumEndKilledJobs) {
        this.totalQuantumEndKilledJobs = totalQuantumEndKilledJobs;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void addDataflowRevenue(double revenue, double execTime) {
        this.revenuePerDataflow.add(revenue);
        this.execTimePerDataflow.add(execTime);
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public void setCacheHitCount(int cacheHitCount) {
        this.cacheHitCount = cacheHitCount;
    }

    public void setCacheHitMB(double cacheHitMB) {
        this.cacheHitMB = cacheHitMB;
    }

    public void setCacheTotalRequestCount(int cacheTotalRequestCount) {
        this.cacheTotalRequestCount = cacheTotalRequestCount;
    }

    public void setCacheTotalRequestMB(double cacheTotalRequestMB) {
        this.cacheTotalRequestMB = cacheTotalRequestMB;
    }

    public void setCacheIntermediateCount(int cacheIntermediateCount) {
        this.cacheIntermediateCount = cacheIntermediateCount;
    }

    public void setCacheIntermediateSizeMB(double cacheIntermediateSizeMB) {
        this.cacheIntermediateSizeMB = cacheIntermediateSizeMB;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Containers: " + SEP + numContainers + "\n");
        sb.append("Tables: " + SEP + db.getNumTables() + "\n");
        sb.append("Total Table Size: " + SEP + FORMAT.format(db.getTotalTableSize()) + "\n");
        sb.append("\n");

        sb.append("Queued Dataflows: " + SEP + queuedDataflows + "\n");
        sb.append("Running Dataflows: " + SEP + runningDataflows + "\n");
        sb.append("Finished Dataflows: " + SEP + finishedDataflows + "\n");
        sb.append("Error Dataflows: " + SEP + errorDataflows + "\n");
        sb.append("Total Dataflow Time: " + SEP + FORMAT.format(totalDataflowComputeTime) + "\n");
        sb.append("\n");

        sb.append("Finished Jobs: " + SEP + totalFinishedJobs + "\n");
        sb.append("Killed Jobs: " + SEP + totalKilledJobs + "\n");
        sb.append("Quantum End Killed Jobs: " + SEP + totalQuantumEndKilledJobs + "\n");
        sb.append("\n");

        sb.append("Compute Time: " + SEP + FORMAT.format(computeTime) + "\n");
        sb.append("Total Compute Time: " + SEP + FORMAT.format(totalComputeTime) + "\n");
        sb.append("Total Compute Time Cost: " +
            SEP + FORMAT.format(totalComputeTime * finProps.timeQuantumCost) + "\n");
        sb.append("Total Wasted Time: " + SEP + FORMAT.format(totalWastedTime) + "\n");
        sb.append("\n");
        sb.append("Percentage Wasted Time: " + SEP + FORMAT
            .format(100.0 * totalWastedTime / (totalWastedTime + totalComputeTime)) + " %\n");
        //    sb.append("Money: " + df.format(totalMonetaryCost) + "\n");
        //    sb.append("Wasted Money: " + df.format(totalWastedMoney) + "\n");
        sb.append("Total IO Data: " + SEP + FORMAT.format(totalIOData) + "\n");
        sb.append("\n");

        sb.append("Cache Total: " + SEP + cacheTotalRequestCount + "\n");
        sb.append("Cache Hit: " + SEP + cacheHitCount + "\n");
        sb.append(
            "Cache Hit %: " + SEP + FORMAT.format(100.0 * cacheHitCount / cacheTotalRequestCount)
                + "\n");
        sb.append("Cache Total MB: " + SEP + FORMAT.format(cacheTotalRequestMB) + "\n");
        sb.append("Cache Hit MB: " + SEP + FORMAT.format(cacheHitMB) + "\n");
        sb.append("Cache Hit MB %: " + SEP + FORMAT.format(100.0 * cacheHitMB / cacheTotalRequestMB)
            + "\n");
        sb.append("Cache Intermediate Count: " + SEP + cacheIntermediateCount + "\n");
        sb.append("Cache Intermediate MB: " + SEP + FORMAT.format(cacheIntermediateSizeMB) + "\n");
        sb.append("\n");

        sb.append("Total Revenue: " + SEP + totalRevenue + "\n");
        sb.append("Total Container Quanta: " + SEP + totalContainerQuantaAllocated + "\n");
        sb.append("Total Cost: " + SEP +
            (totalContainerQuantaAllocated * finProps.timeQuantumCost) + "\n");
        sb.append("\n");

        BarChart computeChart = new BarChart("Compute Time", 50);
        for (double v : computeTimes) {
            computeChart.add(v);
        }
        //    sb.append(computeChart.format());

        BarChart wastedChart = new BarChart("Wasted Time", 50);
        for (double v : wastedTimes) {
            wastedChart.add(v);
        }
        //    sb.append(wastedChart.format());

        BarChart cacheChart = new BarChart("Cache Sizes", 50);
        for (double v : cacheSize) {
            if (v > 0) {
                cacheChart.add(v);
            }
        }
        sb.append(cacheChart.format());

        reportDistribution("Exec", execTimePerDataflow, sb);
        reportDistribution("Revenue", revenuePerDataflow, sb);

        return sb.toString();
    }

    private void reportDistribution(String name, ArrayList<Double> values, StringBuilder sb) {
        DescriptiveStatistics execStats = new DescriptiveStatistics();
        ArrayList<Pair<?, Double>> data = new ArrayList<>(50);

        int id = 0;
        for (double v : values) {
            execStats.addValue(v);
            data.add(new Pair<>(id, v));
            id++;
        }
        sb.append(name + " Min-Max: " + SEP + FORMAT.format(execStats.getMin()) + SEP +
            FORMAT.format(execStats.getMax()) + "\n");
        sb.append(name + " Mean: " + SEP + FORMAT.format(execStats.getMean()) + "\n");
        sb.append(name + " p90: " + SEP + FORMAT.format(execStats.getPercentile(90)) + "\n");
        sb.append(name + " p95: " + SEP + FORMAT.format(execStats.getPercentile(95)) + "\n");
        sb.append(name + " p99: " + SEP + FORMAT.format(execStats.getPercentile(99)) + "\n");
        sb.append(name + " Stdev: " + SEP + FORMAT.format(execStats.getStandardDeviation()) + "\n");

        // Sort data
        Collections.sort(data, new Comparator<Pair<?, Double>>() {
            @Override public int compare(Pair<?, Double> o1, Pair<?, Double> o2) {
                return o1.b.compareTo(o2.b);
            }
        });

        try {
            Histogram execHistogram = new Histogram(
                new PartitionRule(PartitionClass.serial, PartitionConstraint.equi_width));
            LinkedList<Bucket> bucketList = execHistogram.createHistogram(data, 20);
            BarChart execHistogramChart = new BarChart(name + " Histogram", 50);
            for (Bucket b : bucketList) {
                DescriptiveStatistics bStats = b.getStatistics();
                if (bStats.getN() > 0) {
                    execHistogramChart.add(FORMAT.format(bStats.getMin()), b.data.size());
                } else {
                    execHistogramChart.add("-", b.data.size());
                }
            }
            sb.append(execHistogramChart.format());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
