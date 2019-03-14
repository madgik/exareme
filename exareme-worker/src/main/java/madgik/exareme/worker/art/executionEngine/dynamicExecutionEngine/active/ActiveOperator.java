/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionPlan.entity.ObjectType;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author herald
 */
public class ActiveOperator extends ActiveObject {

    public boolean isRunning = false;
    public boolean isActive = false;
    public boolean hasTerminated = false;
    public boolean hasError = false;
    public OperatorEntity operatorEntity = null;

    public int exitCode = -1;
    public Serializable exitMessage = null;
    public Date exitDate = null;

    public ActiveOperator(OperatorEntity operatorEntity, ContainerSessionID containerSessionID,
                          OperatorGroup operatorGroup) {
        super(ObjectType.Operator, operatorEntity.operatorName, containerSessionID, operatorGroup);
        this.operatorEntity = operatorEntity;
    }
}
