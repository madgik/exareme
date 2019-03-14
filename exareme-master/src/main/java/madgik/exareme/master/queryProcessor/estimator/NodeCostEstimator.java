/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.estimator;

import madgik.exareme.master.queryProcessor.decomposer.dag.Node;
import madgik.exareme.master.queryProcessor.decomposer.query.Column;
import madgik.exareme.master.queryProcessor.decomposer.query.NonUnaryWhereCondition;
import madgik.exareme.master.queryProcessor.estimator.metadata.Metadata;

import java.util.List;

import static madgik.exareme.master.queryProcessor.estimator.metadata.Metadata.NETWORK_RATE;

/**
 * @author jim
 */
public class NodeCostEstimator {

    private static final org.apache.log4j.Logger log =
            org.apache.log4j.Logger.getLogger(NodeCostEstimator.class);

    public static Double getCostForOperator(Node o, Node e) {
        if (o.getOpCode() == Node.JOIN) {
            try {
                NonUnaryWhereCondition nuwc = (NonUnaryWhereCondition) o.getObject();
                return estimateJoin(e, nuwc, o.getChildAt(0), o.getChildAt(1));
            } catch (Exception ex) {
                log.debug("Cannot get cost for join op " + o.toString() + ". Assuming dummy cost");
                return 1.0;
            }
        } else if (o.getOpCode() == Node.UNION) {
            try {
                return estimateUnion(o);
            } catch (Exception ex) {
                log.error("Cannot get cost for union op " + o.toString() + ". Assuming dummy cost");
                return 1.0;
            }
        } else if (o.getOpCode() == Node.PROJECT) {
            return estimateProjection(e);
        } else if (o.getOpCode() == Node.SELECT) {
            return estimateFilter(e);
        } else {
            return 0.0;
        }
    }
    //private final NodeSelectivityEstimator selEstimator;

    /*constructor*/
    public NodeCostEstimator() {
        //this.selEstimator = new NodeSelectivityEstimator(schema);
    }

    /*interface methods*/
    public static double estimateBase(Node n) {

        return 0;
    }

    public static double estimateProjection(Node n) {

        return 0;
    }

    public static double estimateFilter(Node n) {

        return 0;
    }

    public static double estimateJoin(Node n, NonUnaryWhereCondition nuwc, Node left, Node right)
            throws Exception {

        double leftRelTuples = left.getNodeInfo().getNumberOfTuples();
        double leftRelSize = left.getNodeInfo().outputRelSize();
        double rightRelTuples = right.getNodeInfo().getNumberOfTuples();
        double rightRelSize = right.getNodeInfo().outputRelSize();

        //        double childrenMaxResponseTime = Math.max(leftRelSize, rightRelSize);
        double responseTime = localJoinProcessingTime(leftRelTuples, leftRelSize, rightRelTuples,
                rightRelSize);// + childrenMaxResponseTime;
        //this.planInfo.get(n.getHashId()).setResponseTimeEstimation(responseTime);
        if (Double.isNaN(responseTime)) {
            throw new Exception("NaN");
        }

        return responseTime;
    }

    public static double estimateRepartition(Node n, Column partitioningCol) {
        //this.planInfo.put(n.getHashId(), new NodeInfo());
        //this.selEstimator.estimateRepartition(n, partitioningCol, child);

        try {
            double relTuples = n.getNodeInfo().getNumberOfTuples();
            double relSize = n.getNodeInfo().outputRelSize();

            double responseTime = repartition(relSize, Metadata.NUMBER_OF_VIRTUAL_MACHINES,
                    Metadata.NUMBER_OF_VIRTUAL_MACHINES);
            responseTime += localHashingTime(relTuples, relSize);
            responseTime += localUnionTime(relSize);

            //this.planInfo.get(n.getHashId()).setResponseTimeEstimation(responseTime);
            if (Double.isNaN(responseTime)) {
                throw new Exception("NaN");
            }
            return responseTime;

        } catch (Exception ex) {
            log.debug("Cannot get cost for repartition op " + partitioningCol.getName() + ". Assuming dummy cost");
            return 1.5;

        }

    }

    public static double estimateReplication(double data, int replicas) {
        return ((data / Metadata.PAGE_SIZE) * Metadata.PAGE_IO_TIME) * replicas + replicas * (data
                / NETWORK_RATE);
    }

    public static double estimateUnion(Node n) {
        //this.planInfo.put(n.getHashId(), new NodeInfo());
        //this.selEstimator.estimateUnion(n);


        List<Node> children = n.getChildren();

        double totalResponseTimeCost = 0;
        double childResponseTimeCost = 0;
        for (Node cn : children) {
            childResponseTimeCost = cn.getNodeInfo().getResponseTimeEstimation();
            totalResponseTimeCost += childResponseTimeCost;
        }

        //this.planInfo.get(n.getHashId()).setResponseTimeEstimation(maxResponseTimeCost);

        return totalResponseTimeCost;
    }

    /*private-helper methods*/
    //estimation model      
    private static double repartition(double relSize, int fromNumOfPartitions,
                                      int toNumOfPartitions) {
        return (relSize * (1 / fromNumOfPartitions)) / (NETWORK_RATE / fromNumOfPartitions);
    }

    private static double localUnionTime(double dataPortion) {
        return (dataPortion / Metadata.PAGE_SIZE) * Metadata.PAGE_IO_TIME;
    }

    //TODO: relSize as argument?? 10 mb/sec => 1 tuple->8bytes(for numeric) thus: (10*2^20)/8 tuples/sec = 1310720 tuples/sec thus for 1 tuple : 0.000000763 sec
    private static double localHashingTime(double relTuples, double relSize) {
        return relTuples
                * 0.000034;        //time for a tuple hushing: 0.000034 sec (disk io + cpu time included)
    }

    private static double localJoinProcessingTime(double leftRelTuples, double leftRelSize,
                                                  double rightRelTuples, double rightRelSize) {
        double cpuLocalCost, diskLocalCost,
                smallRelTuples = leftRelTuples, bigRelTuples = rightRelTuples,
                smallRelSize = leftRelSize, bigRelSize = rightRelSize;

        if (rightRelTuples < leftRelTuples) {
            smallRelTuples = rightRelTuples;
            smallRelSize = rightRelSize;
            bigRelSize = rightRelSize;
            bigRelTuples = leftRelTuples;
        }

        //disk cost
        //->index construcrion, scanning the smallest tule table
        double diskSmallRelIndexConstruction =
                (smallRelSize / Metadata.PAGE_SIZE) * Metadata.PAGE_IO_TIME;
        double diskBigRelScan = (bigRelSize / Metadata.PAGE_SIZE) * Metadata.PAGE_IO_TIME;
        diskLocalCost = diskSmallRelIndexConstruction + diskBigRelScan;

        //cpu cost
        double smallRelTuples_log10 = Math.log10(smallRelTuples);
        double localIndexConstruction =
                smallRelTuples * smallRelTuples_log10 * Metadata.CPU_CYCLE_TIME;
        double localComparisons = bigRelTuples * smallRelTuples_log10 * Metadata.CPU_CYCLE_TIME;
        cpuLocalCost = localIndexConstruction + localComparisons;

        return diskLocalCost + cpuLocalCost;
    }
}
