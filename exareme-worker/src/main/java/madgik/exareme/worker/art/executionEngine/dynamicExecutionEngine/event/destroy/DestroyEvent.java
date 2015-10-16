/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.destroy;

import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionPlan.entity.DestroyEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Destroy;

/**
 * @author herald
 */
public class DestroyEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public Destroy destroy = null;
    public DestroyEntity destroyEntity = null;
    // Pre-process
    public ContainerJobs jobs = null;
    public ContainerSession session = null;
    public ContainerJobResults results = null;
    public int messageCount = 0;

    public DestroyEvent(Destroy destroy, PlanEventSchedulerState state) {
        super(state);
        this.destroy = destroy;
    }

    public DestroyEvent(DestroyEntity destroyEntity, PlanEventSchedulerState state) {
        super(state);
        this.destroyEntity = destroyEntity;
    }
}
