/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator;

import madgik.exareme.common.optimizer.RunTimeParameters;
import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;
import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.Link;
import madgik.exareme.master.queryProcessor.graph.LocalFileData;
import madgik.exareme.master.queryProcessor.optimizer.ContainerResources;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional.TransactionalBitSet;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional.TransactionalIntArray;
import madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional.TransactionalInteger;
import madgik.exareme.master.queryProcessor.optimizer.scheduler.OperatorAssignment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author herald
 * @since 1.0
 */
public class ScheduleEstimator implements Serializable {
    private static final long serialVersionUID = 1L;

    private final RunTimeParameters params;

    private final ArrayList<OperatorAssignment> assignments;
    private final ArrayList<ActiveOperator> activeAssignments;
    private final ArrayList<ActiveContainer> activeContainers;

    private final TransactionalBitSet containerUsed;
    private final TransactionalIntArray containerEnd_SEC;
    private final TransactionalInteger scheduleTime_SEC;

    public ScheduleEstimator(ConcreteQueryGraph graph, ArrayList<ContainerResources> containers,
        RunTimeParameters runTimeParams) {
        // Initialize the active structures
        this.params = runTimeParams;
        assignments = new ArrayList<>(graph.getMaxOpId());
        activeAssignments = new ArrayList<>(graph.getMaxOpId());
        for (int i = 0; i < graph.getMaxOpId(); ++i) {
            assignments.add(null);
            activeAssignments.add(new ActiveOperator());
        }
        activeContainers = new ArrayList<>(containers.size());
        for (int i = 0; i < containers.size(); ++i) {
            activeContainers.add(new ActiveContainer(runTimeParams));
        }
        containerEnd_SEC = new TransactionalIntArray(containers.size());
        containerUsed = new TransactionalBitSet(containers.size());
        scheduleTime_SEC = new TransactionalInteger(0);
    }

    public void addOperatorAssignment(int opID, int container, ConcreteQueryGraph graph) {
        // Make changes
        addOperatorAssignmentInternal(opID, container, graph);
        commit();
    }

    public AssignmentResult getAssignmentResult(int opID, int container, ConcreteQueryGraph graph) {
        AssignmentResult result = new AssignmentResult();
        result.cNum = container;
        double[] moneyNoFrag = new double[2];

        // Get previous values
        computeMoney(moneyNoFrag);
        result.before.time_SEC = scheduleTime_SEC.getValue();
        result.before.moneyQuanta = (int) moneyNoFrag[0];
        result.before.moneyNoFragmentation = moneyNoFrag[1] / params.quantum__SEC;
        result.before.containersUsed = containerUsed.cardinality();

        // Add assignment
        addOperatorAssignmentInternal(opID, container, graph);

        // Get after values
        computeMoney(moneyNoFrag);
        result.after.time_SEC = scheduleTime_SEC.getValue();
        result.after.moneyQuanta = (int) moneyNoFrag[0];
        result.after.moneyNoFragmentation = moneyNoFrag[1] / params.quantum__SEC;
        result.after.containersUsed = containerUsed.cardinality();
        result.operatorDuration = graph.getOperator(opID).getRunTime_SEC();

        // Rollback
        rollback();

        // Remove assignement
        assignments.set(opID, null);

        return result;
    }

