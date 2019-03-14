/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.buffer.BufferQoS;

/**
 * @author heraldkllapi
 */
public class CreateBufferJob implements ContainerJob {

    public final String bufferName;
    public final BufferQoS quality;

    public CreateBufferJob(String bufferName, BufferQoS quality) {
        this.bufferName = bufferName;
        this.quality = quality;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.createBuffer;
    }
}
