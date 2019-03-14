/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.report;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

import java.io.Serializable;

/**
 * @author johnchronis
 */
public class ReportEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public ConcreteOperatorID operatorID = null;
    public int exidCode = 0;
    public Serializable exitMessage = null;
    public int messageCount = 0;
    public PlanEventScheduler scheduler = null;

    public ReportEvent(ConcreteOperatorID operatorID, int exidCode, Serializable exitMessage,
                       PlanEventScheduler scheduler, PlanEventSchedulerState state) {
        super(state);
        this.operatorID = operatorID;
        this.exidCode = exidCode;
        this.exitMessage = exitMessage;
        this.scheduler = scheduler;
    }
}
