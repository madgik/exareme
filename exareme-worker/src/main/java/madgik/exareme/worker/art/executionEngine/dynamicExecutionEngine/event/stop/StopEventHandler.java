/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.stop;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.job.StopOperatorJob;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.worker.art.executionPlan.SemanticError;
import madgik.exareme.worker.art.executionPlan.entity.StopEntity;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class StopEventHandler implements ExecEngineEventHandler<StopEvent> {
    public static final StopEventHandler instance = new StopEventHandler();
    private static final long serialVersionUID = 1L;

    public StopEventHandler() {
    }

    @Override public void preProcess(StopEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        try {
            String operatorName =
                (event.stop != null) ? event.stop.operatorName : event.stopEntity.operatorName;
            ActiveOperator activeOperator = state.getActiveOperator(operatorName);
            ActiveOperatorGroup activeGroup = activeOperator.operatorGroup.objectNameActiveGroupMap
                .get(activeOperator.objectName);
            StopEntity stopEntity = event.stopEntity;
            if (stopEntity == null) {
                stopEntity =
                    activeGroup.planSession.getExecutionPlan().createStopEntity(event.stop);
            }
            ContainerSessionID containerSessionID = activeGroup.containerSessionID;
            event.session = state.getContainerSession(stopEntity.containerName, containerSessionID);
            ConcreteOperatorID opID =
                activeGroup.planSession.getOperatorIdMap().get(stopEntity.operatorEntity);
            event.jobs = new ContainerJobs();
            event.jobs.addJob(new StopOperatorJob(opID));
        } catch (SemanticError e) {
            throw new RemoteException("Cannot handle stop", e);
        }
    }

    @Override public void handle(StopEvent event, EventProcessor proc) throws RemoteException {
        event.results = event.session.execJobs(event.jobs);
        event.messageCount = 1;
    }

    @Override public void postProcess(StopEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
