/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createBuffer;
/*
import java.rmi.RemoteException;
import madgik.exareme.db.art.container.ContainerJobs;
import madgik.exareme.common.id.ContainerSessionID;
import madgik.exareme.db.art.container.buffer.BufferID;
import madgik.exareme.db.art.container.buffer.BufferQoS;
import madgik.exareme.db.art.container.job.CreateBufferJob;
import madgik.exareme.db.art.container.job.CreateBufferJobResult;
import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.active.ActiveBuffer;
import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEventHandler;
import madgik.exareme.db.art.executionPlan.entity.BufferEntity;
import madgik.exareme.utils.eventProcessor.EventProcessor;
*/
/**
 * @author herald
 * <p/>
 * public class CreateBufferEventHandler {
 * //        implements ExecEngineEventHandler<CreateBufferEvent> {
 * private static final long serialVersionUID = 1L;
 * public static final CreateBufferEventHandler instance =
 * new CreateBufferEventHandler();
 * <p/>
 * public CreateBufferEventHandler() {
 * }
 * <p/>
 * //  @Override
 * public void preProcess(CreateBufferEvent event, PlanEventSchedulerState state)
 * throws RemoteException {
 * String bufferName = (event.buffer != null)?
 * event.buffer.bufferName : event.bufferEntity.bufferName;
 * ActiveBuffer activeBuffer = state.getActiveBuffer(bufferName);
 * OperatorGroup group = activeBuffer.operatorGroup;
 * event.activeGroup = group.objectNameActiveGroupMap.get(bufferName);
 * BufferEntity bufferEntity = event.bufferEntity;
 * if (bufferEntity == null) {
 * bufferEntity = event.activeGroup.planSession.getExecutionPlan().addBuffer(event.buffer);
 * }
 * ContainerSessionID containerSessionID = activeBuffer.containerSessionID;
 * event.session = state.getContainerSession(bufferEntity.containerName, containerSessionID);
 * BufferQoS qos = new BufferQoS();
 * int qos_num = Integer.parseInt(bufferEntity.QoS);
 * qos.setRecordCount(qos_num);
 * qos.setSizeMB(qos_num);
 * <p/>
 * event.jobs = new ContainerJobs();
 * event.jobs.addJob(new CreateBufferJob(bufferName, qos));
 * }
 * <p/>
 * //  @Override
 * //  public void handle(CreateBufferEvent event,
 * //                     EventProcessor proc) throws RemoteException {
 * //    event.results = event.session.execJobs(event.jobs);
 * //    event.messageCount = 1;
 * //  }
 * <p/>
 * //  @Override
 * public void postProcess(CreateBufferEvent event, PlanEventSchedulerState state)
 * throws RemoteException {
 * BufferID bufferId =
 * ((CreateBufferJobResult)event.results.getJobResults().get(0)).bufferId;
 * event.activeGroup.planSession.getBufferIdMap().put(event.bufferEntity, bufferId);
 * state.getStatistics().incrBuffersCreated();
 * state.getStatistics().incrControlMessagesCountBy(event.messageCount);
 * }
 * }
 */
