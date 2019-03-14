/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator;

import madgik.exareme.master.queryProcessor.graph.ConcreteQueryGraph;
import madgik.exareme.master.queryProcessor.graph.GraphGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

/**
 * @author heraldkllapi
 */
public class OperatorDependencySolverTest {

    public OperatorDependencySolverTest() {
    }

    @Test
    public void testDependencySolver() {
        ConcreteQueryGraph graph = GraphGenerator.createLatticeGraph(3, 3, 0);
        Assert.assertNotNull(graph);
        assertEquals(5, graph.getNumOfOperators());

        // Add them according to the graph
        SnFOperatorDependencySolver solver = new SnFOperatorDependencySolver(graph);
        int opsAssigned = 0;
        BitSet activated = solver.getInitial();
        System.err.println("LEAF: " + activated);
        while (opsAssigned < graph.getNumOfOperators()) {
            opsAssigned += activated.cardinality();
            activated = solver.addTerminated(activated);
            System.err.println("OPS: " + activated);
        }
        assertEquals(graph.getNumOfOperators(), opsAssigned);
    }
}
