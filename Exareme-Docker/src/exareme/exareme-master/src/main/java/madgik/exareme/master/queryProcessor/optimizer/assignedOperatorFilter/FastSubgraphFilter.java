/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.assignedOperatorFilter;

import madgik.exareme.master.queryProcessor.optimizer.AssignedOperatorFilter;

import java.util.Arrays;

/**
 * @author herald
 */
public class FastSubgraphFilter implements AssignedOperatorFilter {
    private int[] assigments = null;
    private int filtered = 0;

    public FastSubgraphFilter(int numOfOperators) {
        assigments = new int[numOfOperators];
        Arrays.fill(assigments, -1);
    }

    public void assignOperator(int operator, int container) {
        assigments[operator] = container;
        ++filtered;
    }

    @Override
    public int getOperatorAssignment(int opNum) {
        return assigments[opNum];
    }

    @Override
    public int getNumberFilteredOperators() {
        return filtered;
    }
}
