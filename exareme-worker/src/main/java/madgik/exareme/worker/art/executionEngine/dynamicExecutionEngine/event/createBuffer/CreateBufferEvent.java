///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.event.createBuffer;
//
//import madgik.exareme.db.art.container.ContainerJobResults;
//import madgik.exareme.db.art.container.ContainerJobs;
//import madgik.exareme.db.art.container.ContainerSession;
//import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
//import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
//import madgik.exareme.db.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
//import madgik.exareme.db.art.executionPlan.entity.BufferEntity;
//import madgik.exareme.db.art.executionPlan.parser.expression.Buffer;
//
///**
// *
// * @author herald
// */
//public class CreateBufferEvent extends ExecEngineEvent {
//  private static final long serialVersionUID = 1L;
//  public Buffer buffer = null;
//  public BufferEntity bufferEntity = null;
//  // Pre-process
//  public ContainerSession session = null;
//  public ContainerJobs jobs = null;
//  public ActiveOperatorGroup activeGroup = null;
//  // Process
//  public ContainerJobResults results = null;
//  public int messageCount = 0;
//
//  public CreateBufferEvent(Buffer create, PlanEventSchedulerState state) {
//    super(state);
//    this.buffer = null;
//  }
//
//  public CreateBufferEvent(BufferEntity createEntity, PlanEventSchedulerState state) {
//    super(state);
//    this.bufferEntity = createEntity;
//  }
//}
