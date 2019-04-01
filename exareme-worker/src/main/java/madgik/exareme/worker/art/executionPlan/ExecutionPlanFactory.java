/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.worker.art.executionPlan;

import madgik.exareme.worker.art.executionPlan.parser.expression.PlanExpression;

/**
 * @author herald
 */
public class ExecutionPlanFactory {

    private ExecutionPlanFactory() {
    }

    public static EditableExecutionPlan createEditableExecutionPlan() {
        return new ExecutionPlanImpl();
    }

    public static EditableExecutionPlan createEditableExecutionPlan(PlanExpression expression)
            throws SemanticError {
        return new ExecutionPlanImpl(expression);
    }
}
