/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionPlan.entity.ObjectType;

/**
 * @author herald
 */
public class ActiveObject {

    public final ObjectType objectType;
    public final String objectName;
    public final ContainerSessionID containerSessionID;
    public OperatorGroup operatorGroup = null;

    public ActiveObject(ObjectType objectType, String objectName,
                        ContainerSessionID containerSessionID, OperatorGroup operatorGroup) {
        this.objectType = objectType;
        this.objectName = objectName;
        this.containerSessionID = containerSessionID;
        this.operatorGroup = operatorGroup;
    }
}
