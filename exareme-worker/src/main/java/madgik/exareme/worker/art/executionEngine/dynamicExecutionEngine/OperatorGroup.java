/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.common.optimizer.OperatorBehavior;
import madgik.exareme.common.optimizer.OperatorCategory;
import madgik.exareme.common.optimizer.OperatorType;
import madgik.exareme.worker.art.executionEngine.ExecEngineConstants;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveBufferPool;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.materialized.MaterializedBuffer;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlan;
import madgik.exareme.worker.art.executionPlan.ExecutionPlanFactory;
import madgik.exareme.worker.art.executionPlan.SemanticError;
import madgik.exareme.worker.art.executionPlan.entity.BufferEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorLinkEntity;
import madgik.exareme.worker.art.executionPlan.entity.PragmaEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Container;
import madgik.exareme.worker.art.executionPlan.parser.expression.Operator;
import madgik.exareme.worker.art.executionPlan.parser.expression.OperatorLink;
import madgik.exareme.worker.art.executionPlan.parser.expression.Parameter;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author herald
 */
public class OperatorGroup {

    private static Logger log = Logger.getLogger(OperatorGroup.class);
    public long groupID;
    public PlanEventSchedulerState state = null;
    public boolean hasStarted = false;
    /* The active group that has terminated */
    public boolean hasTerminated = false;
    public ActiveOperatorGroup groupTerminated = null;
    /* Operators belonging to this group */
    public HashMap<String, OperatorEntity> operatorMap = null;
    public EditableExecutionPlan partialPlan = null;
    /* Inputs and outputs (must be materialized) */
    public HashMap<String, BufferEntity> inputs = null;
    public HashMap<String, BufferEntity> outputs = null;
    /* The mapping of operators to active groups */
    public HashMap<String, ActiveOperatorGroup> opNameActiveGroupMap = null;
    public HashMap<String, ActiveOperatorGroup> objectNameActiveGroupMap = null;
    /* Active groups */
    public LinkedList<ActiveOperatorGroup> activeOperatorGroups = null;
    public HashMap<String, OperatorEntity> partialToRealOperatorNameMap = null;
    public HashMap<String, OperatorEntity> realToPartialOperatorNameMap = null;
    public HashMap<String, BufferEntity> partialToRealBufferNameMap = null;
    public HashMap<String, BufferEntity> realToPartialBufferNameMap = null;
    /* Buffer pool sessions */
    public ArrayList<MaterializedBuffer> inputBufferPoolSessions = null;
    public ArrayList<MaterializedBuffer> outputBufferPoolSessions = null;
    // Recovery
    public int timesFailed = 0;
    // Data materialization memory
    // TODO(herald): this 0.1 looks like a magik number
    private double dataMaterializationMem = 0.0;

    public OperatorGroup(long groupID, PlanEventSchedulerState state) {
        this.groupID = groupID;
        this.state = state;
        this.operatorMap = new HashMap<String, OperatorEntity>();

        this.inputs = new HashMap<String, BufferEntity>();
        this.outputs = new HashMap<String, BufferEntity>();
        this.opNameActiveGroupMap = new HashMap<String, ActiveOperatorGroup>();
        this.objectNameActiveGroupMap = new HashMap<String, ActiveOperatorGroup>();
        this.activeOperatorGroups = new LinkedList<ActiveOperatorGroup>();

        this.partialToRealOperatorNameMap = new HashMap<String, OperatorEntity>();
        this.realToPartialOperatorNameMap = new HashMap<String, OperatorEntity>();
        this.partialToRealBufferNameMap = new HashMap<String, BufferEntity>();
        this.realToPartialBufferNameMap = new HashMap<String, BufferEntity>();

        this.inputBufferPoolSessions = new ArrayList<MaterializedBuffer>();
        this.outputBufferPoolSessions = new ArrayList<MaterializedBuffer>();

    }

