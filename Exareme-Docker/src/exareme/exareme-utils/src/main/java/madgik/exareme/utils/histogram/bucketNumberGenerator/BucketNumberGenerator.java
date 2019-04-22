/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.bucketNumberGenerator;

/**
 * @author herald
 */
public interface BucketNumberGenerator {

    boolean hasNext();

    int getNext();
}
