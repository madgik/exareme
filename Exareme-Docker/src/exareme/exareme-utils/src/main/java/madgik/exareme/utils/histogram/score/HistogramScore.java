/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.score;

import madgik.exareme.utils.histogram.Bucket;

import java.util.LinkedList;

/**
 * @author herald
 */
public interface HistogramScore {

    double getScore(LinkedList<Bucket> bucketList);
}
