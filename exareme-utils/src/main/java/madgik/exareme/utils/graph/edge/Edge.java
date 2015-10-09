package madgik.exareme.utils.graph.edge;

import java.io.Serializable;

/**
 * The interface all edges must implement.
 *
 * @param <V> The vertex type
 * @author konikos
 */
public interface Edge<V> extends Serializable {
    /**
     * Gets the source vertex of the edge.
     *
     * @return The source vertex.
     */
    V getSourceVertex();

    /**
     * Gets the target vertex of the edge.
     *
     * @return The target vertex.
     */
    V getTargetVertex();
}
