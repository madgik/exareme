/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.clock.globalQuantumClockTick;

import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

/**
 * @author herald
 */
public class GlobalQuantumClockTickEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    // Pre-process
    //..
    public long timeToTick_ms;
    public long quantumCount;
    public boolean warning;

    // Process
    // ...

    public GlobalQuantumClockTickEvent(long quantumCount, PlanEventSchedulerState state) {
        super(state);
        this.timeToTick_ms = 0;
        this.quantumCount = quantumCount;
        this.warning = false;
    }

    public GlobalQuantumClockTickEvent(long timeToTick_ms, long quantumCount,
        PlanEventSchedulerState state) {
        super(state);
        this.timeToTick_ms = timeToTick_ms;
        this.quantumCount = quantumCount;
        this.warning = true;
    }
}
