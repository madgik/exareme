package madgik.exareme.master.queryProcessor.graph;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author konikos
 */
public class AbstractOperatorTest extends TestCase {
    /**
     * Test of getName method, of class AbstractOperator.
     */
    public void testGetName() {
        System.out.println("getName");

        assertEquals(new AbstractOperator("id").getName(), new AbstractOperator("id").getName());
        Assert.assertFalse(new AbstractOperator("id").getName()
                .equals(new AbstractOperator("another.id").getName()));
    }

    /**
     * Test of getConcreteOperators method, of class AbstractOperator.
     //     */
//    public void testGetConcreteOperators() {
//        System.out.println("getConcreteOperators");
//        Assert.fail("The test case is a prototype.");
//    }

    /**
     * Test of equals method, of class AbstractOperator.
     */
//    public void testEquals() {
//        System.out.println("equals");
//
//        assertEquals(new AbstractOperator("id"), new AbstractOperator("id"));
//        Assert.assertFalse(new AbstractOperator("id").
//            equals(new AbstractOperator("another.id")));
//    }

    /**
     * Test of toString method, of class AbstractOperator.
     */
    public void testToString() {
        System.out.println("toString");

        assertEquals(new AbstractOperator("id").toString(), new AbstractOperator("id").toString());
        Assert.assertFalse(new AbstractOperator("id").toString()
                .equals(new AbstractOperator("another.id").toString()));
    }
}
