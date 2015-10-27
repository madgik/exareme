/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.event.terminated;

import madgik.exareme.worker.art.concreteOperator.ConcreteOperatorID;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventScheduler;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerState;
import madgik.exareme.worker.art.executionEngine.dynamicExecutionEngine.PlanEventSchedulerStateElasticTree;

import java.io.Serializable;

/**
 * @author heraldkllapi
 */
public class OperatorElasticTreeTerminatedEvent extends OperatorTerminatedEvent {
    public final PlanEventSchedulerStateElasticTree elasticState;

    public OperatorElasticTreeTerminatedEvent(ConcreteOperatorID operatorID, int exidCode,
        Serializable exitMessage, PlanEventScheduler scheduler, PlanEventSchedulerState state,
        PlanEventSchedulerStateElasticTree elasticState) {
        super(operatorID, exidCode, exitMessage, scheduler, state, false);
        this.elasticState = elasticState;
    }
}
