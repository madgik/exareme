/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author heraldkllapi
 */
public class TransactionalIntegerTest {

    public TransactionalIntegerTest() {
    }

    @Test public void testInteger() {
        TransactionalInteger ti = new TransactionalInteger(5);
        assertEquals(5, ti.getValue());

        ti.delta(2);
        assertEquals(7, ti.getValue());

        ti.delta(-10);
        assertEquals(-3, ti.getValue());

        ti.commit();
        assertEquals(-3, ti.getValue());

        ti.delta(20);
        assertEquals(17, ti.getValue());

        ti.rollback();
        assertEquals(-3, ti.getValue());

        ti.setValue(5);
        assertEquals(5, ti.getValue());

        ti.commit();
        assertEquals(5, ti.getValue());
    }
}
