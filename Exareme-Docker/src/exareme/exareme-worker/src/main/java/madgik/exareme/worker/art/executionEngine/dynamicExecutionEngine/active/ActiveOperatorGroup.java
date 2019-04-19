/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.materialized.MaterializedBuffer;
import madgik.exareme.worker.art.executionEngine.session.PlanSession;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanFactory;
import madgik.exareme.worker.art.executionPlan.SemanticError;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorLinkEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;
import madgik.exareme.worker.art.executionPlan.parser.expression.Operator;
import madgik.exareme.worker.art.executionPlan.parser.expression.OperatorLink;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;

//import madgik.exareme.db.art.executionPlan.entity.BufferEntity;
//import madgik.exareme.db.art.executionPlan.entity.BufferLinkEntity;
//import madgik.exareme.db.art.executionPlan.parser.expression.Buffer;
//import madgik.exareme.db.art.executionPlan.parser.expression.BufferLink;


/**
 * @author herald
 */
public class ActiveOperatorGroup {

    private static final Logger log = Logger.getLogger(ActiveOperatorGroup.class);
    public OperatorGroup group = null;
    public long activeGroupId;
    public boolean hasStarted = false;
    public boolean hasTerminated = false;
    public boolean hasError = false;
    /* Operators belonging to this group */
    public HashMap<String, OperatorEntity> operatorMap = new HashMap<>();
    public PlanSession planSession = null;
    public ContainerSessionID containerSessionID = null;
    // Container map
    public HashMap<String, String> activeToRealContainerNameMap = new HashMap<>();
    public HashMap<String, String> realToActiveContainerNameMap = new HashMap<>();
    // Operator
    public HashMap<String, String> activeToRealOperatorNameMap = new HashMap<>();
    public HashMap<String, String> realToActiveOperatorNameMap = new HashMap<>();
    // Buffer
    //public HashMap<String, String> activeToRealBufferNameMap = new HashMap<>();
    //public HashMap<String, String> realToActiveBufferNameMap = new HashMap<>();
    // Statistics
    public HashMap<String, OperatorEntity> runningOperators = new HashMap<>();
    public HashMap<String, OperatorEntity> terminatedOperators = new HashMap<>();
    public HashMap<String, OperatorEntity> errorOperators = new HashMap<>();
    public HashMap<String, LinkedList<Exception>> exceptionMap = new HashMap<>(4);

