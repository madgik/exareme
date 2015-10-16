/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduler;

import madgik.exareme.common.optimizer.FinancialProperties;
import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.queryProcessor.optimizer.*;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.AssignmentResult;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.ScheduleEstimator;
import madgik.exareme.utils.check.Check;
import madgik.exareme.utils.serialization.SerializationUtil;
import madgik.exareme.utils.units.Metrics;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.*;

/**
 * @author herald
 */
public class SkyScheduler implements MultiObjectiveQueryScheduler {
    private static final Logger LOG = Logger.getLogger(SkyScheduler.class);
    protected ConcreteQueryGraph graph = null;
    protected AssignedOperatorFilter subgraphFilter = null;
    protected RunTimeParameters runTimeParams = null;
    protected FinancialProperties financialProps = null;
    protected int numOfContainers = 0;
    protected ArrayList<ContainerResources> containers = null;
    protected ContainerFilter containerFilter = null;
    protected Exception exception = null;
    protected PriorityQueue<ConcreteOperator> readyOps;
    protected BitSet readyOpsMask;
    protected BitSet assignedOperators;
    protected ArrayList<ScheduleEstimator> skylinePlans = new ArrayList<>();
    protected int skylinePlansToKeep = 100;

    public SkyScheduler() {
    }

    @Override public SolutionSpace callOptimizer(final ConcreteQueryGraph graph,
        final AssignedOperatorFilter subgraphFilter, final ArrayList<ContainerResources> containers,
        final ContainerFilter containerFilter, final RunTimeParameters runTimeParams,
        final FinancialProperties finProps) throws RemoteException {
        long startCPU = System.currentTimeMillis() / Metrics.MiliSec;
        if (containers.size() == 1) {
            this.onlyOneContainer(graph, subgraphFilter, containers, containerFilter, runTimeParams,
                finProps);
        } else {
            this.initialize(graph, subgraphFilter, containers, containerFilter, runTimeParams,
                finProps);
            this.createAssigments();
        }
        // Create solution space
        SolutionSpace space = new SolutionSpace();
        for (ScheduleEstimator plan : skylinePlans) {
            if (plan == null) {
                continue;
            }
            space.addResult(
                new SchedulingResult(containers.size(), runTimeParams, finProps, exception, plan));
        }
        long endCPU = System.currentTimeMillis() / Metrics.MiliSec;
        space.setOptimizationTime(endCPU - startCPU);
        return space;
    }

    public void onlyOneContainer(ConcreteQueryGraph graph, AssignedOperatorFilter subgraphFilter,
        ArrayList<ContainerResources> containers, ContainerFilter containerFilter,
        RunTimeParameters runTimeParams, FinancialProperties financialProps) {
        ScheduleEstimator plan = new ScheduleEstimator(graph, containers, runTimeParams);
        for (ConcreteOperator co : graph.getOperators()) {
            plan.addOperatorAssignment(co.opID, 0, graph);
        }
        skylinePlans = new ArrayList<>();
        skylinePlans.add(plan);
    }

    public void initialize(ConcreteQueryGraph graph, AssignedOperatorFilter subgraphFilter,
        ArrayList<ContainerResources> containers, ContainerFilter containerFilter,
        RunTimeParameters runTimeParams, FinancialProperties financialProps) {
        this.graph = graph;
        this.subgraphFilter = subgraphFilter;
        this.runTimeParams = runTimeParams;
        this.financialProps = financialProps;
        this.numOfContainers = containers.size();
        this.containers = new ArrayList<>(containers);
        this.containerFilter = containerFilter;
        this.exception = null;
        this.skylinePlans = new ArrayList<>();
        this.assignedOperators = new BitSet(graph.getMaxOpId());

        // Initialize with input operators
        this.readyOps = new PriorityQueue<>();
        this.readyOpsMask = new BitSet(graph.getMaxOpId());
        for (ConcreteOperator start : graph.getLeafOperators()) {
            this.readyOps.offer(start);
            this.readyOpsMask.set(start.opID);
        }
    }

