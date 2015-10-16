/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.queryProcessor.optimizer.scheduleEstimator.transactional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author herald
 */
public class TransactionalBitSetTest {

    public TransactionalBitSetTest() {
    }

    /**
     * Test of rollback method, of class TransactionalBitSet.
     */
    @Test public void testBitSet() {
        TransactionalBitSet tbs = new TransactionalBitSet();

        tbs.set(10);
        assertEquals(1, tbs.cardinality());
        assertEquals(true, tbs.getValue().get(10));

        tbs.commit();
        assertEquals(1, tbs.cardinality());
        assertEquals(true, tbs.getValue().get(10));

        tbs.set(20, 30);
        assertEquals(11, tbs.cardinality());
        assertEquals(true, tbs.getValue().get(10));
        for (int i = 20; i < 30; ++i) {
            assertEquals(true, tbs.getValue().get(i));
        }

        tbs.rollback();
        assertEquals(1, tbs.cardinality());
        assertEquals(true, tbs.getValue().get(10));
    }
}
