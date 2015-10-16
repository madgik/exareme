/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.art.PlanSessionID;

/**
 * @author herald
 */
public interface PlanTerminationListener {

    void terminated(PlanSessionID sessionID);
}
