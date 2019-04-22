/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.adaptor.CombinedWriteAdaptorProxy;

/**
 * @author heraldkllapi
 */
public class CreateWriteAdaptorJobResult extends CreateAdaptorJobResult {

    public final CombinedWriteAdaptorProxy proxy;

    public CreateWriteAdaptorJobResult(AdaptorID adaptorId, CombinedWriteAdaptorProxy proxy) {
        super(adaptorId);
        this.proxy = proxy;
    }

    @Override
    public ContainerJobType getType() {
        return ContainerJobType.createWriteAdaptor;
    }
}
