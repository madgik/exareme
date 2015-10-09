/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.buffer.BufferID;

/**
 * @author heraldkllapi
 */
public class DestroyBufferJob implements ContainerJob {
    public final BufferID bufferID;

    public DestroyBufferJob(BufferID bufferID) {
        this.bufferID = bufferID;
    }

    @Override public ContainerJobType getType() {
        return ContainerJobType.destroyBuffer;
    }
}
