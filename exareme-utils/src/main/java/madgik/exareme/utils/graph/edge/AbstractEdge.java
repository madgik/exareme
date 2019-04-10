package madgik.exareme.utils.graph.edge;

/**
 * A helper abstract class for implementing an edge.
 *
 * @author konikos
 */
abstract public class AbstractEdge<V> implements Edge<V> {

    private V source;
    private V target;

    public AbstractEdge(V source, V target) {
        this.source = source;
        this.target = target;
    }

    public V getTargetVertex() {
        return this.target;
    }

    public V getSourceVertex() {
        return this.source;
    }

    /**
     * An edge is equal with another if the source vertices are the same and the
     * target vertices are the same.
     */
    @Override
    public boolean equals(Object e) {
        if (!(e instanceof Edge)) {
            return false;
        }

        Edge edge = (Edge) e;

        return this.source.equals(edge.getSourceVertex()) && this.target
                .equals(edge.getTargetVertex());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.source != null ? this.source.hashCode() : 0);
        hash = 79 * hash + (this.target != null ? this.target.hashCode() : 0);
        return hash;
    }
}
