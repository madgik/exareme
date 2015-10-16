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
public class BalanceVarianceAndBucketsHistogramScore implements HistogramScore {

    public double a = 0.0;

    public BalanceVarianceAndBucketsHistogramScore(double a) {
        this.a = a;
    }

    public double getScore(LinkedList<Bucket> bucketList) {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (Bucket b : bucketList) {
            DescriptiveStatistics bs = new DescriptiveStatistics();
            for (Pair<?, Double> d : b.data) {
                bs.addValue(d.b);
            }

            stats.addValue(bs.getStandardDeviation());
        }

        return -(a * stats.getStandardDeviation() + (1.0 - a) * bucketList.size());
    }
}
