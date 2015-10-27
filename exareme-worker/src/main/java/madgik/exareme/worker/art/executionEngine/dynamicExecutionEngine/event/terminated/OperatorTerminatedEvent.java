/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

import java.io.Serializable;

/**
 * @author herald
 */
public class OperatorTerminatedEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public ConcreteOperatorID operatorID = null;
    public int exidCode = 0;
    public Serializable exitMessage = null;
    public int messageCount = 0;
    public PlanEventScheduler scheduler = null;
    public boolean terminateGroup;

    public OperatorTerminatedEvent(ConcreteOperatorID operatorID, int exidCode,
        Serializable exitMessage, PlanEventScheduler scheduler, PlanEventSchedulerState state,
        boolean terminateGroup) {
        super(state);
        this.operatorID = operatorID;
        this.exidCode = exidCode;
        this.exitMessage = exitMessage;
        this.scheduler = scheduler;
        this.terminateGroup = terminateGroup;
    }
}
