/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event;

import madgik.exareme.utils.eventProcessor.Event;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;

import java.util.concurrent.Semaphore;

/**
 * @author heraldkllapi
 */
public abstract class ExecEngineEvent implements Event {
    public final PlanEventSchedulerState state;
    public Semaphore wait = null;

    public ExecEngineEvent(PlanEventSchedulerState state) {
        this.state = state;
    }

    public void done() {
        if (wait != null) {
            wait.release();
        }
    }
}
