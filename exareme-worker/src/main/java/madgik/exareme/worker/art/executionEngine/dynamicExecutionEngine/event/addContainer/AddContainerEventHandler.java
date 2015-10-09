/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.addContainer;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.container.ContainerProxy;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveContainer;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class AddContainerEventHandler implements ExecEngineEventHandler<AddContainerEvent> {
    public static final AddContainerEventHandler instance = new AddContainerEventHandler();
    private static final long serialVersionUID = 1L;

    public AddContainerEventHandler() {
    }

    @Override public void preProcess(AddContainerEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        try {
            ActiveContainer activeCont = state.getActiveContainer(event.containerName);
            OperatorGroup group = activeCont.operatorGroup;
            ActiveOperatorGroup activeGroup =
                group.objectNameActiveGroupMap.get(event.containerName);
            EntityName containerEntity = event.containerEntity;
            if (containerEntity == null) {
                containerEntity =
                    activeGroup.planSession.getExecutionPlan().addContainer(event.container);
            }
            ContainerSessionID containerSessionID = activeCont.containerSessionID;
            ContainerSession containerSession =
                state.getContainerSession(event.containerName, containerSessionID);
      /* If the session does not exist */
            if (containerSession == null) {
                ContainerProxy containerProxy = state.getContainerProxy(containerEntity.getName());
                if (containerProxy == null) {
                    containerProxy = state.registryProxy.lookupContainer(containerEntity);
                    state.addContainerProxy(containerEntity.getName(), containerProxy);
                }
                containerSession =
                    containerProxy.createSession(containerSessionID, state.getPlanSessionID());
                state
                    .addContainerSession(event.containerName, containerSessionID, containerSession);
                state.getStatistics().incrContainerSessions();
            }
        } catch (RemoteException e) {
            throw new RemoteException("Cannot handle add container event", e);
        }
    }

    @Override public void handle(AddContainerEvent event, EventProcessor proc)
        throws RemoteException {
        // Do nothing!
    }

    @Override public void postProcess(AddContainerEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
