/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.dataTransfer;

import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

/**
 * @author Herald Kllapi
 */
public class DataTransferEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    // Pre-process
    public ContainerSession session = null;
    public ContainerJobs jobs = null;
    public ActiveOperatorGroup activeGroup = null;
    // Process
    public ContainerJobResults results = null;
    public int messageCount = 0;

    public DataTransferEvent(PlanEventSchedulerState state) {
        super(state);
    }
}
