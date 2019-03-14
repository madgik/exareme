/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.adaptorMgr.AdaptorType;
import madgik.exareme.worker.art.container.buffer.BufferID;
import madgik.exareme.worker.art.parameter.Parameters;

/**
 * @author heraldkllapi
 */
public class CreateWriteAdaptorJob extends CreateAdaptorJob {

    public CreateWriteAdaptorJob(BufferID bufferID, AdaptorType type) {
        super(bufferID, type);
    }

    public CreateWriteAdaptorJob(ConcreteOperatorID concreteOperatorId, BufferID bufferID,
                                 String portName, Parameters parameters, AdaptorType type) {
        super(bufferID, type);
        this.concreteOperatorId = concreteOperatorId;
        this.portName = portName;
        this.parameters = parameters;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.createWriteAdaptor;
    }
}
