/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.containersError;

import madgik.exareme.common.art.entity.EntityName;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Herald Kllapi
 */
public class ContainersErrorEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    // Pre-process
    public Set<EntityName> containers = null;
    public Set<EntityName> faultyContainers = new HashSet<>();


    public ContainersErrorEvent(PlanEventSchedulerState state, Set<EntityName> containers) {
        super(state);
        this.containers = containers;
    }
}
