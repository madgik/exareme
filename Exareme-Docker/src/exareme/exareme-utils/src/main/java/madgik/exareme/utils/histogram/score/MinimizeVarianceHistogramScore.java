/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.score;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.histogram.Bucket;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.LinkedList;

/**
 * @author herald
 */
public class MinimizeVarianceHistogramScore implements HistogramScore {

    public static MinimizeVarianceHistogramScore instance = new MinimizeVarianceHistogramScore();

    public double getScore(LinkedList<Bucket> bucketList) {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (Bucket b : bucketList) {
            DescriptiveStatistics bs = new DescriptiveStatistics();
            for (Pair<?, Double> d : b.data) {
                bs.addValue(d.b);
            }

            stats.addValue(bs.getStandardDeviation());
        }

        return -stats.getStandardDeviation();
    }
}
