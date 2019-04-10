/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.histogram.bucketNumberGenerator.BucketNumberGenerator;
import madgik.exareme.utils.histogram.constructionAlgorithm.ConstructionAlgorithm;
import madgik.exareme.utils.histogram.constructionAlgorithm.ConstructionAlgorithmFactory;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;
import madgik.exareme.utils.histogram.score.HistogramScore;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author herald
 */
public class Histogram {

    private static Logger log = Logger.getLogger(Histogram.class);
    private PartitionRule partitionRule = null;
    private ConstructionAlgorithm algorithm = null;
    //    private FrequencyApproximation frequencyApproximation = null;
    //    private ValueApproximation valueApproximation = null;

    public Histogram(PartitionRule partitionRule) {
        this.partitionRule = partitionRule;
        this.algorithm = ConstructionAlgorithmFactory.getAlgorithm(partitionRule);
        //	this.frequencyApproximation = frequencyApproximation;
        //	this.valueApproximation = valueApproximation;
    }

    /**
     * @param data      the dataset with <key,value> pairs
     * @param bucketNum the number of buckets
     * @return a list of buckets
     * @throws RemoteException in case of exception
     */
    public LinkedList<Bucket> createHistogram(ArrayList<Pair<?, Double>> data, int bucketNum)
            throws RemoteException {

        LinkedList<Bucket> bucketList = algorithm.createHistogram(data, bucketNum, partitionRule);

        /* TODO: approximate value and frequency */

        return bucketList;
    }

    /**
     * @param data
     * @param score
     * @param generator
     * @return the histogram with the maximum score
     * @throws RemoteException
     */
    public LinkedList<Bucket> createHistogram(ArrayList<Pair<?, Double>> data, HistogramScore score,
                                              BucketNumberGenerator generator) throws RemoteException {

        LinkedList<Bucket> best = null;
        double bestScore = 0;

        while (generator.hasNext()) {
            int next = generator.getNext();

            if (best == null) {
                best = algorithm.createHistogram(data, next, partitionRule);
                bestScore = score.getScore(best);
                continue;
            }

            LinkedList<Bucket> h = algorithm.createHistogram(data, next, partitionRule);
            double s = score.getScore(h);

            log.debug(bestScore + "(" + best.size() + ")" + " >=? " + s + "(" + next + ")");

            if (s > bestScore) {
                best = h;
                bestScore = s;
            }
        }

        return best;
    }
}
