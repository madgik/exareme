/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.ContainerJobType;

/**
 * @author heraldkllapi
 */
public class CreateOperatorJobResult extends ContainerJobResult {

    public final ConcreteOperatorID opID;
    public CreateBufferJobResult bufferJobResult;

    public CreateOperatorJobResult(ConcreteOperatorID opID) {
        this.opID = opID;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.createOperator;
    }

    public void SetBufferJobResult(CreateBufferJobResult createBufferJobResult) {
        bufferJobResult = createBufferJobResult;
    }
}

