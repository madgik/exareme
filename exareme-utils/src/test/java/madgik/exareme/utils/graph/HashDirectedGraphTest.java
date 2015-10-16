package madgik.exareme.utils.graph;

import madgik.exareme.utils.graph.edge.Edge;

/**
 * @author konikos
 */
public class HashDirectedGraphTest extends DirectedGraphTest {

    protected DirectedGraph<Integer, Edge<Integer>> newGraph() {
        return new HashDirectedGraph<Integer, Edge<Integer>>();
    }
}
