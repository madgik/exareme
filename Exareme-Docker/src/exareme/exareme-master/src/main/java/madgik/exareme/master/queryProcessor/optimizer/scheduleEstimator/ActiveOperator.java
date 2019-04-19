/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator;

import madgik.exareme.master.queryProcessor.optimizer.scheduler.OperatorAssignment;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class ActiveOperator implements Serializable {
    public OperatorAssignment assignment = null;
    public int start_SEC = 0;
    public int end_SEC = 0;
}
