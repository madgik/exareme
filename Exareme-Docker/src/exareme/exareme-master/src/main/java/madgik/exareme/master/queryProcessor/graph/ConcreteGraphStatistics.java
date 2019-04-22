/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.graph;

import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.utils.check.Check;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author heraldkllapi
 */
public class ConcreteGraphStatistics {
    private final ConcreteQueryGraph graph;
    private final RunTimeParameters runTime;
    private double minWork = 0.0;
    private double maxWork = 0.0;
    private double minSpan = 0.0;
    private double maxSpan = 0.0;

    public ConcreteGraphStatistics(ConcreteQueryGraph graph, RunTimeParameters runTime) {
        this.graph = graph;
        this.runTime = runTime;
        computeStats();
    }

    private void computeStats() {
        computeWork();
        workComputeSpan();
    }

    private void computeWork() {
        // Compute work
        for (ConcreteOperator co : graph.getOperators()) {
            minWork += co.runTime_SEC;
            for (LocalFileData file : co.inputFileDataArray) {
                minWork += file.size_MB / runTime.disk_throughput__MB_SEC;
            }
            for (LocalFileData file : co.outputFileDataArray) {
                minWork += file.size_MB / runTime.disk_throughput__MB_SEC;
            }
        }
        maxWork = minWork;
        for (Link link : graph.getLinks()) {
            maxWork += link.data.size_MB / runTime.network_speed__MB_SEC;
        }
        minWork /= runTime.quantum__SEC;
        maxWork /= runTime.quantum__SEC;
    }

    private void workComputeSpan() {
        int numOps = graph.getNumOfOperators();
        BitSet visited = new BitSet();
        HashMap<Integer, double[]> minMaxSpan = new HashMap<Integer, double[]>(numOps);
        LinkedList<ConcreteOperator> available = new LinkedList<ConcreteOperator>();
        // Find the first available operators
        for (ConcreteOperator co : graph.getOperators()) {
            if (co == null) {
                continue;
            }
            if (co.inputLinks.isEmpty()) {
                available.add(co);
            }
        }
        while (available.isEmpty() == false) {
            ConcreteOperator next = available.remove();
            visited.set(next.opID);
            // Compute span
            double[] span = minMaxSpan.get(next.opID);
            if (span == null) {
                span = new double[2];
                minMaxSpan.put(next.opID, span);
            }
            span[0] = 0.0;
            span[1] = 0.0;
            double netIOTime = 0.0;
            for (Link link : next.inputLinks) {
                netIOTime += link.data.size_MB / runTime.network_speed__MB_SEC;
                double[] parent = minMaxSpan.get(link.from.opID);
                if (span[1] < parent[1]) {
                    span[0] = parent[0];
                    span[1] = parent[1];
                }
            }
            // Compute total disk io
            double diskIOTime = 0.0;
            for (LocalFileData file : next.inputFileDataArray) {
                diskIOTime += file.size_MB / runTime.disk_throughput__MB_SEC;
            }
            for (LocalFileData file : next.outputFileDataArray) {
                diskIOTime += file.size_MB / runTime.disk_throughput__MB_SEC;
            }
            span[0] += next.runTime_SEC + diskIOTime;
            span[1] += next.runTime_SEC + diskIOTime + netIOTime;

            // Add the activated operators
            for (Link link : next.outputLinks) {
                ConcreteOperator to = link.to;
                boolean allActive = true;
                for (Link from : to.inputLinks) {
                    if (visited.get(from.from.opID) == false) {
                        allActive = false;
                        break;
                    }
                }
                if (allActive) {
                    available.add(to);
                }
            }
        }
        Check.True(visited.cardinality() == numOps,
                "Not all operators are examined: " + visited.cardinality() + " / " + numOps);
        for (double[] span : minMaxSpan.values()) {
            if (maxSpan < span[1]) {
                minSpan = span[0] / runTime.quantum__SEC;
                maxSpan = span[1] / runTime.quantum__SEC;
            }
        }
    }

    public double getMinWork() {
        return minWork;
    }

    public double getMaxWork() {
        return maxWork;
    }

    public double getMinSpan() {
        return minSpan;
    }

    public double getMaxSpan() {
        return maxSpan;
    }

    public double getMinP() {
        return minWork / maxSpan;
    }

    public double getMaxP() {
        return maxWork / minSpan;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return "Work : " + df.format(minWork) + " - " + df.format(maxWork) + "\n" +
                "Span : " + df.format(minSpan) + " - " + df.format(maxSpan) + "\n" +
                "P    : " + df.format(minWork / maxSpan) + " - " + df.format(maxWork / minSpan);
    }
}
