/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.assignedOperatorFilter;

import madgik.exareme.master.queryProcessor.optimizer.AssignedOperatorFilter;

/**
 * @author herald
 */
public class PredefinedSubgraphFilter implements AssignedOperatorFilter {

    private int container = -1;
    private int numOfOperators = -1;

    public PredefinedSubgraphFilter(int container, int numOfOperators) {
        this.container = container;
        this.numOfOperators = numOfOperators;
    }

    @Override
    public int getOperatorAssignment(int opNum) {
        return container;
    }

    @Override
    public int getNumberFilteredOperators() {
        return numOfOperators;
    }
}
