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
public class ActiveState extends ActiveObject {

    public ActiveState(String stateName, ContainerSessionID containerSessionID,
        OperatorGroup operatorGroup) {
        super(ObjectType.State, stateName, containerSessionID, operatorGroup);
    }
}
