/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.start;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.job.StartOperatorJob;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class StartEventHandler implements ExecEngineEventHandler<StartEvent> {
    public static final StartEventHandler instance = new StartEventHandler();
    private static final long serialVersionUID = 1L;

    public StartEventHandler() {
    }

    @Override
    public void preProcess(StartEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        String operatorName =
                (event.start != null) ? event.start.operatorName : event.startEntity.operatorName;
        ActiveOperator activeOperator = state.getActiveOperator(operatorName);
        ActiveOperatorGroup activeGroup =
                activeOperator.operatorGroup.objectNameActiveGroupMap.get(activeOperator.objectName);
        if (event.startEntity == null) {
            event.startEntity =
                    activeGroup.planSession.getExecutionPlan().createStartEntity(event.start);
            activeGroup.setRunning(event.startEntity.operatorEntity);
        }
        ContainerSessionID containerSessionID = activeGroup.containerSessionID;
        event.session =
                state.getContainerSession(event.startEntity.containerName, containerSessionID);
        ConcreteOperatorID opID =
                activeGroup.planSession.getOperatorIdMap().get(event.startEntity.operatorEntity);
        if (activeOperator.operatorEntity.type == OperatorType.processing) {
            event.processOperator = true;
        }
        event.jobs = new ContainerJobs();
        event.jobs.addJob(new StartOperatorJob(opID, activeGroup.containerSessionID));
    }

    @Override
    public void handle(StartEvent event, EventProcessor proc) throws RemoteException {
        event.results = event.session.execJobs(event.jobs);
        event.messageCount = 1;
    }

    @Override
    public void postProcess(StartEvent event, PlanEventSchedulerState state)
            throws RemoteException {
        if (event.processOperator) {
            String opName = event.startEntity.operatorName.split("\\.")[0];
            state.getStatistics().error.remove(opName);
            state.getStatistics().completed.remove(opName);
            state.getStatistics().running.add(opName);
        }
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
