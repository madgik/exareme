/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated;

import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.executionEngine.Exception.NotEnoughResourcesException;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs.ContainerJobsEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs.ContainerJobsEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEvents;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorLinkEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import madgik.exareme.worker.art.executionPlan.parser.expression.Start;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * @author herald
 */
public class OperatorGroupTerminatedEventHandler
    implements ExecEngineEventHandler<OperatorTerminatedEvent> {

    private static final long serialVersionUID = 1L;
    public static final OperatorGroupTerminatedEventHandler instance =
        new OperatorGroupTerminatedEventHandler();
    private static final Logger log = Logger.getLogger(OperatorGroupTerminatedEventHandler.class);

    public OperatorGroupTerminatedEventHandler() {
    }

    @Override public void preProcess(OperatorTerminatedEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        // Set terminated
        if (event.operatorID != null) {
            log.trace("OperatorTerminated: " + event.operatorID.operatorName);
            state.getStatistics().incrOperatorCompleted();
            ActiveOperator activeOperator = state.getActiveOperator(event.operatorID);
            if (event.terminateGroup
                || activeOperator.operatorEntity.type == OperatorType.processing) {
                if (activeOperator.operatorEntity.type == OperatorType.dataTransfer) {
                    state.getStatistics().incrDataTransferCompleted();
                    log.trace("IncrDTC: " + activeOperator.operatorEntity.operatorName);
                } else {
                    state.getStatistics().incrProcessingOperatorsCompleted();
                }
                String opName = activeOperator.operatorEntity.operatorName.split("\\.")[0];
                state.getStatistics().error.remove(opName);
                state.getStatistics().running.remove(opName);
                state.getStatistics().completed.add(opName);
            } else {
                state.getStatistics().incrDataTransferCompleted();
            }
            OperatorGroup group = activeOperator.operatorGroup;
            ActiveOperatorGroup activeGroup =
                group.setTerminated(activeOperator.operatorEntity, event.terminateGroup);
            activeOperator.exitCode = event.exidCode;
            activeOperator.exitMessage = event.exitMessage;
            activeOperator.exitDate = new Date();
            // Check if the group has terminated
            if ((activeGroup.hasError == false) && group.hasTerminated) {
                log.trace("Operator Group Terminated: " + group.toString());
                state.groupDependencySolver().setTerminated(group);
                // Close the container sessions
                IndependentEvents jobs = new IndependentEvents(state);
                state.eventScheduler.closeContainerSession(activeGroup.containerSessionID, jobs);
                state.eventScheduler.queueIndependentEvents(jobs);
            }
            // Check if the plan has terminated
            log.debug("Terminated: " + state.getTerminatedOperatorCount() + " Expecting: " + state
                .getStatistics().totalProcessingOperators());
            if (state.getTerminatedOperatorCount() == state.getStatistics()
                .totalProcessingOperators()) {

                IndependentEvents termJobs = new IndependentEvents(state);
                state.eventScheduler.planTerminated(termJobs);
                state.eventScheduler.queueIndependentEvents(termJobs);

                IndependentEvents closeJobs = new IndependentEvents(state);
                state.eventScheduler.closeSession(closeJobs);
                state.eventScheduler.queueIndependentEvents(closeJobs);
            }
        }

        log.debug("Checking queued terminated ops ...");
        try {
            event.scheduler.lock.lock();
            log.debug("Queued Finished: " + event.scheduler.terminatedActiveEvents);
            event.scheduler.terminatedActiveEvents--;
            if (event.scheduler.terminatedActiveEvents < 0) {
                event.scheduler.terminatedActiveEvents = 0;
            }
            // TODO(herald): fix the following
            //               maybe the problem is the reentrant lock of the scheduler :-)
            //      if (event.scheduler.terminatedActiveEvents > 10) {
            //        return;
            //      }
        } finally {
            event.scheduler.lock.unlock();
        }

        try {
            state.eventScheduler.lock.lock();
            // Get the activated groups of operators
            HashMap<Long, OperatorGroup> groupMap =
                state.groupDependencySolver().getActivatedGroups();
            if (groupMap == null && event.operatorID == null && !state.isTerminated()) {
                log.trace("Not enough resources to start plan.");
                state.getPlanSession().getPlanSessionStatus().planInstantiationException(
                    new NotEnoughResourcesException("Cannot execute query"), new Date());
                state.eventScheduler.destroyPlanWithError(state.getPlanSessionID());
            } else if (groupMap == null) {
                groupMap = new LinkedHashMap<Long, OperatorGroup>();
            }
            // Create the active groups
            LinkedList<ActiveOperatorGroup> activeGroups = new LinkedList<ActiveOperatorGroup>();
            // Add the materialization operators (readers and writters)
            for (OperatorGroup group : groupMap.values()) {
                // Create the partial plan
                group.createPartialPlan();

                ActiveOperatorGroup activeGroup = group.createNewActiveGroup(
                    state.eventScheduler.getPlanManager()
                        .createContainerSession(state.getPlanSessionID()));

                activeGroups.add(activeGroup);
                log.debug(
                    "GROUP: " + activeGroup.containerSessionID.getLongId() + " activeGroupId: "
                        + activeGroup.activeGroupId + " OPS: " + activeGroup.planSession
                        .getExecutionPlan().getOperatorCount());
            }

            IndependentEvents addContainer = new IndependentEvents(state);

            HashMap<String, ContainerJobsEvent> createMap =
                new HashMap<String, ContainerJobsEvent>();
            IndependentEvents create = new IndependentEvents(state);

            HashMap<String, ContainerJobsEvent> linkMap = new HashMap<String, ContainerJobsEvent>();
            IndependentEvents links = new IndependentEvents(state);

            HashMap<String, ContainerJobsEvent> startMap =
                new HashMap<String, ContainerJobsEvent>();
            IndependentEvents start = new IndependentEvents(state);

            for (ActiveOperatorGroup activeGroup : activeGroups) {
                ExecutionPlan plan = activeGroup.planSession.getExecutionPlan();
                // Add the containers
                for (String containerName : plan.iterateContainers()) {
                    state.eventScheduler
                        .addContainer(containerName, plan.getContainer(containerName),
                            addContainer);
                }
                // Schedule the operators
                for (OperatorEntity createOp : plan.iterateOperators()) {
                    createOp.paramList.add(
                        new Parameter("OpsInGroup", Integer.toString(plan.getOperatorCount())));
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

            state.eventScheduler.queueIndependentEvents(addContainer);
            state.eventScheduler.queueIndependentEvents(create);
            state.eventScheduler.queueIndependentEvents(links);
            state.eventScheduler.queueIndependentEvents(start);

        } finally {
            state.eventScheduler.lock.unlock();
        }
    }

    @Override public void handle(OperatorTerminatedEvent event, EventProcessor proc)
        throws RemoteException {
    }

    @Override public void postProcess(OperatorTerminatedEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }



    private void queueIndependentEvents(IndependentEvents jobs, boolean b) {
        throw new UnsupportedOperationException(
            "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
