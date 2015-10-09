package madgik.exareme.utils.graph;

import junit.framework.Assert;
import junit.framework.TestCase;
import madgik.exareme.utils.graph.edge.Edge;
import madgik.exareme.utils.graph.edge.UnweightedEdge;

import java.util.Set;

/**
 * Abstract helper class for testing subclasses of Graph. Every subclass must
 * implement newGraph.
 *
 * @author konikos
 */
public abstract class GraphTest extends TestCase {

    abstract protected Graph<Integer, Edge<Integer>> newGraph();

    /**
     * generates a graph that contains all Integers from 1 to 24 as vertices and
     * edges between all integers for which (i + j) is even. As a result, the
     * graph has 313 edges in total, each odd vertex has 12 edges touching it and
     * each even vertex has 13 edges.
     *
     * @return the generated graph
     */
    protected Graph<Integer, Edge<Integer>> generateGraph() {
        Graph<Integer, Edge<Integer>> graph = newGraph();
        Integer[] integersArray = new Integer[25];

    /*
     * add all integers in [0,24]
     */
        for (int i = 0; i < 25; i++) {
            integersArray[i] = new Integer(i);
            graph.addVertex(integersArray[i]);
        }

    /*
     * create edges between all integers i, j for which (i + j) is even
     */
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if ((i + j) % 2 != 0) {
                    continue;
                }
                graph.addEdge(new UnweightedEdge(integersArray[i], integersArray[j]));
            }
        }

        return graph;
    }

    /**
     * Test of addVertex method, of class Graph.
     */
    public void testAddVertex() {
        System.out.println("addVertex");

        Graph<Integer, Edge<Integer>> graph = newGraph();

        for (int i = 0; i < 25; i++) {
            Assert.assertTrue(graph.addVertex(new Integer(i)));
        }

        for (int i = 0; i < 25; i++) {
            Assert.assertFalse(graph.addVertex(new Integer(i)));
        }
    }

    /**
     * Test of addEdge method, of class Graph.
     */
    public void testAddEdge() {
        System.out.println("addEdge");

        Graph<Integer, Edge<Integer>> graph = newGraph();

        for (int i = 0; i < 25; i++) {
            graph.addVertex(new Integer(i));
        }

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                Assert
                    .assertTrue(graph.addEdge(new UnweightedEdge(new Integer(i), new Integer(j))));
            }
        }

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                Assert
                    .assertFalse(graph.addEdge(new UnweightedEdge(new Integer(i), new Integer(j))));
            }
        }
    }

    /**
     * Test of containsEdge method, of class Graph.
     */
    public void testContainsEdge() {
        System.out.println("containsEdge");

        Graph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if ((i + j) % 2 != 0) {
                    Assert.assertFalse(graph.containsEdge(new Integer(i), new Integer(j)));
                } else {
                    Assert.assertTrue(graph.containsEdge(new Integer(i), new Integer(j)));
                }
            }
        }

    }

    /**
     * Test of containsVertex method, of class Graph.
     */
    public void testContainsVertex() {
        System.out.println("containsVertex");

        Graph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            Assert.assertTrue(graph.containsVertex(new Integer(i)));
        }
    }

    /**
     * Test of verticesSet method, of class Graph.
     */
    public void testVerticesSet() {
        System.out.println("verticesSet");

        Assert.assertTrue(generateGraph().verticesSet().size() == 25);
    }

    /**
     * Test of getEdge method, of class Graph.
     */
    public void testGetEdge() {
        System.out.println("getEdge");

        Graph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if ((i + j) % 2 != 0) {
                    Assert.assertNull(graph.getEdge(new Integer(i), new Integer(j)));
                } else {
                    Assert.assertNotNull(graph.getEdge(new Integer(i), new Integer(j)));
                }
            }
        }
    }

    /**
     * Test of edgesSet method, of class Graph.
     */
    public void testEdgesSet_0args() {
        System.out.println("edgesSet");

        Set<Edge<Integer>> set = generateGraph().edgesSet();

        Assert.assertEquals(set.size(), 313);
        for (Edge<Integer> edge : set) {
            int sum = edge.getSourceVertex().intValue() + edge.getTargetVertex().intValue();
            Assert.assertTrue(sum % 2 == 0);
        }
    }

    /**
     * Test of edgesSet method, of class Graph.
     */
    public void testEdgesSet_ofVertex() {
        System.out.println("edgesSet");

        Graph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            Set<Edge<Integer>> set = graph.edgesSet(new Integer(i));

            Assert.assertEquals(set.size(), i % 2 == 0 ? 25 : 23);
            for (Edge<Integer> edge : set) {
                Assert.assertTrue((edge.getTargetVertex() + i) % 2 == 0);
            }
        }
    }

    /**
     * Test of edgesSet method, of class Graph.
     */
    public void testEdgesSet_ofVertices() {
        System.out.println("edgesSet");

        Graph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                Set<Edge<Integer>> set = graph.edgesSet(new Integer(i), new Integer(j));

                if ((i + j) % 2 == 0) {
                    Assert.assertEquals(set.size(), 1);
                } else {
                    Assert.assertEquals(set.size(), 0);
                }
            }
        }
    }

    /**
     * Test of removeEdge method, of class Graph.
     */
    public void testRemoveEdge_betweenVertices() {
        System.out.println("removeEdge");

        Graph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if ((i + j) % 2 != 0) {
                    continue;
                }

                Assert.assertNotNull(graph.removeEdge(new Integer(i), new Integer(j)));
                Assert.assertFalse(graph.containsEdge(new Integer(i), new Integer(j)));
            }
        }
    }

    /**
     * Test of removeEdge method, of class Graph.
     */
    public void testRemoveEdge_edge() {
        System.out.println("removeEdge");

        Graph<Integer, Edge<Integer>> graph = generateGraph();

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if ((i + j) % 2 != 0) {
                    continue;
                }

                Assert.assertNotNull(
                    graph.removeEdge(new UnweightedEdge(new Integer(i), new Integer(j))));
                Assert.assertFalse(graph.containsEdge(new Integer(i), new Integer(j)));
            }
        }
    }

    /**
     * Test of removeAllEdges method, of class Graph.
     */
    public void testRemoveAllEdges() {
        System.out.println("removeAllEdges");

        Graph<Integer, Edge<Integer>> graph = generateGraph();
        Assert.assertTrue(graph.removeAllEdges());
        Assert.assertEquals(graph.edgesSet().size(), 0);
    }

    /**
     * Test of removeVertex method, of class Graph.
     */
    public void testRemoveVertex() {
        System.out.println("removeVertex");

        Graph<Integer, Edge<Integer>> graph = generateGraph();
        for (int i = 24; i >= 0; i--) {
            Assert.assertTrue(graph.removeVertex(new Integer(i)));
            Assert.assertFalse(graph.containsVertex(new Integer(i)));
            Assert.assertEquals(graph.verticesSet().size(), i);
        }
    }

    /**
     * Test of removeAllVertices method, of class Graph.
     */
    public void testRemoveAllVertices() {
        System.out.println("removeAllVertices");

        Graph<Integer, Edge<Integer>> graph = generateGraph();
        Assert.assertTrue(graph.removeAllVertices());
        Assert.assertEquals(graph.verticesSet().size(), 0);
    }
}
