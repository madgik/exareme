/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.check;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author heraldkllapi
 */
public class CheckTest extends TestCase {

    public CheckTest() {
    }

    /**
     * Test of NotNull method, of class Check.
     */
    public void testNotNull_Object() {
        System.out.println("NotNull");
        Object obj = null;
        Check.NotNull(obj);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }

    /**
     * Test of NotNull method, of class Check.
     */
    public void testNotNull_Object_String() {
        System.out.println("NotNull");
        Object obj = null;
        String msg = "";
        Check.NotNull(obj, msg);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }

    /**
     * Test of Equals method, of class Check.
     */
    public void testEquals() {
        System.out.println("Equals");
        Object o1 = null;
        Object o2 = null;
        Check.Equals(o1, o2);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }

    /**
     * Test of True method, of class Check.
     */
    public void testTrue_boolean() {
        System.out.println("True");
        boolean cond = false;
        Check.True(cond);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }

    /**
     * Test of True method, of class Check.
     */
    public void testTrue_boolean_String() {
        System.out.println("True");
        boolean cond = false;
        String msg = "";
        Check.True(cond, msg);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }
}
