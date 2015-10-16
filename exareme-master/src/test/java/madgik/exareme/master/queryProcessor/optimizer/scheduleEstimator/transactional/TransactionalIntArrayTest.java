/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author heraldkllapi
 */
public class TransactionalIntArrayTest {

    public TransactionalIntArrayTest() {
    }

    @Test public void testIntArray() {
        TransactionalIntArray tia = new TransactionalIntArray(100);
        tia.set(5, 10);
        assertEquals(10, tia.getValue(5));

        tia.commit();
        assertEquals(10, tia.getValue(5));

        tia.set(7, 20);
        assertEquals(20, tia.getValue(7));
        tia.rollback();

        assertEquals(0, tia.getValue(7));
    }
}
