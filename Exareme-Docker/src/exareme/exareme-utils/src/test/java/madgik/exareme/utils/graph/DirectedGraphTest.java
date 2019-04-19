package madgik.exareme.utils.graph;

import junit.framework.Assert;
import madgik.exareme.utils.graph.edge.Edge;

import java.util.Set;

/**
 * Abstract helper class for testing subclasses of DirectedGraph. Every subclass
 * must implement newGraph.
 *
 * @author konikos
 */
public abstract class DirectedGraphTest extends GraphTest {

    abstract protected DirectedGraph<Integer, Edge<Integer>> newGraph();

    @Override
    protected DirectedGraph<Integer, Edge<Integer>> generateGraph() {
        return (DirectedGraph<Integer, Edge<Integer>>) super.generateGraph();
    }

    public void incomingEdges_ofVertex() {
        System.out.println("incomingEdges");

        DirectedGraph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            Set<Edge<Integer>> incomingEdges = graph.incomingEdgesSet(new Integer(i));
            Assert.assertEquals(incomingEdges.size(), i % 2 == 0 ? 13 : 12);
            for (Edge<Integer> edge : incomingEdges) {
                Assert.assertEquals((i + edge.getSourceVertex().intValue()) % 2, 0);
            }
        }
    }

    public void outgoingEdges_ofVertex() {
        System.out.println("outgoingEdges");

        DirectedGraph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            Set<Edge<Integer>> outgoingEdges = graph.outgoingEdgesSet(new Integer(i));
            Assert.assertEquals(outgoingEdges.size(), i % 2 == 0 ? 13 : 12);
            for (Edge<Integer> edge : outgoingEdges) {
                Assert.assertEquals((i + edge.getTargetVertex().intValue()) % 2, 0);
            }
        }
    }

}
