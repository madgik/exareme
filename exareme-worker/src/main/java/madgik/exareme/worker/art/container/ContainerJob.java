/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.container;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public interface ContainerJob extends Serializable {

    // TODO(DSD) check
    //  public JobResources getResources();
    ContainerJobType getType();
}
