/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception;

import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.concreteOperator.DataTransferOperatorException;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs.ContainerJobsEvent;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containerJobs.ContainerJobsEventHandler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.independent.IndependentEvents;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Destroy;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author herald
 * @author Vaggelis
 */
public class OperatorExceptionEventHandler
    implements ExecEngineEventHandler<OperatorExceptionEvent> {
    public static final OperatorExceptionEventHandler instance =
        new OperatorExceptionEventHandler();
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(OperatorExceptionEventHandler.class);

    public OperatorExceptionEventHandler() {
    }

    @Override public void preProcess(OperatorExceptionEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        ActiveOperator activeOperator = state.getActiveOperator(event.operatorID);
        if (activeOperator.operatorEntity.type == OperatorType.processing) {
            String opName = activeOperator.operatorEntity.operatorName.split("\\.")[0];
            state.getStatistics().running.remove(opName);
            state.getStatistics().error.add(opName);
        }
        OperatorGroup group = activeOperator.operatorGroup;
        log.error("Exception: ", event.exception);
        int errorCnt = group.setError(activeOperator.operatorEntity, event.exception);
        // If is the first error - stop execution of the group
        HashMap<String, ContainerJobsEvent> jobsMap = new HashMap<String, ContainerJobsEvent>();
        IndependentEvents events = new IndependentEvents(state);
        if (errorCnt == 1) {
            // Stop & destroy the operators
            ActiveOperatorGroup activeGroup =
                group.opNameActiveGroupMap.get(activeOperator.objectName);
            for (OperatorEntity entity : activeGroup.planSession.getExecutionPlan()
                .iterateOperators()) {
                ContainerJobsEvent e = ContainerJobsEventHandler
                    .getEvent(entity.containerName, events, jobsMap, state);
                log.debug("Destroying operator: " + entity.operatorName + " at container: "
                    + entity.containerName);

                state.eventScheduler
                    .destroy(new Destroy(entity.operatorName, entity.containerName), e);
            }
            // Destroy the buffers
      /*for (BufferEntity buffer : activeGroup.planSession.getExecutionPlan().iterateBuffers()) {
        ContainerJobsEvent e = ContainerJobsEventHandler.getEvent(
            buffer.containerName, events, jobsMap, state);
        state.eventScheduler.destroy(new Destroy(buffer.bufferName, buffer.containerName), e);
      }*/
            state.eventScheduler.queueIndependentEvents(events);
            // Close the container sessions
            IndependentEvents close = new IndependentEvents(state);
            state.eventScheduler.closeContainerSession(activeGroup.containerSessionID, close);
            state.eventScheduler.queueIndependentEvents(close);
            // TODO(herald): check the retry policy
            group.timesFailed++;
            if (state.retryPolicy.retry(event.exception, group.timesFailed)) {
                // Re-schedule operator group
                log.debug("Operator " + activeOperator.operatorEntity.operatorName
                    + " failed, retrying...");
                if (activeOperator.operatorEntity.type.equals(OperatorType.dataTransfer)
                    && (event.exception.getCause() instanceof DataTransferOperatorException)) {
                    log.debug("Data transfer exception");
                    DataTransferOperatorException ex =
                        (DataTransferOperatorException) event.exception.getCause();
                    for (String outOperator : ex.getFailedOut()) {
                        log.debug("Out " + outOperator + " failed");
                    }
                    log.debug("Open File Descriptors count: " + ex.getOpenFileDescCount());
                    for (OperatorEntity op : group.operatorMap.values()) {
                        Iterator<String> it = op.linksparams.keySet().iterator();
                        while (it.hasNext()) {
                            String outOp = it.next();
                            if (!ex.getFailedOut().contains(outOp)) {
                                it.remove();
                            }
                        }

                    }
                    state.groupDependencySolver().rescheduleDTGroup(group);
                } else {
                    state.groupDependencySolver().rescheduleGroup(group);
                }

                state.eventScheduler.continueExecutionEvent(state.getSessionID());
            } else {
                // Fail: stop excecution
                log.debug("Operator " + activeOperator.operatorEntity.operatorName
                    + " failed, destroying Plan...");
                state.getPlanSession().getPlanSessionStatus()
                    .operatorException(activeOperator.operatorEntity.operatorName, event.exception,
                        new Date());
                state.eventScheduler.destroyPlanWithError();
            }
        }
        state.getStatistics().incrOperatorsError();
    }

    @Override public void handle(OperatorExceptionEvent event, EventProcessor proc)
        throws RemoteException {
    }

    @Override public void postProcess(OperatorExceptionEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