    public void createPartialPlan() throws RemoteException {
        /* Add materialization operators */
        if (partialPlan == null) {
            {/* Create the partial plan */

                partialPlan = ExecutionPlanFactory.createEditableExecutionPlan();
                /* Add the operators */
                for (OperatorEntity co : operatorMap.values()) {
                    /* Add the container */
                    addContainer(co.containerName, co.container);
                    OperatorEntity newCo = partialPlan.addOperator(
                            new Operator(co.operatorName, co.operator, co.paramList, co.queryString,
                                    co.containerName, co.linksparams, co.locations));
                    this.partialToRealOperatorNameMap.put(newCo.operatorName, co);
                    this.realToPartialOperatorNameMap.put(co.operatorName, newCo);
                }
            }/* END: Create the partial plan */

            HashMap<String, OperatorEntity> ops = new HashMap<String, OperatorEntity>(operatorMap);

            for (OperatorEntity op : ops.values()) {
                /* Add the read materialized buffers */
                if (op.type.equals(OperatorType.dataTransfer)) {
                    OperatorEntity from = state.getPlan().getFromLinks(op).iterator().next();
                    for (OperatorEntity to : state.getPlan().getToLinks(op)) {
                        if (ops.containsKey(from.operatorName) == false) {
                            log.trace("Get matBuff: " + from.operatorName + "_" + to.operatorName);
                            MaterializedBuffer matBuff = this.state
                                    .getMaterializedBuffer(from.operatorName + "_" + to.operatorName);
                            String fileName = matBuff.fileName;
                            log.trace("FILENAME DT: " + fileName);
                            //TODO(JV) make this right for more than one outputs
                            op.linksparams.get(to.operatorName)
                                    .add(new Parameter("Name", fileName));
                        }

                    }
                    continue;
                }
                Iterable<OperatorEntity> fromList = state.getPlan().getFromLinks(op);
                for (OperatorEntity from : fromList) {
                    log.trace(from.operatorName + " -> " + op.operatorName);
                    if (ops.containsKey(from.operatorName) == false) {
                        String fromName = from.operatorName;
                        if (from.type.equals(OperatorType.dataTransfer)) {
                            fromName =
                                    state.getPlan().getFromLinks(from).iterator().next().operatorName;
                        }

                        MaterializedBuffer matBuff = this.state.getMaterializedBuffer(
                                fromName.split("\\.")[0] + "_" + op.operatorName);

                        this.inputBufferPoolSessions.add(matBuff);

                        String fileName = matBuff.fileName;
                        log.trace("FILENAME:::: GET :::: " + from.operatorName);
                        log.trace("FILENAME read " + fileName);
                        LinkedList<Parameter> readParameters = new LinkedList<Parameter>();
                        readParameters.add(new Parameter("Name", fileName));
                        readParameters.add(new Parameter(OperatorEntity.MEMORY_PARAM,
                                String.valueOf(dataMaterializationMem)));
                        readParameters.add(new Parameter(OperatorEntity.BEHAVIOR_PARAM,
                                String.valueOf(OperatorBehavior.store_and_forward)));
                        readParameters.add(new Parameter(OperatorEntity.TYPE_PARAM,
                                String.valueOf(OperatorType.dataMaterialization)));
                        readParameters.add(new Parameter(OperatorEntity.CATEGORY_PARAM,
                                op.category + "_R_" + OperatorCategory.dt));

                        String bufferReader;
                        PragmaEntity bufferReaderPragma = state.getPlan()
                                .getPragma(ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_READER);
                        if (bufferReaderPragma != null) {
                            bufferReader = bufferReaderPragma.pragmaValue;
                        } else {
                            bufferReader = ExecEngineConstants.MATERIALIZED_BUFFER_READER;
                        }
                        OperatorEntity read = partialPlan.addOperator(
                                new Operator(from.operatorName + "-R", bufferReader, readParameters, "",
                                        op.containerName, null));
                        operatorMap.put(read.operatorName, read);

                        /* Create and register the active operator */
                        ActiveOperator acOp = new ActiveOperator(read, null, this);
                        acOp.isActive = true;
                        this.state.addActiveObject(read.operatorName, acOp);

                        OperatorLinkEntity connect =
                                state.getPlan().getOperatorLink(from.operatorName, op.operatorName);
                        /* Create the connection */
                        partialPlan.addOperatorLink(
                                new OperatorLink(read.operatorName, op.operatorName,
                                        connect.containerName, connect.paramList));

                    } else {
                        OperatorLinkEntity connect =
                                state.getPlan().getOperatorLink(from.operatorName, op.operatorName);
                        /* Create the connection */
                        partialPlan.addOperatorLink(
                                new OperatorLink(connect.fromOperator.operatorName,
                                        connect.toOperator.operatorName, connect.containerName,
                                        connect.paramList));

                    }
                }
                /* Add the write materialized buffers */
                Iterable<OperatorEntity> toList = state.getPlan().getToLinks(op);
                Map<String, MaterializedBuffer> partToMatBuff = new HashMap<>();
                for (OperatorEntity to1 : toList) {

                    if (to1.type.equals(OperatorType.dataTransfer)) { //next is dt
                        for (OperatorEntity to : state.getPlan()
                                .getToLinks(to1)) { //iterate on dt's next operators
                            log.trace(op.operatorName + " -> " + to1.operatorName + " -> "
                                    + to.operatorName);
                            if (ops.containsKey(to.operatorName) == false) {
                                addContainer(to1.containerName, to1.container);
                                String part = null;
                                for (Parameter p : state.getPlan()
                                        .getOperatorLink(to1.operatorName, to.operatorName).paramList) {
                                    if (p.name.equals("part")) {
                                        part = p.value;
                                    }
                                }
                                if (part == null) {
                                    throw new RemoteException("part parameter in Operator is null");
                                }
                                String fileName = op.operatorName + "_P_" + part + ".S-" + state
                                        .getPlanSessionID().getLongId();
                                if (!partToMatBuff.containsKey(part)) {
                                    /* Register the materialized buffer */
                                    MaterializedBuffer matBuff =
                                            new MaterializedBuffer(op.operatorName, fileName,
                                                    op.containerName);
                                    partToMatBuff.put(part, matBuff);
                                    // Register the buffer pool session
                                    ActiveBufferPool bufferPool =
                                            new ActiveBufferPool(matBuff.fileName, matBuff, null, this);
                                    this.state.addActiveObject(matBuff.fileName, bufferPool);
                                    this.state.addMaterializedBuffer(
                                            op.operatorName + "_" + to.operatorName, matBuff);
                                    this.outputBufferPoolSessions.add(matBuff);

                                    log.trace("FILENAME:::: ADD :::: " + op.operatorName);
                                    log.trace("FILENAME write " + fileName);
                                    LinkedList<Parameter> writeParameters = new LinkedList<>();
                                    writeParameters.add(new Parameter("Name", fileName));
                                    writeParameters.add(new Parameter(OperatorEntity.MEMORY_PARAM,
                                            String.valueOf(dataMaterializationMem)));
                                    writeParameters.add(new Parameter(OperatorEntity.BEHAVIOR_PARAM,
                                            String.valueOf(OperatorBehavior.store_and_forward)));
                                    writeParameters.add(new Parameter(OperatorEntity.TYPE_PARAM,
                                            String.valueOf(OperatorType.dataMaterialization)));
                                    writeParameters.add(new Parameter(OperatorEntity.CATEGORY_PARAM,
                                            to1.category + "_W_" + OperatorCategory.dt));


                                    String bufferWriter;
                                    state.getPlan()
                                            .getOperatorLink(to1.operatorName, to.operatorName);
                                    PragmaEntity bufferWriterEntity = state.getPlan().getPragma(
                                            ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_WRITER);
                                    if (bufferWriterEntity != null) {
                                        bufferWriter = bufferWriterEntity.pragmaValue;
                                    } else {
                                        bufferWriter =
                                                ExecEngineConstants.MATERIALIZED_BUFFER_WRITER;
                                    }

                                    log.trace("Adding operator: " + op.operatorName + "-W-" + "P-"
                                            + part);
                                    OperatorEntity write = partialPlan.addOperator(
                                            new Operator(op.operatorName + "-W-" + "P-" + part,
                                                    bufferWriter, writeParameters, "", op.containerName,
                                                    null));


                                    /* Create and register the active operator */
                                    ActiveOperator acOp = new ActiveOperator(write, null, this);
                                    acOp.isActive = true;
                                    this.state.addActiveObject(write.operatorName, acOp);
                                    OperatorLinkEntity connect = state.getPlan()
                                            .getOperatorLink(to1.operatorName, to.operatorName);

                                    /* Create the connections */
                                    partialPlan.addOperatorLink(
                                            new OperatorLink(op.operatorName, write.operatorName,
                                                    op.containerName, connect.paramList));

                                } else {
                                    this.state.addMaterializedBuffer(
                                            op.operatorName + "_" + to.operatorName,
                                            partToMatBuff.get(part));
                                }
                            }
                        }
                    }
                }

                for (OperatorEntity to : toList) {
                    log.trace(op.operatorName + " -> " + to.operatorName);

                    if (ops.containsKey(to.operatorName) == false && !to.type
                            .equals(OperatorType.dataTransfer)) {
                        addContainer(to.containerName, to.container);
                        String part = null;
                        for (Parameter p : state.getPlan()
                                .getOperatorLink(op.operatorName, to.operatorName).paramList) {
                            if (p.name.equals("part")) {
                                part = p.value;
                            }
                        }
                        if (part == null) {
                            throw new RemoteException("part parameter in Operator is null");
                        }
                        if (!partToMatBuff.containsKey(part)) {
                            String fileName =
                                    op.operatorName + "_P_" + part + ".S-" + state.getPlanSessionID()
                                            .getLongId();
                            /* Register the materialized buffer */
                            MaterializedBuffer matBuff =
                                    new MaterializedBuffer(op.operatorName, fileName, op.containerName);
                            partToMatBuff.put(part, matBuff);
                            // Register the buffer pool session
                            ActiveBufferPool bufferPool =
                                    new ActiveBufferPool(matBuff.fileName, matBuff, null, this);

                            this.state.addActiveObject(matBuff.fileName, bufferPool);
                            this.state
                                    .addMaterializedBuffer(op.operatorName + "_" + to.operatorName,
                                            matBuff);
                            this.outputBufferPoolSessions.add(matBuff);

                            log.trace("FILENAME:::: ADD :::: " + op.operatorName);
                            log.trace("FILENAME write " + fileName);
                            LinkedList<Parameter> writeParameters = new LinkedList<>();
                            writeParameters.add(new Parameter("Name", fileName));
                            writeParameters.add(new Parameter(OperatorEntity.MEMORY_PARAM,
                                    String.valueOf(dataMaterializationMem)));
                            writeParameters.add(new Parameter(OperatorEntity.BEHAVIOR_PARAM,
                                    String.valueOf(OperatorBehavior.store_and_forward)));
                            writeParameters.add(new Parameter(OperatorEntity.TYPE_PARAM,
                                    String.valueOf(OperatorType.dataMaterialization)));
                            writeParameters.add(new Parameter(OperatorEntity.CATEGORY_PARAM,
                                    op.category + "_W_" + OperatorCategory.dt));

                            String bufferWriter;
                            state.getPlan().getOperatorLink(op.operatorName, to.operatorName);
                            PragmaEntity bufferWriterEntity = state.getPlan()
                                    .getPragma(ExecEngineConstants.PRAGMA_MATERIALIZED_BUFFER_WRITER);
                            if (bufferWriterEntity != null) {
                                bufferWriter = bufferWriterEntity.pragmaValue;
                            } else {
                                bufferWriter = ExecEngineConstants.MATERIALIZED_BUFFER_WRITER;
                            }

                            log.trace("Adding operator: " + op.operatorName + "-W-" + "P-" + part);
                            OperatorEntity write = partialPlan.addOperator(
                                    new Operator(op.operatorName + "-W-" + "P-" + part, bufferWriter,
                                            writeParameters, "", op.containerName, null));

                            /* Create and register the active operator */
                            ActiveOperator acOp = new ActiveOperator(write, null, this);
                            acOp.isActive = true;
                            this.state.addActiveObject(write.operatorName, acOp);
                            OperatorLinkEntity connect =
                                    state.getPlan().getOperatorLink(op.operatorName, to.operatorName);

                            /* Create the connections */
                            partialPlan.addOperatorLink(
                                    new OperatorLink(op.operatorName, write.operatorName,
                                            connect.containerName, connect.paramList));

                        } else {
                            this.state
                                    .addMaterializedBuffer(op.operatorName + "_" + to.operatorName,
                                            partToMatBuff.get(part));
                        }

                    }
                }
            }
        }
        log.debug("Partial Plan: " + partialPlan);
    }

