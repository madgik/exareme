/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.materialized;

import madgik.exareme.worker.art.executionPlan.entity.ObjectType;

/**
 * @author herald
 */
public class MaterializedObject {
    public ObjectType objectType = null;

    public MaterializedObject(ObjectType objectType) {
        this.objectType = objectType;
    }
}
