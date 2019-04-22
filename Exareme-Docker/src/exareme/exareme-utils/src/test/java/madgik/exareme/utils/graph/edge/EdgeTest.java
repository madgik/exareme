package madgik.exareme.utils.graph.edge;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Random;

/**
 * @author konikos
 */
abstract public class EdgeTest extends TestCase {

    abstract protected Edge<Integer> newEdge(Integer source, Integer target);

    /**
     * Test of getSourceVertex method, of class Edge.
     */
    public void testGetSourceVertex() {
        Integer source = new Integer(new Random().nextInt());
        Assert.assertEquals(newEdge(source, null).getSourceVertex(), source);
    }

    /**
     * Test of getTargetVertex method, of class Edge.
     */
    public void testGetTargetVertex() {
        Integer target = new Integer(new Random().nextInt());
        Assert.assertEquals(newEdge(null, target).getTargetVertex(), target);
    }

    public void testEquals() {
        Random r = new Random();
        Integer source = new Integer(r.nextInt());
        Integer target = new Integer(r.nextInt());

        Assert.assertEquals(newEdge(source, target), newEdge(source, target));
        Assert.assertFalse(newEdge(source, target).equals(newEdge(source, target + 1)));
    }
}
