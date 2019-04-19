package madgik.exareme.utils.graph.edge;

/**
 * @author konikos
 */
public class UnweightedEdgeTest extends EdgeTest {

    @Override
    protected Edge<Integer> newEdge(Integer source, Integer target) {
        return new UnweightedEdge(source, target);
    }
}
