/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.stop;

import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionPlan.entity.StopEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Stop;

/**
 * @author herald
 */
public class StopEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public Stop stop = null;
    public StopEntity stopEntity = null;
    // Pre-process
    public ContainerSession session = null;
    public ContainerJobs jobs = null;
    // Process
    public ContainerJobResults results = null;
    public int messageCount = 0;

    public StopEvent(Stop stop, PlanEventSchedulerState state) {
        super(state);
        this.stop = stop;
    }

    public StopEvent(StopEntity stopEntity, PlanEventSchedulerState state) {
        super(state);
        this.stopEntity = stopEntity;
    }
}
