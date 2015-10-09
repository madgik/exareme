/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan.entity;

import madgik.exareme.common.art.entity.EntityName;

import java.io.Serializable;

/**
 * @author herald
 */
public class BufferPoolSessionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String bufferPoolSessionName;
    public final String containerName;
    public final EntityName container;

    public BufferPoolSessionEntity(String bufferPoolSessionName, String containerName,
        EntityName container) {
        this.bufferPoolSessionName = bufferPoolSessionName;
        this.containerName = containerName;
        this.container = container;
    }
}
