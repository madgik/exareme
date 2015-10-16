/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active;

import madgik.exareme.common.art.ContainerSessionID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.OperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.materialized.MaterializedBuffer;
import madgik.exareme.worker.art.executionPlan.entity.ObjectType;

/**
 * @author herald
 */
public class ActiveBufferPool extends ActiveObject {

    public MaterializedBuffer buffer = null;

    public ActiveBufferPool(String bufferPoolSessionName, MaterializedBuffer buffer,
        ContainerSessionID containerSessionID, OperatorGroup operatorGroup) {
        super(ObjectType.BufferPool, bufferPoolSessionName, containerSessionID, operatorGroup);
        this.buffer = buffer;
    }
}
