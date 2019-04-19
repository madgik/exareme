/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.assignedOperatorFilter;

import madgik.exareme.master.queryProcessor.optimizer.AssignedOperatorFilter;

/**
 * @author herald
 */
public class NoSubgraphFilter implements AssignedOperatorFilter {
    private static final NoSubgraphFilter instance = new NoSubgraphFilter();

    public static NoSubgraphFilter getInstance() {
        return instance;
    }

    @Override
    public int getOperatorAssignment(int opNum) {
        return -1;
    }

    @Override
    public int getNumberFilteredOperators() {
        return 0;
    }
}
