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
public class ActiveBuffer extends ActiveObject {

    public boolean isMaterialized = false;
    public String fileName = null;

    public ActiveBuffer(String bufferName, ContainerSessionID containerSessionID,
        OperatorGroup operatorGroup) {
        super(ObjectType.Buffer, bufferName, containerSessionID, operatorGroup);
    }
}
