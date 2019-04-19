/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer;

import java.io.Serializable;

/**
 * This interface is a subgraph filtering, i.e. return the nodes that are
 * candidate for scheduling.
 *
 * @author Herald Kllapi <br>
 * herald@di.uoa.gr /
 * University of Athens
 * @since 1.0
 */
public interface AssignedOperatorFilter extends Serializable {
    // Returns a non-negative number if the operator must be assigned to the
    // returned container, otherwise a negative number. A negative number means
    // that the assignment of the operator has to be determined by the algorithm.
    int getOperatorAssignment(int opNum);

    // Return the nunber of operators that are filtered by this filter.
    int getNumberFilteredOperators();
}
