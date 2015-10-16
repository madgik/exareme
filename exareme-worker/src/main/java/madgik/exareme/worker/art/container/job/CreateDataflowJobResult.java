/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.ContainerJobType;

/**
 * @author Vaggelis Nikolopoulos
 */
public class CreateDataflowJobResult extends ContainerJobResult {

    @Override public ContainerJobType getType() {
        return ContainerJobType.distributedJobCreateDataflow;
    }
}
