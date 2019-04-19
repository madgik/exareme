package madgik.exareme.utils.graph;

import madgik.exareme.utils.graph.edge.Edge;

import java.util.Set;

/**
 * @author konikos
 */
public interface DirectedGraph<V, E extends Edge<V>> extends Graph<V, E> {

    /**
     * Gets a set of all the edges that have v as target. If v does not
     * exist, IllegalArgumentException is thrown.
     *
     * @param v a vertex in the graph
     * @return a set of all edges going from or to V.
     * @throws IllegalArgumentException If v isn't in the graph.
     */
    Set<E> incomingEdgesSet(V v);

    /**
     * Gets a set of all the edges that has v as source. If v does not
     * exist, IllegalArgumentException is thrown.
     *
     * @param v a vertex in the graph
     * @return a set of all edges going from or to V.
     * @throws IllegalArgumentException If v isn't in the graph.
     */
    Set<E> outgoingEdgesSet(V v);
}
