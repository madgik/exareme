package madgik.exareme.utils.graph.edge;

/**
 * A simple edge without weight.
 *
 * @author konikos
 */
final public class UnweightedEdge<V> extends AbstractEdge<V> {

    public UnweightedEdge(V source, V target) {
        super(source, target);
    }
}
