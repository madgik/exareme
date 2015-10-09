/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJob;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorType;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.parameter.Parameters;

/**
 * @author heraldkllapi
 */
public abstract class CreateAdaptorJob implements ContainerJob {

    public ConcreteOperatorID concreteOperatorId = null;
    public BufferID bufferID = null;
    public String portName = null;
    public Parameters parameters = null;
    public AdaptorType type = null;
    public String adaptorName = null;

    public CreateAdaptorJob(BufferID bufferID, AdaptorType type) {
        this.bufferID = bufferID;
        this.type = type;
    }
}
