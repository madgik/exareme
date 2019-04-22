/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.common.app.engine.scheduler.elasticTree.system.registry;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public interface Entry extends Serializable {
    String getId();

    double getSize_MB();
}
