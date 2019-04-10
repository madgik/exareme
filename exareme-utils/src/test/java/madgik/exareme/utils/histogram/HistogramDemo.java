/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.histogram.partitionRule.PartitionClass;
import madgik.exareme.utils.histogram.partitionRule.PartitionConstraint;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;

import java.util.*;

/**
 * @author herald
 */
public class HistogramDemo {

    public static void main(String[] args) throws Exception {
        Random rand = new Random();
        ArrayList<Pair<?, Double>> data = new ArrayList<Pair<?, Double>>(50);
        for (int i = 0; i < 40; i++) {
            data.add(new Pair<Integer, Double>(i, rand.nextDouble() * 30));
        }
        for (int i = 40; i < 50; i++) {
            data.add(new Pair<Integer, Double>(i, 30.0 + rand.nextDouble() * 30));
        }
        /* Sort */
        Collections.sort(data, new Comparator<Pair<?, Double>>() {
            public int compare(Pair<?, Double> o1, Pair<?, Double> o2) {
                return o1.b.compareTo(o2.b);
            }
        });
        Histogram histogram =
                new Histogram(new PartitionRule(PartitionClass.serial, PartitionConstraint.equi_width));

        //    LinkedList<Bucket> bucketList = histogram.createHistogram(
        //            data,
        //            new BalanceVarianceAndBucketsHistogramScore(0.9),
        //            new LinearBucketNumberGenerator(2, 1, 40));

        LinkedList<Bucket> bucketList = histogram.createHistogram(data, 6);
    }
}
