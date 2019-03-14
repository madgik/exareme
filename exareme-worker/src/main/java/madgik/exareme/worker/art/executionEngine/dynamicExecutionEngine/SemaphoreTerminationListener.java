/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine;

import madgik.exareme.common.art.PlanSessionID;

import java.util.concurrent.Semaphore;

/**
 * @author herald
 */
public class SemaphoreTerminationListener implements PlanTerminationListener {

    private Semaphore wait = null;

    SemaphoreTerminationListener(Semaphore wait) {
        this.wait = wait;
    }

    @Override
    public void terminated(PlanSessionID sessionID) {
        this.wait.release();
    }
}
