/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.estimator;

import madgik.exareme.master.queryProcessor.graph.ConcreteOperator;

/**
 * @author heraldkllapi
 */
public class EstimatorResultOperator extends EstimatorResult {

    public final ConcreteOperator op;

    public EstimatorResultOperator(String name, ConcreteOperator op, boolean foundInHistory) {
        super(name, foundInHistory);
        this.op = op;
    }
}
