/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJobType;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;
import madgik.exareme.worker.art.container.adaptor.CombinedReadAdaptorProxy;

/**
 * @author heraldkllapi
 */
public class CreateReadAdaptorJobResult extends CreateAdaptorJobResult {

    public final CombinedReadAdaptorProxy proxy;

    public CreateReadAdaptorJobResult(AdaptorID adaptorId, CombinedReadAdaptorProxy proxy) {
        super(adaptorId);
        this.proxy = proxy;
    }

    @Override public ContainerJobType getType() {
        return ContainerJobType.createReadAdaptor;
    }
}
