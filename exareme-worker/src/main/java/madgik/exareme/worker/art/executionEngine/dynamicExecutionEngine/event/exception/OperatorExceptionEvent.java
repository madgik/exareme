/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.exception;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;

import java.rmi.RemoteException;
import java.util.Date;

/**
 * @author herald
 */
public class OperatorExceptionEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public ConcreteOperatorID operatorID = null;
    public RemoteException exception = null;
    public Date time = null;
    public int messageCount = 0;

    public OperatorExceptionEvent(ConcreteOperatorID operatorID, RemoteException ex, Date time,
                                  PlanEventSchedulerState state) {
        super(state);
        this.operatorID = operatorID;
        this.exception = ex;
        this.time = time;
    }
}
