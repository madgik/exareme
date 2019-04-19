package madgik.exareme.utils.graph.edge;

/**
 * A simple weighted edge.
 *
 * @param <V> The vertex type.
 * @param <T> The weight type.
 * @author konikos
 */
final public class WeightedEdge<V, T> extends AbstractEdge<V> {

    private T weight;

    public WeightedEdge(V source, V target, T weight) {
        super(source, target);
        this.weight = weight;
    }

    public T getWeight() {
        return this.weight;
    }

    /**
     * A weighted edge is equal to another if the source vertices are the same,
     * the target vertices are the same and if the weights are the same.
     *
     * @param e
     */
    @Override
    public boolean equals(Object e) {
        if (!(e instanceof WeightedEdge)) {
            return false;
        }

        return super.equals(e) && ((WeightedEdge) e).getWeight().equals(this.weight);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + super.hashCode();
        hash = 89 * hash + (this.weight != null ? this.weight.hashCode() : 0);
        return hash;
    }
}
