/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container.job;

import madgik.exareme.worker.art.container.ContainerJobResult;
import madgik.exareme.worker.art.container.adaptor.AdaptorID;

/**
 * @author heraldkllapi
 */
public abstract class CreateAdaptorJobResult extends ContainerJobResult {
    public final AdaptorID adaptorId;

    public CreateAdaptorJobResult(AdaptorID adaptorId) {
        this.adaptorId = adaptorId;
    }
}
