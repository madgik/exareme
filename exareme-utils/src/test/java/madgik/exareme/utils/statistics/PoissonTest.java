package madgik.exareme.utils.statistics;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * @author heraldkllapi
 */
public class PoissonTest extends TestCase {

    public PoissonTest(String testName) {
        super(testName);
    }

    @Override protected void setUp() throws Exception {
        super.setUp();
    }

    @Override protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of next method, of class Poisson.
     */
    public void testNext() {
        System.out.println("next");
        for (double l = 1; l < 10.0; l++) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            Poisson poisson = new Poisson(l, 0);
            for (int i = 0; i < 100000; i++) {
                stats.addValue(poisson.next());
            }
            // In poison, mean and variance are equal to lambda
            Assert.assertEquals(l, stats.getMean(), 0.1);
            Assert.assertEquals(l, stats.getVariance(), 0.1);
           // System.out.println(l + "\t" + stats.getMean() + "\t" + stats.getVariance());
        }
    }

    public void testCos() {
        for (double step = 1; step < 7200.0; step++) {
            double l = 5.0 + 4 * Math.cos(step / 200);
            Poisson poisson = new Poisson(l, 0);
           // System.out.println(poisson.next());
        }
    }
}
