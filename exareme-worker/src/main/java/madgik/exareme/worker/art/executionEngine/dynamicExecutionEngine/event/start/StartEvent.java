/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.start;

import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionPlan.entity.StartEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Start;

/**
 * @author herald
 */
public class StartEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public Start start = null;
    public StartEntity startEntity = null;
    // Pre-process
    public ContainerSession session = null;
    public ContainerJobs jobs = null;
    public boolean processOperator = false;
    // Process
    public ContainerJobResults results = null;
    public int messageCount = 0;

    public StartEvent(Start start, PlanEventSchedulerState state) {
        super(state);
        this.start = start;
    }

    public StartEvent(StartEntity startEntity, PlanEventSchedulerState state) {
        super(state);
        this.startEntity = startEntity;
    }
}
