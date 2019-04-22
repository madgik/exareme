package madgik.exareme.utils.graph.edge;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Random;

/**
 * @author konikos
 */
public class WeightedEdgeTest extends TestCase {

    protected WeightedEdge<Integer, Integer> newEdge(Integer source, Integer target,
                                                     Integer weight) {
        return new WeightedEdge<Integer, Integer>(source, target, weight);
    }

    /**
     * Test of getWeight method, of class WeightedEdge.
     */
    public void testGetWeight() {
        Integer weight = new Integer(new Random().nextInt());
        Assert.assertEquals(newEdge(null, null, weight).getWeight(), weight);
    }

    /**
     * Test of equals method, of class WeightedEdge.
     */
    public void testEquals() {
        Random r = new Random();
        Integer source = new Integer(r.nextInt());
        Integer target = new Integer(r.nextInt());
        Integer weight = new Integer(r.nextInt());

        WeightedEdge<Integer, Integer> edge = newEdge(source, target, weight);

        Assert.assertEquals(edge, newEdge(source, target, weight));
        Assert.assertFalse(edge.equals(newEdge(source, target, weight + 1)));
    }

    /**
     * Test of hashCode method, of class WeightedEdge.
     */
    public void testHashCode() {
        Random r = new Random();
        Integer source = new Integer(r.nextInt());
        Integer target = new Integer(r.nextInt());
        Integer weight = new Integer(r.nextInt());

        Assert.assertTrue(
                newEdge(source, target, weight).hashCode() == newEdge(source, target, weight)
                        .hashCode());
    }
}
