/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.statistics;

import java.util.Random;

/**
 * @author heraldkllapi
 */
public class Poisson {
    private final double L;
    private final Random rand;

    public Poisson(double lambda, int seed) {
        L = Math.pow(Math.E, -lambda);
        rand = new Random(seed);
    }

    public int next() {
        double p = 1.0;
        int k = 0;
        do {
            k++;
            p *= rand.nextDouble();
        } while (p > L);
        return k - 1;
    }
}
