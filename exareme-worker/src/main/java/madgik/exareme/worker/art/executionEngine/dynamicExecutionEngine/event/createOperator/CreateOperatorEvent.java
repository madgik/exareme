/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.createOperator;

import madgik.exareme.worker.art.container.ContainerJobResults;
import madgik.exareme.worker.art.container.ContainerJobs;
import madgik.exareme.worker.art.container.ContainerSession;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperator;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.active.ActiveOperatorGroup;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.ExecEngineEvent;
import madgik.exareme.worker.art.executionPlan.entity.OperatorEntity;
import madgik.exareme.worker.art.executionPlan.parser.expression.Operator;
import madgik.exareme.worker.art.parameter.Parameters;

/**
 * @author herald
 */
public class CreateOperatorEvent extends ExecEngineEvent {
    private static final long serialVersionUID = 1L;
    public Operator operator = null;
    public OperatorEntity operatorEntity = null;
    public Parameters linkParameters = null;
    // Pre-process
    public ActiveOperator activeOperator = null;
    public ActiveOperatorGroup activeGroup = null;
    public ContainerJobs jobs = null;
    public ContainerSession session = null;
    // Process
    public ContainerJobResults results = null;
    public int messageCount = 0;

    public CreateOperatorEvent(Operator operator, PlanEventSchedulerState state) {
        super(state);
        this.operator = operator;

    }

    public CreateOperatorEvent(OperatorEntity instantiateEntity, PlanEventSchedulerState state) {
        super(state);
        this.operatorEntity = instantiateEntity;

    }
}