    private void addContainer(String containerName, EntityName entityName) throws SemanticError {
        /* Add the container */
        if (partialPlan.isDefined(containerName) == false) {
            partialPlan.addContainer(
                    new Container(containerName, entityName.getName(), entityName.getPort(),
                            entityName.getDataTransferPort()));
        }
    }

    public ActiveOperatorGroup createNewActiveGroup(ContainerSessionID containerSessionID)
            throws SemanticError {
        ActiveOperatorGroup group =
                new ActiveOperatorGroup(activeOperatorGroups.size(), this, containerSessionID);
        activeOperatorGroups.add(group);
        return group;
    }

    public void setRunning(OperatorEntity entity) {
        ActiveOperatorGroup group = opNameActiveGroupMap.get(entity.operatorName);
        group.setRunning(entity);
    }

    public ActiveOperatorGroup setTerminated(OperatorEntity entity, boolean force) {
        ActiveOperatorGroup group = opNameActiveGroupMap.get(entity.operatorName);
        group.setTerminated(entity, force);
        if ((group.hasTerminated) && (group.hasError == false)) {
            log.trace("~~~~~~ GROUP TERMINATED: " + groupID + "." + group.activeGroupId);
            hasTerminated = true;
            groupTerminated = group;
        } else {
            log.trace("~~~~~~ GROUP: " + groupID + "." + group.activeGroupId);
        }
        return group;
    }

    public int setError(OperatorEntity entity, Exception exception) {
        ActiveOperatorGroup group = opNameActiveGroupMap.get(entity.operatorName);
        return group.setError(entity, exception);
    }

    @Override
    public String toString() {
        return operatorMap.values().toString();
    }
}