    public ActiveOperatorGroup(long activeGroupId, OperatorGroup group,
                               ContainerSessionID containerSessionID) throws SemanticError {
        this.activeGroupId = activeGroupId;
        this.containerSessionID = containerSessionID;
        this.group = group;
        /* Create active plan */
        EditableExecutionPlan activePlan = ExecutionPlanFactory.createEditableExecutionPlan();
        /* Create the plan session */
        planSession = new PlanSession(activePlan, null, null);
        String id = "." + group.groupID + "." + activeGroupId;
        ExecutionPlan partialPlan = group.partialPlan;
        /* Add the containers */
        for (OperatorEntity co : partialPlan.iterateOperators()) {
            String containerName = co.containerName + id;
            if (activePlan.isDefined(containerName) == false) {
                activePlan.addContainer(
                        new Container(containerName, co.container.getName(), co.container.getPort(),
                                co.container.getDataTransferPort()));
                // Register the container
                ActiveContainer activeContainer =
                        new ActiveContainer(containerName, containerSessionID, group);
                this.group.objectNameActiveGroupMap.put(containerName, this);
                this.activeToRealContainerNameMap.put(containerName, co.containerName);
                this.realToActiveContainerNameMap.put(co.containerName, containerName);
                this.group.state.addActiveObject(containerName, activeContainer);
            }
        }
        /* Add the operators */
        for (OperatorEntity co : partialPlan.iterateOperators()) {
            String containerName = realToActiveContainerNameMap.get(co.containerName);
            OperatorEntity newCo = activePlan.addOperator(
                    new Operator(co.operatorName + id, co.operator, co.paramList, co.queryString,
                            containerName, co.linksparams, co.locations));
            this.operatorMap.put(newCo.operatorName, newCo);
            this.group.opNameActiveGroupMap.put(newCo.operatorName, this);
            this.group.objectNameActiveGroupMap.put(newCo.operatorName, this);
            this.activeToRealOperatorNameMap.put(newCo.operatorName, co.operatorName);
            this.realToActiveOperatorNameMap.put(co.operatorName, newCo.operatorName);
            /* Register the operator to the schedule state */
            ActiveOperator acOp = new ActiveOperator(newCo, containerSessionID, group);
            acOp.isActive = true;
            this.group.state.addActiveObject(newCo.operatorName, acOp);
        }
        /* Add the buffers */
    /*for (BufferEntity buffer : partialPlan.iterateBuffers()) {
      String containerName = realToActiveContainerNameMap.get(buffer.containerName);
      BufferEntity newBuffer = activePlan.addBuffer(new Buffer(
        buffer.bufferName + id,
        buffer.QoS,
        containerName,
        buffer.paramList));
      activeToRealBufferNameMap.put(newBuffer.bufferName, buffer.bufferName);
      realToActiveBufferNameMap.put(buffer.bufferName, newBuffer.bufferName);
      this.group.objectNameActiveGroupMap.put(newBuffer.bufferName, this);
      /* Create the active buffer *//*
      ActiveBuffer acBuff = new ActiveBuffer(newBuffer.bufferName, containerSessionID, group);
      /* Register the active buffer *//*
      this.group.state.addActiveObject(newBuffer.bufferName, acBuff);
    }*/
        /* Add the links */
    /*for (BufferLinkEntity connect : partialPlan.iterateBufferLinks()) {
      String containerName = realToActiveContainerNameMap.get(connect.containerName);
      String operatorName = realToActiveOperatorNameMap.get(connect.operatorEntity.operatorName);
      String bufferName = realToActiveBufferNameMap.get(connect.bufferEntity.bufferName);
      switch (connect.direction) {
        case reader: {
          activePlan.addBufferLink(new BufferLink(bufferName,
            operatorName,
            containerName,
            connect.paramList));
          break;
        }
        case writer: {
          activePlan.addBufferLink(new BufferLink(operatorName,
            bufferName,
            containerName,
            connect.paramList));
          break;
        }
      }
    }*/
        for (OperatorLinkEntity opLink : partialPlan.iterateOperatorLinks()) {
            String containerName = realToActiveContainerNameMap.get(opLink.containerName);
            String fromOperatorName =
                    realToActiveOperatorNameMap.get(opLink.fromOperator.operatorName);
            String toOperatorName = realToActiveOperatorNameMap.get(opLink.toOperator.operatorName);
            activePlan.addOperatorLink(
                    new OperatorLink(fromOperatorName, toOperatorName, containerName,
                            opLink.paramList));
            activePlan.getOperator(fromOperatorName).addLinkParam(toOperatorName, opLink.paramList);
        }


        // Add the buffer pool sessions
        for (MaterializedBuffer inBufferPool : group.inputBufferPoolSessions) {
            String containerName = realToActiveContainerNameMap.get(inBufferPool.containerName);
            ActiveBufferPool bufferPool =
                    this.group.state.getActiveBufferPool(inBufferPool.fileName);
            String bufferPoolSessionName = bufferPool.objectName + id;
            MaterializedBuffer newBuffer =
                    new MaterializedBuffer(bufferPool.buffer.bufferName, bufferPool.buffer.fileName,
                            containerName);
            ActiveBufferPool activeBuffer =
                    new ActiveBufferPool(bufferPoolSessionName, newBuffer, containerSessionID, group);
            /* Register the active buffer */
            this.group.objectNameActiveGroupMap.put(activeBuffer.objectName, this);
            this.group.state.addActiveObject(activeBuffer.objectName, activeBuffer);
        }
        for (MaterializedBuffer outBufferPool : group.outputBufferPoolSessions) {
            String containerName = realToActiveContainerNameMap.get(outBufferPool.containerName);
            ActiveBufferPool bufferPool =
                    this.group.state.getActiveBufferPool(outBufferPool.fileName);
            String bufferPoolSessionName = bufferPool.objectName + id;
            MaterializedBuffer newBuffer =
                    new MaterializedBuffer(bufferPool.buffer.bufferName, bufferPool.buffer.fileName,
                            containerName);
            ActiveBufferPool activeBuffer =
                    new ActiveBufferPool(bufferPoolSessionName, newBuffer, containerSessionID, group);
            /* Register the active buffer */
            this.group.objectNameActiveGroupMap.put(activeBuffer.objectName, this);
            this.group.state.addActiveObject(activeBuffer.objectName, activeBuffer);
        }
    }

    public void setRunning(OperatorEntity entity) {
        runningOperators.put(entity.operatorName, entity);
        hasStarted = true;
    }

    public void setTerminated(OperatorEntity entity, boolean force) {
        runningOperators.remove(entity.operatorName);
        terminatedOperators.put(entity.operatorName, entity);
        log.debug("Active GROUP: " + activeGroupId + " - running: " + runningOperators.size()
                + " - finished: " + terminatedOperators.size());
        if (force || runningOperators.isEmpty()) {
            hasTerminated = true;
        }
    }

    public int setError(OperatorEntity entity, Exception e) {
        hasError = true;
        runningOperators.remove(entity.operatorName);
        errorOperators.put(entity.operatorName, entity);
        LinkedList<Exception> exceptions = exceptionMap.get(entity.operatorName);
        if (exceptions == null) {
            exceptions = new LinkedList<>();
            exceptionMap.put(entity.operatorName, exceptions);
        }
        exceptions.add(e);
        if (runningOperators.isEmpty()) {
            hasTerminated = true;
        }
        return exceptionMap.size();
    }
}
