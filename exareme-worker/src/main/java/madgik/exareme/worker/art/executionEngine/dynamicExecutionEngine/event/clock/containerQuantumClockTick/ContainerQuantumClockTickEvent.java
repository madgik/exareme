/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.containerQuantumClockTick;

import madgik.exareme.worker.art.container.ContainerID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

/**
 * @author herald
 */
public class ContainerQuantumClockTickEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    // Pre-process
    public ContainerID id;
    public long timeToTick_ms;
    public long quantumCount;
    public boolean warning;
    // Process
    // ...

    public ContainerQuantumClockTickEvent(ContainerID id, long quantumCount,
        PlanEventSchedulerState state) {
        super(state);
        this.id = id;
        this.timeToTick_ms = 0;
        this.quantumCount = quantumCount;
        this.warning = false;
    }

    public ContainerQuantumClockTickEvent(ContainerID id, long timeToTick_ms, long quantumCount,
        PlanEventSchedulerState state) {
        super(state);
        this.id = id;
        this.timeToTick_ms = timeToTick_ms;
        this.quantumCount = quantumCount;
        this.warning = true;
    }
}
