/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperatorConnect;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorType;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.container.job.CreateAdaptorJobResult;
import madgik.exareme.worker.art.container.job.CreateOperatorLinkJob;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveObject;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;

import java.rmi.RemoteException;

/**
 * @author John Chronis
 */
public class CreateOperatorConnectEventHandler {
    //    implements ExecEngineEventHandler<CreateBufferConnectEvent> {

    public static final CreateOperatorConnectEventHandler instance =
        new CreateOperatorConnectEventHandler();
    private static final long serialVersionUID = 1L;

    public CreateOperatorConnectEventHandler() {
    }

    //  @Override
    public void preProcess(CreateOperatorConnectEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        String toObjectName, fromObjectName;

        if (event.connect != null) {
            toObjectName = event.connect.from;
            fromObjectName = event.connect.to;
        } else {
            toObjectName = event.connectEntity.toOperator.operatorName;
            fromObjectName = event.connectEntity.fromOperator.operatorName;
        }
        //TODO(JV) ? an den exei ginei activateo deyteros op to group tou iparxei?

        ActiveObject toActiveObject = state.getActiveObject(toObjectName);
        event.activeGroup = toActiveObject.operatorGroup.objectNameActiveGroupMap.
            get(toActiveObject.objectName);
        if (event.connectEntity == null) {
            event.connectEntity = event.activeGroup.planSession.getExecutionPlan().
                addOperatorLink(event.connect);//TODO(jv) ? why
        }
        ActiveOperator toActiveOperator = state.
            getActiveOperator(event.connectEntity.toOperator.operatorName);
        ContainerSessionID toContainerSessionID = toActiveOperator.containerSessionID;
        event.session =
            state.getContainerSession(event.connectEntity.containerName, toContainerSessionID);
        event.jobs = new ContainerJobs();
        ConcreteOperatorID toOperatorID =
            event.activeGroup.planSession.getOperatorIdMap().get(event.connectEntity.toOperator);

        ActiveObject fromActiveObject = state.getActiveObject(toObjectName);
        event.activeGroup = fromActiveObject.operatorGroup.objectNameActiveGroupMap.
            get(fromActiveObject.objectName);
        if (event.connectEntity == null) {
            event.connectEntity = event.activeGroup.planSession.getExecutionPlan().
                addOperatorLink(event.connect);//TODO(jv) ? why
        }
        event.jobs = new ContainerJobs();
        ConcreteOperatorID fromOperatorID =
            event.activeGroup.planSession.getOperatorIdMap().get(event.connectEntity.fromOperator);
        AdaptorType adtp = AdaptorType.LOCAL_ADAPTOR;
        if (!event.connectEntity.fromOperator.container
            .equals(event.connectEntity.toOperator.container)) {
            adtp = AdaptorType.REMOTE_ADAPTOR;
        }

        BufferID bufferID =
            event.activeGroup.planSession.getBufferIdMap().get(event.connectEntity.fromOperator)
                .get(event.connectEntity.toOperator.operatorName).getB();
        String bufferName =
            event.activeGroup.planSession.getBufferIdMap().get(event.connectEntity.fromOperator)
                .get(event.connectEntity.toOperator.operatorName).getA();

        String IpProducer = event.connectEntity.fromOperator.container.getIP().split("_")[0];
        String IpConsumer = event.connectEntity.toOperator.container.getIP().split("_")[0];


        event.jobs.addJob(
            new CreateOperatorLinkJob(fromOperatorID, toOperatorID, event.connectEntity.paramList,
                adtp, bufferID, bufferName, IpProducer, IpConsumer,
                fromActiveObject.containerSessionID));
    }

    //  @Override
    //  public void handle(CreateBufferConnectEvent event,
    //                     EventProcessor proc) throws RemoteException {
    //    event.results = event.session.execJobs(event.jobs);
    //    event.messageCount = 1;
    //  }
    //  @Override
    public void postProcess(CreateOperatorConnectEvent event, PlanEventSchedulerState state)
        throws RemoteException {
        AdaptorID adaptorId =
            ((CreateAdaptorJobResult) event.results.getJobResults().get(0)).adaptorId;
        event.activeGroup.planSession.getAdaptorIdMap()
            .put(event.connectEntity, adaptorId);//TODO(jv) fix

        state.getStatistics().incrLinksCreated();
        state.getStatistics().incrControlMessagesCountBy(event.messageCount);
    }
}
