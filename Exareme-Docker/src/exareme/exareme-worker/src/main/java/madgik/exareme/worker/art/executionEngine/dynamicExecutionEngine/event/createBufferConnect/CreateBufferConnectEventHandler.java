/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createBufferConnect;
/*
import java.rmi.RemoteException;
import madgik.exareme.db.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.db.art.container.ContainerJobs;
import madgik.exareme.common.id.ContainerSessionID;
import madgik.exareme.db.art.container.adaptor.AdaptorID;
import madgik.exareme.db.art.container.adaptorMgr.AdaptorType;
import madgik.exareme.db.art.container.buffer.BufferID;
import madgik.exareme.db.art.container.job.CreateAdaptorJobResult;
import madgik.exareme.db.art.container.job.CreateReadAdaptorJob;
import madgik.exareme.db.art.container.job.CreateWriteAdaptorJob;
import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.active.ActiveObject;
import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.db.art.parameter.Parameter;
import madgik.exareme.db.art.parameter.Parameters;
import madgik.exareme.utils.eventProcessor.EventProcessor;
 */
/**
 * @author herald
 */
/*
public class CreateBufferConnectEventHandler {
//    implements ExecEngineEventHandler<CreateBufferConnectEvent> {
  private static final long serialVersionUID = 1L;
  public static final CreateBufferConnectEventHandler instance =
      new CreateBufferConnectEventHandler();

  public CreateBufferConnectEventHandler() {
  }

//  @Override
  public void preProcess(CreateBufferConnectEvent event,
                         PlanEventSchedulerState state) throws RemoteException {
    String objectName;
    if (event.connect != null) {
      objectName = event.connect.from;
    } else {
      objectName = event.connectEntity.operatorEntity.operatorName;
    }
    ActiveObject activeObject = state.getActiveObject(objectName);
    event.activeGroup = activeObject.operatorGroup.objectNameActiveGroupMap.
        get(activeObject.objectName);
    if (event.connectEntity == null) {
      event.connectEntity = event.activeGroup.planSession.getExecutionPlan().
          addBufferLink(event.connect);
    }
    ActiveOperator activeOperator = state.
        getActiveOperator(event.connectEntity.operatorEntity.operatorName);
    ContainerSessionID containerSessionID = activeOperator.containerSessionID;
    event.session = state.getContainerSession(event.connectEntity.containerName,
                                              containerSessionID);
    event.jobs = new ContainerJobs();
    ConcreteOperatorID operatorID = event.activeGroup.planSession.getOperatorIdMap()
        .get(event.connectEntity.operatorEntity);
    BufferID bufferID = event.activeGroup.planSession.getBufferIdMap()
        .get(event.connectEntity.bufferEntity);

    Parameters params = new Parameters();
    if (event.connectEntity.paramList != null) {
      for (madgik.exareme.db.art.executionPlan.parser.expression.Parameter param :
           event.connectEntity.paramList) {
        params.addParameter(new Parameter(param.name, param.value));
      }
    }
    switch (event.connectEntity.direction) {
      case reader:
        AdaptorType readType = null;
        switch (event.connectEntity.type) {
          case local:
            readType = AdaptorType.LOCAL_ADAPTOR;
            break;
          case remote:
            readType = AdaptorType.REMOTE_ADAPTOR;
            break;
        }
        event.jobs.addJob(new CreateReadAdaptorJob(
            bufferID,
            operatorID,
            event.connectEntity.bufferEntity.bufferName,
            params,
            readType));
        break;
      case writer:
        AdaptorType WriteType = null;
        switch (event.connectEntity.type) {
          case local:
            WriteType = AdaptorType.LOCAL_ADAPTOR;
            break;
          case remote:
            WriteType = AdaptorType.REMOTE_ADAPTOR;
            break;
        }
        event.jobs.addJob(new CreateWriteAdaptorJob(
            operatorID,
            bufferID,
            event.connectEntity.bufferEntity.bufferName,
            params,
            WriteType));
        break;
    }
  }

//  @Override
//  public void handle(CreateBufferConnectEvent event,
//                     EventProcessor proc) throws RemoteException {
//    event.results = event.session.execJobs(event.jobs);
//    event.messageCount = 1;
//  }

//  @Override
  public void postProcess(CreateBufferConnectEvent event,
                          PlanEventSchedulerState state) throws RemoteException {
    AdaptorID adaptorId = ((CreateAdaptorJobResult)event.results.getJobResults().get(0)).adaptorId;
    event.activeGroup.planSession.getAdaptorIdMap().put(event.connectEntity, adaptorId);
    state.getStatistics().incrLinksCreated();
    state.getStatistics().incrControlMessagesCountBy(event.messageCount);
  }
}
 */
