/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.bucketNumberGenerator;

/**
 * @author Herald Kllapi <br>
 * University of Athens /
 * Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class LinearBucketNumberGenerator implements BucketNumberGenerator {

    private int step = 0;
    private int max = 0;
    private int current = 0;

    public LinearBucketNumberGenerator(int start, int step, int max) {
        this.step = step;
        this.max = max;

        this.current = start;
    }

    public boolean hasNext() {
        return current <= max;

    }

    public int getNext() {
        int ret = current;
        current += step;
        return ret;
    }
}
