/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.combinatorics;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.math.BigInteger;

/**
 * @author heraldkllapi
 */
public class FactorialTest extends TestCase {

    public FactorialTest() {
    }

    /**
     * Test of complete method, of class Factorial.
     */
    public void testComplete() {
        System.out.println("complete");
        int n = 0;
        BigInteger[] expResult = null;
        BigInteger[] result = Factorial.complete(n);
        //    assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        Assert.fail("The test case is a prototype.");
    }
}
