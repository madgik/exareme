/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.utils.eventProcessor.EventProcessor;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.job.DestroyOperatorJob;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveObject;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.worker.art.executionPlan.SemanticError;
import madgik.exareme.worker.art.executionPlan.entity.DestroyEntity;
import madgik.exareme.worker.art.executionPlan.entity.ObjectType;

import java.rmi.RemoteException;

/**
 * @author herald
 */
public class DestroyEventHandler implements ExecEngineEventHandler<DestroyEvent> {

    public static final DestroyEventHandler instance = new DestroyEventHandler();
    private static final long serialVersionUID = 1L;

    public DestroyEventHandler() {
    }

    @Override public void preProcess(DestroyEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        try {
            String objectName =
                (event.destroy != null) ? event.destroy.objectName : event.destroyEntity.objectName;

            ActiveObject activeObject = state.getActiveObject(objectName);
            OperatorGroup group = activeObject.operatorGroup;

            ActiveOperatorGroup activeGroup = group.objectNameActiveGroupMap.get(objectName);

            DestroyEntity destroyEntity = event.destroyEntity;
            if (destroyEntity == null) {
                destroyEntity = activeGroup.planSession.getExecutionPlan().
                    createDestroyEntity(event.destroy);
            }
            ContainerSessionID containerSessionID = activeGroup.containerSessionID;
            event.session =
                state.getContainerSession(destroyEntity.containerName, containerSessionID);

            ObjectType type = activeGroup.planSession.getExecutionPlan().getType(objectName);
            switch (type) {
                case Operator: {
                    ConcreteOperatorID operatorID = activeGroup.planSession.
                        getOperatorIdMap().get(destroyEntity.operatorEntity);
                    event.jobs = new ContainerJobs();
                    event.jobs.addJob(new DestroyOperatorJob(operatorID));

                    //          for (Pair<String, BufferID> p : activeGroup.planSession.getBufferIdMap().get(event.destroyEntity.operatorEntity).values()) {
                    //
                    //            event.jobs = new ContainerJobs();
                    //            event.jobs.addJob(new DestroyBufferJob(p.getB()));
                    //          }

                    break;
                }
                //        case Buffer: {
                //          BufferID bufferID = activeGroup.planSession.getBufferIdMap().get(
                //              destroyEntity.bufferEntity);
                //          event.jobs = new ContainerJobs();
                //          event.jobs.addJob(new DestroyBufferJob(bufferID));
                //          break;
                //        }
            }
        } catch (SemanticError e) {
            throw new RemoteException("Cannot handle destroy", e);
        }
    }

    @Override public void handle(DestroyEvent event, EventProcessor proc) throws RemoteException {
        event.session.execJobs(event.jobs);
        event.messageCount = 1;
    }

    @Override public void postProcess(DestroyEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