    private void createAssigments() throws RemoteException {
        ScheduleEstimator firstPlan = new ScheduleEstimator(graph, containers, runTimeParams);
        skylinePlans.add(firstPlan);
        while (readyOpsMask.cardinality() > 0) {
            // Get the most expensive operator from the ready ones
            ConcreteOperator op = readyOps.poll();
            int nextOpID = op.opID;

            readyOpsMask.clear(nextOpID);
            ArrayList<WhatIfEstimation> allCandidates = new ArrayList<>();
            for (ScheduleEstimator p : skylinePlans) {
                if (p == null) {
                    continue;
                }
                getCandidateContainers(op, p, allCandidates);
            }
            skylinePlans.clear();

            // Compute new skyline
            ArrayList<WhatIfEstimation> skylineCandidates = computeSkyline(op, allCandidates);
            for (WhatIfEstimation est : skylineCandidates) {
                ScheduleEstimator newPlan = SerializationUtil.deepCopy(est.estimator);
                newPlan.addOperatorAssignment(op.opID, est.container, graph);
                assignedOperators.set(op.opID);
                skylinePlans.add(newPlan);
            }

            // Prune the skyline ...
            if (skylinePlans.size() > skylinePlansToKeep) {
                // Keep only some schedules in the skyline
                Collections.sort(skylinePlans, new Comparator<ScheduleEstimator>() {
                    @Override public int compare(ScheduleEstimator o1, ScheduleEstimator o2) {
                        return Double.compare(o1.getScheduleStatistics().getTimeInQuanta(),
                            o2.getScheduleStatistics().getTimeInQuanta());
                    }
                });
                int schedulesKept = 2;
                int windowSize =
                    (int) Math.ceil((skylinePlans.size() - 2.0) / (skylinePlansToKeep - 2.0));
                for (int p = 1; p < skylinePlans.size() - 1; ++p) {
                    if (p % windowSize != 0) {
                        skylinePlans.set(p, null);
                    } else {
                        ++schedulesKept;
                    }
                }
                Check.True(Math.abs(schedulesKept - skylinePlansToKeep) <= skylinePlansToKeep / 2,
                    "Error. Schedules kept: " + schedulesKept + " / " + skylinePlansToKeep);
            }

            // All the producers have to be assigned before the consumer.
            for (Link outLink : graph.getOutputLinks(op.opID)) {
                if (readyOpsMask.get(outLink.to.opID) || assignedOperators.get(outLink.to.opID)) {
                    continue;
                }
                boolean allAssigned = true;
                for (Link inLink : graph.getInputLinks(outLink.to.opID)) {
                    if (assignedOperators.get(inLink.from.opID) == false) {
                        allAssigned = false;
                        break;
                    }
                }
                if (allAssigned) {
                    readyOpsMask.set(outLink.to.opID);
                    readyOps.add(outLink.to);
                }
            }
        }
    }

    private ArrayList<WhatIfEstimation> computeSkyline(ConcreteOperator co,
        ArrayList<WhatIfEstimation> candidates) {
        // Sort by time breaking equality by sorting by money
        Collections.sort(candidates);

        // Keep only the skyline
        ArrayList<WhatIfEstimation> skyline = new ArrayList<>();
        WhatIfEstimation previous = null;
        for (WhatIfEstimation est : candidates) {
            if (previous == null) {
                skyline.add(est);
                previous = est;
                continue;
            }
            if (previous.time_SEC == est.time_SEC) {
                // Already sorted by money
                continue;
            }
            if (previous.moneyQuanta > est.moneyQuanta) {
                skyline.add(est);
                previous = est;
            }
        }
        return skyline;
    }

    private void getCandidateContainers(ConcreteOperator co, ScheduleEstimator plan,
        ArrayList<WhatIfEstimation> estimations) throws RemoteException {
        int containerNum = subgraphFilter.getOperatorAssignment(co.opID);
        if (containerNum < 0) {
            int[] end = plan.getContainerEnd();
            boolean assignedToEmpty = false;
            // Check every container if it can be assigned
            for (int i = 0; i < numOfContainers; ++i) {
                if (containerFilter.canUseContainer(i) == false) {
                    continue;
                }
                if (assignedToEmpty && end[i] == 0) {
                    continue;
                } else {
                    if (end[i] == 0) {
                        assignedToEmpty = true;
                    }
                }
                AssignmentResult result = plan.getAssignmentResult(co.opID, i, graph);
                estimations.add(new WhatIfEstimation(co.opID, i, result, plan));
            }
        } else {
            AssignmentResult result = plan.getAssignmentResult(co.opID, containerNum, graph);
            estimations.add(new WhatIfEstimation(co.opID, containerNum, result, plan));
        }
    }

    private static class WhatIfEstimation implements Comparable<WhatIfEstimation> {
        public final int opID;
        public final int container;
        public final AssignmentResult estimation;
        public final ScheduleEstimator estimator;
        public final int time_SEC;
        public final int moneyQuanta;
        public final int containersUsed;
        public final double fragmentation;

        public WhatIfEstimation(int opID, int container, AssignmentResult estimation,
            ScheduleEstimator estimator) {
            this.opID = opID;
            this.container = container;
            this.estimation = estimation;
            this.estimator = estimator;
            this.time_SEC = estimation.after.time_SEC;
            this.moneyQuanta = estimation.after.moneyQuanta;
            this.containersUsed = estimation.after.containersUsed;
            this.fragmentation = moneyQuanta - estimation.after.moneyNoFragmentation;
        }

        @Override public int compareTo(WhatIfEstimation other) {
            if (time_SEC == other.time_SEC) {
                if (moneyQuanta == other.moneyQuanta) {
                    if (containersUsed == other.containersUsed) {
                        // Keep the one with the least fragmentation
                        return Double.compare(fragmentation, other.fragmentation);
                    } else {
                        // Keep the one with the lest number of containers
                        return Integer.compare(containersUsed, other.containersUsed);
                    }
                } else {
                    // Order by money
                    return Integer.compare(moneyQuanta, other.moneyQuanta);
                }
            } else {
                // Order by time
                return Integer.compare(time_SEC, other.time_SEC);
            }
        }
    }
}
