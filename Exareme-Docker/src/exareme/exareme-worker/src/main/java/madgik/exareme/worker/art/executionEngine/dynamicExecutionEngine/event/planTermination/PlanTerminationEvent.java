/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.planTermination;

import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

/**
 * @author herald
 */
public class PlanTerminationEvent extends ExecEngineEvent {

    private static final long serialVersionUID = 1L;
    public int messageCount = 0;
    //  public ContainerJobs jobs = null;

    public PlanTerminationEvent(PlanEventSchedulerState state) {
        super(state);
    }
}
