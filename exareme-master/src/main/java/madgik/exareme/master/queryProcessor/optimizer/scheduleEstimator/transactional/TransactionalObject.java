/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public interface TransactionalObject extends Serializable {

    void commit();

    void rollback();
}
