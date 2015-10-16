/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.ContainerJobType;

/**
 * @author John Chronis
 */
public class CreateOperatorLinkJobResult extends ContainerJobResult {

    public CreateBufferJobResult bufferJobResult;

    public CreateOperatorLinkJobResult(CreateBufferJobResult bufferJobResult) {
        this.bufferJobResult = bufferJobResult;
    }

    @Override public ContainerJobType getType() {
        return ContainerJobType.createOperatorLink;
    }
}
