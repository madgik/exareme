package madgik.exareme.utils.graph;

import madgik.exareme.utils.graph.edge.Edge;

import java.util.*;

/**
 * Abstract Graph subclass that stores the edges in an adjacency HashMap
 *
 * @author konikos
 */
public class HashDirectedGraph<V, E extends Edge<V>> implements DirectedGraph<V, E> {


    private HashMap<V, HashSet<E>> adjacencyMap;
    private HashMap<V, HashSet<E>> reverseAdjacencyMap;

    public HashDirectedGraph() {
        this.adjacencyMap = new LinkedHashMap<V, HashSet<E>>();
        this.reverseAdjacencyMap = new LinkedHashMap<V, HashSet<E>>();
    }

    @Override
    public boolean addEdge(E e) {
        if (!this.adjacencyMap.containsKey(e.getSourceVertex())) {
            throw new IllegalArgumentException("Source not in graph");
        }

        if (!this.adjacencyMap.containsKey(e.getTargetVertex())) {
            throw new IllegalArgumentException("Target not in graph");
        }

        return this.adjacencyMap.get(e.getSourceVertex()).add(e) && this.reverseAdjacencyMap
                .get(e.getTargetVertex()).add(e);
    }

    @Override
    public boolean addVertex(V v) {
        if (this.adjacencyMap.containsKey(v)) {
            return false;
        }

        this.adjacencyMap.put(v, new HashSet<E>());
        this.reverseAdjacencyMap.put(v, new HashSet<E>());
        return true;
    }

    @Override
    public boolean containsEdge(V source, V target) {
        return getEdge(source, target) != null;
    }

    @Override
    public boolean containsVertex(V v) {
        return this.adjacencyMap.containsKey(v);
    }

    @Override
    public Set<V> verticesSet() {
        return Collections.unmodifiableSet(this.adjacencyMap.keySet());
    }

    @Override
    public E getEdge(V source, V target) {
        if (!this.adjacencyMap.containsKey(source)) {
            return null;
        }

        for (E edge : this.adjacencyMap.get(source)) {
            if (edge.getTargetVertex().equals(target)) {
                return edge;
            }
        }

        return null;
    }

    @Override
    public Set<E> edgesSet() {
        HashSet<E> edges = new HashSet();

        for (HashSet<E> s : this.adjacencyMap.values()) {
            edges.addAll(s);
        }

        return Collections.unmodifiableSet(edges);
    }

    @Override
    public Set<E> incomingEdgesSet(V v) {
        if (!this.reverseAdjacencyMap.containsKey(v)) {
            throw new IllegalArgumentException("Vertex not in graph");
        }

        return Collections.unmodifiableSet(this.reverseAdjacencyMap.get(v));
    }

    @Override
    public Set<E> outgoingEdgesSet(V v) {
        if (!this.adjacencyMap.containsKey(v)) {
            throw new IllegalArgumentException("Vertex not in graph");
        }

        return Collections.unmodifiableSet(this.adjacencyMap.get(v));
    }

    @Override
    public Set<E> edgesSet(V v) {
        HashSet<E> edges = new HashSet();

        edges.addAll(incomingEdgesSet(v));
        edges.addAll(outgoingEdgesSet(v));
        return Collections.unmodifiableSet(edges);
    }

    @Override
    public Set<E> edgesSet(V source, V target) {
        if (!this.adjacencyMap.containsKey(source) || !this.adjacencyMap.containsKey(target)) {
            throw new IllegalArgumentException("Vertex not in graph");
        }

        HashSet<E> edges = new HashSet();

        for (E e : this.adjacencyMap.get(source)) {
            if (e.getTargetVertex().equals(target)) {
                edges.add(e);
            }
        }

        return edges;
    }

    @Override
    public E removeEdge(V source, V target) {
        if (!this.adjacencyMap.containsKey(source) || !this.adjacencyMap.containsKey(target)) {
            throw new IllegalArgumentException("Vertex not in graph");
        }

        E edge = null;
        Set<E> sourceEdges = this.adjacencyMap.get(source);
        Set<E> targetEdges = this.reverseAdjacencyMap.get(target);

        for (E sourceEdge : sourceEdges) {
            if (sourceEdge.getTargetVertex().equals(target)) {
                sourceEdges.remove(sourceEdge);
                edge = sourceEdge;
                break;
            }
        }

        if (edge != null) {
            for (E targetEdge : targetEdges) {
                if (targetEdge.equals(edge)) {
                    targetEdges.remove(targetEdge);
                    break;
                }
            }
        }

        return edge;
    }

    @Override
    public boolean removeEdge(E e) {
        if (!this.adjacencyMap.containsKey(e.getSourceVertex()) || !this.adjacencyMap
                .containsKey(e.getTargetVertex())) {
            throw new IllegalArgumentException("Edge vertex not in graph");
        }

        boolean edgeRemoved = false;

        for (HashSet<E> vertexEdges : this.adjacencyMap.values()) {
            Iterator<E> it = vertexEdges.iterator();
            while (it.hasNext()) {
                E edge = it.next();
                if (edge.equals(e)) {
                    it.remove();
                    edgeRemoved = true;
                }
            }
        }

        if (edgeRemoved) {
            for (HashSet<E> vertexEdges : this.reverseAdjacencyMap.values()) {
                Iterator<E> it = vertexEdges.iterator();
                while (it.hasNext()) {
                    E edge = it.next();
                    if (edge.equals(e)) {
                        it.remove();
                    }
                }
            }
        }

        return edgeRemoved;
    }

    @Override
    public boolean removeAllEdges() {
        boolean hasChanged = false;

        for (HashSet<E> edgesSet : this.adjacencyMap.values()) {
            if (!edgesSet.isEmpty()) {
                hasChanged = true;
                edgesSet.clear();
            }
        }

        if (hasChanged) {
            for (HashSet<E> edgesSet : this.reverseAdjacencyMap.values()) {
                if (!edgesSet.isEmpty()) {
                    edgesSet.clear();
                }
            }
        }


        return hasChanged;
    }

    @Override
    public boolean removeVertex(V v) {
        if (!this.adjacencyMap.containsKey(v)) {
            return false;
        }

        this.adjacencyMap.remove(v);
        this.reverseAdjacencyMap.remove(v);
        return true;
    }

    @Override
    public boolean removeAllVertices() {
        if (this.adjacencyMap.keySet().isEmpty()) {
            return false;
        }

        this.adjacencyMap.clear();
        this.reverseAdjacencyMap.clear();
        return true;
    }
}
