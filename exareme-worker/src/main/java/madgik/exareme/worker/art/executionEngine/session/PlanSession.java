/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.utils.association.Pair;
import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.executionPlan.EditableExecutionPlan;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorLinkEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 *         {herald,paparas,evas}@di.uoa.gr<br>
 *         University of Athens /
 *         Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class PlanSession {
    private EditableExecutionPlan executionPlan = null;
    private ActiveExecutionPlan activeExecutionPlan = null;
    private PlanSessionStatus planSessionStatus = null;
    private Map<EntityName, ContainerSession> containerSessionMap = null;
    private PlanSessionReportID sessionReportID = null;
    private Map<OperatorEntity, ConcreteOperatorID> operatorIdMap = null;
    private Map<ConcreteOperatorID, OperatorEntity> operatorIdEntityMap = null;
    private Map<OperatorEntity, Map<String, Pair<String, BufferID>>> bufferIdMap = null;
    private Map<OperatorLinkEntity, AdaptorID> adaptorIdMap = null;

    public PlanSession(EditableExecutionPlan executionPlan, PlanSessionStatus planSessionStatus,
        PlanSessionReportID sessionReportID) {
        this.executionPlan = executionPlan;
        this.planSessionStatus = planSessionStatus;
        this.sessionReportID = sessionReportID;
        this.containerSessionMap =
            Collections.synchronizedMap(new HashMap<EntityName, ContainerSession>());

        this.operatorIdMap =
            Collections.synchronizedMap(new HashMap<OperatorEntity, ConcreteOperatorID>());

        this.operatorIdEntityMap =
            Collections.synchronizedMap(new HashMap<ConcreteOperatorID, OperatorEntity>());
        this.bufferIdMap = Collections
            .synchronizedMap(new HashMap<OperatorEntity, Map<String, Pair<String, BufferID>>>());
        this.adaptorIdMap =
            Collections.synchronizedMap(new HashMap<OperatorLinkEntity, AdaptorID>());
    }

    public EditableExecutionPlan getExecutionPlan() {
        return executionPlan;
    }

    public ActiveExecutionPlan getActiveExecutionPlan() {
        if (activeExecutionPlan == null) {
            createActivePlan();
        }
        return activeExecutionPlan;
    }

    public PlanSessionStatus getPlanSessionStatus() {
        return planSessionStatus;
    }

    public Map<EntityName, ContainerSession> getContainerSessionMap() {
        return containerSessionMap;
    }

    public PlanSessionReportID getSessionReportID() {
        return sessionReportID;
    }

    public Map<OperatorLinkEntity, AdaptorID> getAdaptorIdMap() {
        return adaptorIdMap;
    }

    public Map<OperatorEntity, Map<String, Pair<String, BufferID>>> getBufferIdMap() {
        return bufferIdMap;
    }

    public Map<OperatorEntity, ConcreteOperatorID> getOperatorIdMap() {
        return operatorIdMap;
    }

    public Map<ConcreteOperatorID, OperatorEntity> getOperatorIdEntityMap() {
        return operatorIdEntityMap;
    }

    public void planChanged() {
        this.activeExecutionPlan = null;
    }

    public void createActivePlan() {
        this.activeExecutionPlan =
            new ActiveExecutionPlan(executionPlan, operatorIdMap, adaptorIdMap);
    }
}
