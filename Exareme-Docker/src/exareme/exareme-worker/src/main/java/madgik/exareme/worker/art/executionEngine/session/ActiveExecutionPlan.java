/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.session;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.executionPlan.ExecutionPlan;
import madgik.exareme.worker.art.executionPlan.entity.BufferLinkEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.entity.OperatorLinkEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Herald Kllapi<br>
 * @author Dimitris Paparas<br>
 * @author Eva Sitaridi<br>
 * {herald,paparas,evas}@di.uoa.gr<br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class ActiveExecutionPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    private ExecutionPlan executionPlan = null;
    private Map<OperatorEntity, ConcreteOperatorID> operatorIdMap = null;
    private Map<ConcreteOperatorID, OperatorEntity> opEntityMap = null;
    private Map<OperatorLinkEntity, AdaptorID> adaptorIdMap = null;
    private Map<String, OperatorEntity> entityMap = null;

    public ActiveExecutionPlan(ExecutionPlan executionPlan,
                               Map<OperatorEntity, ConcreteOperatorID> operatorIdMap,
                               Map<OperatorLinkEntity, AdaptorID> adaptorIdMap) {
        this.executionPlan = executionPlan;
        setOperatorIdMap(operatorIdMap);
        setAdaptorIdMap(adaptorIdMap);
    }

    private void setOperatorIdMap(Map<OperatorEntity, ConcreteOperatorID> operatorIdMap) {
        this.operatorIdMap = operatorIdMap;
        this.entityMap = new HashMap<String, OperatorEntity>();
        this.opEntityMap = new HashMap<ConcreteOperatorID, OperatorEntity>();

        for (OperatorEntity entity : operatorIdMap.keySet()) {
            entityMap.put(entity.operatorName, entity);
            opEntityMap.put(operatorIdMap.get(entity), entity);
        }
    }

    private void setAdaptorIdMap(Map<OperatorLinkEntity, AdaptorID> adaptorIdMap) {
        this.adaptorIdMap = adaptorIdMap;
    }

    public OperatorEntity getOperatorEntityByName(String name) {
        return entityMap.get(name);
    }

    public ConcreteOperatorID mapNameToID(String opName) {
        return operatorIdMap.get(entityMap.get(opName));
    }

    public ConcreteOperatorID mapEntityToID(OperatorEntity entity) {
        return operatorIdMap.get(entity);
    }

    public AdaptorID mapEntityToID(BufferLinkEntity entity) {
        return adaptorIdMap.get(entity);
    }


    public OperatorEntity mapIDtoEntity(ConcreteOperatorID operatorID) {
        return opEntityMap.get(operatorID);
    }

    public ExecutionPlan getExecutionPlan() {
        return executionPlan;
    }
}
