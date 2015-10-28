/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated;


import madgik.exareme.common.app.engine.ExecuteQueryExitMessage;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.utils.check.Check;
import madgik.exareme.utils.elastic.tree.ElasticTreeUtils;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerStateElasticTree;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs.ContainerJobsEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs.ContainerJobsEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEvents;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorLinkEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Start;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author heraldkllapi
 */
public class OperatorElasticTreeTerminatedEventHandler
    implements ExecEngineEventHandler<OperatorTerminatedEvent> {
    public static final OperatorElasticTreeTerminatedEventHandler instance =
        new OperatorElasticTreeTerminatedEventHandler();
    private static final long serialVersionUID = 1L;
    private static final Logger log =
        Logger.getLogger(OperatorElasticTreeTerminatedEventHandler.class);

    @Override public void preProcess(OperatorTerminatedEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        //    log.info("PRE");
        if (event instanceof OperatorElasticTreeTerminatedEvent == false) {
            throw new RemoteException("Event not supported: " + event.getClass().getName());
        }

        PlanEventSchedulerStateElasticTree treeState =
            ((OperatorElasticTreeTerminatedEvent) event).elasticState;

        boolean hasTerminated = false;

        if (event.operatorID != null) {
            state.getStatistics().incrOperatorCompleted();
            ActiveOperator activeOperator = state.getActiveOperator(event.operatorID);
            if (activeOperator.operatorEntity.type == OperatorType.processing) {
                state.getStatistics().incrProcessingOperatorsCompleted();
                String opName = activeOperator.operatorEntity.operatorName.split("\\.")[0];
                state.getStatistics().error.remove(opName);
                state.getStatistics().running.remove(opName);
                state.getStatistics().completed.add(opName);
            } else {
                state.getStatistics().incrDataTransferCompleted();
            }
            OperatorGroup group = activeOperator.operatorGroup;
            ActiveOperatorGroup activeGroup =
                group.setTerminated(activeOperator.operatorEntity, false);
            activeOperator.exitCode = event.exidCode;
            activeOperator.exitMessage = event.exitMessage;
            activeOperator.exitDate = new Date();
            // Update container utilization
            if (activeOperator.operatorEntity.type == OperatorType.processing) {
                ExecutionPlan plan = activeGroup.planSession.getExecutionPlan();
                String containerName = activeOperator.operatorEntity.containerName;
                EntityName anyCont = plan.getContainer(containerName);
                EntityName realCont =
                    treeState.getRealContainer(state.getSessionID(), anyCont.getName());
                treeState.operatorFinished(state.getSessionID(),
                    activeOperator.operatorEntity.operatorName, realCont,
                    (ExecuteQueryExitMessage) event.exitMessage);
            }
            // Check if the group has terminated
            if ((activeGroup.hasError == false) && group.hasTerminated) {
                state.groupDependencySolver().setTerminated(group);
                // Close the container sessions
                IndependentEvents jobs = new IndependentEvents(state);
                state.eventScheduler.closeContainerSession(activeGroup.containerSessionID, jobs);
                state.eventScheduler.queueIndependentEvents(jobs);
            }
            // Check if the plan has terminated
            if (state.getTerminatedOperatorCount() == state.getStatistics()
                .totalProcessingOperators()) {
                // Notify and close the session
                IndependentEvents termJobs = new IndependentEvents(state);
                state.eventScheduler.planTerminated(termJobs);
                state.eventScheduler.queueIndependentEvents(termJobs);

                IndependentEvents closeJobs = new IndependentEvents(state);
                state.eventScheduler.closeSession(closeJobs);
                state.eventScheduler.queueIndependentEvents(closeJobs);

                hasTerminated = true;
            }
        }

        // Get the activated groups of operators
        HashMap<Long, OperatorGroup> groupMap = state.groupDependencySolver().getActivatedGroups();
        // Create the active groups
        LinkedList<ActiveOperatorGroup> activeGroups = new LinkedList<>();
        // Add the materialization operators (readers and writters)
        for (OperatorGroup group : groupMap.values()) {
            // Create the partial plan
            group.createPartialPlan();

            ActiveOperatorGroup activeGroup = group.createNewActiveGroup(
                state.eventScheduler.getPlanManager().createContainerSession(state.getSessionID()));

            activeGroups.add(activeGroup);
        }
        IndependentEvents addContainer = new IndependentEvents(state);

        HashMap<String, ContainerJobsEvent> createMap = new HashMap<>();
        IndependentEvents create = new IndependentEvents(state);

        HashMap<String, ContainerJobsEvent> linkMap = new HashMap<>();
        IndependentEvents links = new IndependentEvents(state);

        HashMap<String, ContainerJobsEvent> startMap = new HashMap<>();
        IndependentEvents start = new IndependentEvents(state);
        for (ActiveOperatorGroup activeGroup : activeGroups) {
            ExecutionPlan plan = activeGroup.planSession.getExecutionPlan();

            // Make the assignment of the operators to the containers
            for (OperatorEntity op : plan.iterateOperators()) {
                String containerName = op.containerName;
                EntityName anyCont = plan.getContainer(containerName);
                EntityName realCont =
                    treeState.getRealContainer(state.getSessionID(), anyCont.getName());
                if (op.type == OperatorType.processing) {
                    // Find the level and rank of the operator
                    int[] levelRank = new int[2];
                    ElasticTreeUtils.findProcOpLevelAndRank(op.operatorName, levelRank);

                    switch (levelRank[0]) {
                        case 0: { // Leaf
                            log.info("LEAF OP: (" + levelRank[1] + ") " + op.type + " :: "
                                + op.operatorName);
                            // Make the assignment of the operator
                            if (realCont != null) {
                                throw new RemoteException(
                                    "Internal Error: Leaf operators should not be assigned");
                            }
                            realCont =
                                treeState.getDataContainer(state.getSessionID(), levelRank[1]);
                            treeState
                                .addAnyContainer(state.getSessionID(), anyCont.getName(), realCont);
                            log.info(
                                "DATA CONT: " + containerName + "@" + anyCont + " -> " + realCont);
                            treeState
                                .scheduleOperator(state.getSessionID(), op.operatorName, realCont,
                                    0);
                            break;
                        }
                        case 1: // Internal
                            log.info("INTERNAL OP: " + op.type + " :: " + op.operatorName);
                            treeState
                                .scheduleOperator(state.getSessionID(), op.operatorName, realCont,
                                    1);
                            break;
                        case 2: // Root
                            treeState
                                .scheduleOperator(state.getSessionID(), op.operatorName, realCont,
                                    2);
                            log.info("ROOT OP: " + op.type + " :: " + op.operatorName);
                            break;
                        default: // ERROR
                            throw new RemoteException(
                                "Unsuported graph type: it should be a tree!");
                    }
                } else {
                    if (realCont == null) {
                        int[] levelRank = new int[2];
                        ElasticTreeUtils.findDataOpLevelAndRank(op.operatorName, levelRank);
                        log.info("DATA OP: (" + levelRank[0] + "," + levelRank[1] + ") " +
                            op.type + " :: " + op.operatorName);
                        Check.True(levelRank[0] > 0, "Level of unassigned data ops should be >= 1");
                        realCont = treeState.getContainer(state.getSessionID(), levelRank[0]);
                        treeState
                            .addAnyContainer(state.getSessionID(), anyCont.getName(), realCont);
                        log.info("CONTAINER: " + containerName + "@" + anyCont + " -> " + realCont);
                    }
                }
            }

            // Add the containers
            for (String containerName : plan.iterateContainers()) {
                EntityName anyContainer = plan.getContainer(containerName);
                EntityName realContainer =
                    treeState.getRealContainer(state.getSessionID(), anyContainer.getName());
                if (realContainer == null) {
                    throw new RemoteException(
                        "Internal Error: containers should be specified earlier.");
                }
                state.eventScheduler.addContainer(containerName, realContainer, addContainer);
            }
            // Schedule the operators
            for (OperatorEntity createOp : plan.iterateOperators()) {
                log.info("OPERATOR: " + createOp.type + " :: " + createOp.operatorName);
                ContainerJobsEvent e = ContainerJobsEventHandler
                    .getEvent(createOp.container.getName(), create, createMap, state);
                state.eventScheduler.createOperator(createOp, e);
            }
            for (OperatorLinkEntity link : plan.iterateOperatorLinks()) {
                ContainerJobsEvent e = ContainerJobsEventHandler
                    .getEvent(link.container.getName(), links, linkMap, state);
                state.eventScheduler.createLink(link, e);
            }
            // Schedule the start of the operators
            for (OperatorEntity createOp : plan.iterateOperators()) {
                ContainerJobsEvent e = ContainerJobsEventHandler
                    .getEvent(createOp.container.getName(), start, startMap, state);
                state.eventScheduler
                    .start(new Start(createOp.operatorName, createOp.containerName), e);
            }
        }
        if (activeGroups.size() > 0) {
            state.eventScheduler.queueIndependentEvents(addContainer);
            state.eventScheduler.queueIndependentEvents(create);
            state.eventScheduler.queueIndependentEvents(links);
            state.eventScheduler.queueIndependentEvents(start);
            log.info("ACTIVE STATES: " + treeState.getRunningDataflows());
        } else {
            if (hasTerminated == false) {
                state.eventScheduler.continueExecutionEvent(state.getSessionID());
            }
        }
    }

    @Override public void handle(OperatorTerminatedEvent event, EventProcessor proc)
        throws RemoteException {
        //    log.info("PROCESS");
        //
    }

    @Override public void postProcess(OperatorTerminatedEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        //    log.info("POST");
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
