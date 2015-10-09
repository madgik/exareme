/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.materialized;

import madgik.exareme.worker.art.executionPlan.entity.ObjectType;

/**
 * @author herald
 */
public class MaterializedBuffer extends MaterializedObject {

    public String bufferName = null;
    public String fileName = null;
    public String containerName = null;

    public MaterializedBuffer(String bufferName, String fileName, String containerName) {
        super(ObjectType.Buffer);
        this.bufferName = bufferName;
        this.fileName = fileName;
        this.containerName = containerName;
    }
}
