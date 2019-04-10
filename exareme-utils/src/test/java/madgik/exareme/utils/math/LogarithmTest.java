/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.math;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author heraldkllapi
 */
public class LogarithmTest extends TestCase {

    private static final Logger log = Logger.getLogger(LogarithmTest.class);

    public LogarithmTest() {
        Logger.getRootLogger().setLevel(Level.ALL);
    }

    public void testCorrectness() {
        for (int num = 1; num < 100000; ++num) {
            for (int base = 2; base < 100; ++base) {
                //        log.debug(num + " / " + base);
                Assert.assertTrue(Math.abs(
                        Logarithm.DiscreeteLogCeil(num, base) - Logarithm.DiscreeteLogCeil2(num, base))
                        <= 1);
                //        assertEquals(Logarithm.DiscreeteLogFloor(num, base),
                //                     Logarithm.DiscreeteLogFloor2(num, base));
            }
        }
    }

    public void testPerformance() {
        int max_num = 1000000;
        int max_base = 100;
        {
            long start = System.currentTimeMillis();
            long total = 0;
            for (int num = 1; num < max_num; ++num) {
                for (int base = 2; base < max_base; ++base) {
                    total += Logarithm.DiscreeteLogCeil(num, base);
                }
            }
            long end = System.currentTimeMillis();
            log.info("Total 1: " + total);
            log.info("1      : " + (end - start));
        }
        {
            long start = System.currentTimeMillis();
            long total = 0;
            for (int num = 1; num < max_num; ++num) {
                for (int base = 2; base < max_base; ++base) {
                    total += Logarithm.DiscreeteLogCeil2(num, base);
                }
            }
            long end = System.currentTimeMillis();
            log.info("Total 2: " + total);
            log.info("2      : " + (end - start));
        }
    }
}