    public void addOperatorAssignmentInternal(int opID, int container, ConcreteQueryGraph graph) {
        // Add assignment
        ConcreteOperator cOp = graph.getOperator(opID);
        assignments.set(opID, new OperatorAssignment(cOp, container));

        // Get active operator and container
        ActiveOperator aCOp = activeAssignments.get(opID);
        ActiveContainer aCont = activeContainers.get(container);

        int timeNow_SEC = 0;

        // Compute start based on the dependencies
        int depStart_SEC = 0;
        for (Link link : graph.getInputLinks(opID)) {
            int fromId = link.from.opID;
            ActiveOperator aFrom = activeAssignments.get(fromId);
            if (depStart_SEC < aFrom.end_SEC) {
                depStart_SEC = aFrom.end_SEC;
            }
        }
        depStart_SEC = Math.max(depStart_SEC, aCont.lastOpEnd_SEC.getValue());

        timeNow_SEC += depStart_SEC;
        // Operator started
        aCOp.start_SEC = timeNow_SEC;

        // Add network delay and use
        int networkDelay_SEC = 0;
        for (Link link : graph.getInputLinks(opID)) {
            int fromId = link.from.opID;
            OperatorAssignment from = assignments.get(fromId);
            if (from.container != container) {
                ActiveContainer fromACont = activeContainers.get(from.container);
                int dtTime_SEC = (int) Math.ceil(link.data.size_MB / params.network_speed__MB_SEC);

                // Set network usage to containers
                int dtStart = timeNow_SEC + networkDelay_SEC;
                int dtEnd = dtStart + dtTime_SEC;
                fromACont.setUse(dtStart, dtEnd);

                networkDelay_SEC += dtTime_SEC;
            }
        }
        timeNow_SEC += networkDelay_SEC;

        // Include input disk cost
        int inputDiskTime_SEC = 0;
        for (LocalFileData local : cOp.inputFileDataArray) {
            int diskTime = (int) Math.ceil(local.size_MB / params.disk_throughput__MB_SEC);
            inputDiskTime_SEC += diskTime;
        }
        timeNow_SEC += inputDiskTime_SEC;

        // Execute operator
        int runTime = (int) Math.ceil(cOp.getRunTime_SEC());
        timeNow_SEC += runTime;

        // Include output disk
        int outputDiskTime_SEC = 0;
        for (LocalFileData local : cOp.outputFileDataArray) {
            int diskTime = (int) Math.ceil(local.size_MB / params.disk_throughput__MB_SEC);
            outputDiskTime_SEC += diskTime;
        }
        timeNow_SEC += outputDiskTime_SEC;

        aCOp.end_SEC = timeNow_SEC;
        aCont.setUse(aCOp.start_SEC, aCOp.end_SEC);
        aCont.lastOpEnd_SEC.setValue(aCOp.end_SEC);

        containerEnd_SEC.set(container, aCOp.end_SEC);
        if (scheduleTime_SEC.getValue() < aCOp.end_SEC) {
            scheduleTime_SEC.setValue(aCOp.end_SEC);
        }
        containerUsed.set(container);
    }

    private void commit() {
        containerUsed.commit();
        scheduleTime_SEC.commit();
        containerEnd_SEC.commit();
        for (ActiveContainer ac : activeContainers) {
            ac.commit();
        }
    }

    private void rollback() {
        containerUsed.rollback();
        scheduleTime_SEC.rollback();
        containerEnd_SEC.rollback();
        for (ActiveContainer ac : activeContainers) {
            ac.rollback();
        }
    }

    private void computeMoney(double moneyNoFrag[]) {
        moneyNoFrag[0] = 0.0;
        moneyNoFrag[1] = 0.0;
        for (ActiveContainer ac : activeContainers) {
            moneyNoFrag[0] += ac.timeUsed_SEC.cardinality();
            moneyNoFrag[1] += ac.timeUsedNoFrag_SEC.cardinality();
        }
    }

    public ScheduleStatistics getScheduleStatistics() {
        double[] moneyNoFrag = new double[2];
        computeMoney(moneyNoFrag);
        return new ScheduleStatistics(activeContainers.size(), containerUsed.cardinality(),
            scheduleTime_SEC.getValue(), (double) scheduleTime_SEC.getValue() / params.quantum__SEC,
            (int) moneyNoFrag[0], moneyNoFrag[1] / params.quantum__SEC);
    }

    public List<OperatorAssignment> getAssignments() {
        return assignments;
    }

    public int[] getContainerEnd() {
        return containerEnd_SEC.getValue();
    }
}
