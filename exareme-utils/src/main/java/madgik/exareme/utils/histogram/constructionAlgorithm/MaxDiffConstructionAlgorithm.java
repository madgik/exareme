/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.constructionAlgorithm;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.histogram.Bucket;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;

import java.rmi.RemoteException;
import java.util.*;

/**
 * @author herald
 */
public class MaxDiffConstructionAlgorithm implements ConstructionAlgorithm {

    private static final long serialVersionUID = 1L;

    public LinkedList<Bucket> createHistogram(ArrayList<Pair<?, Double>> data, int bucketNum,
        PartitionRule partitionRule) throws RemoteException {

        PriorityQueue<Pair<Double, Integer>> diffQueue =
            new PriorityQueue<Pair<Double, Integer>>(bucketNum,
                new Comparator<Pair<Double, Integer>>() {

                    public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                        return o1.a.compareTo(o2.a);
                    }
                });

        for (int i = 0; i < data.size() - 1; i++) {
            Pair<?, Double> d1 = data.get(i);
            Pair<?, Double> d2 = data.get(i + 1);

            diffQueue.offer(new Pair<Double, Integer>(d2.b - d1.b, i + 1));

      /* Remove the first */
            if (i >= bucketNum - 1) {
                diffQueue.poll();
            }
        }

        //	for(Triple<Double, Integer, Integer> t : diffQueue) {
        //	    log.debug(">> " + t.a);
        //	}

        LinkedList<Bucket> bucketList = new LinkedList<Bucket>();

    /* Sort the buckets */
        ArrayList<Pair<Double, Integer>> thresholdArray =
            new ArrayList<Pair<Double, Integer>>(diffQueue);
        Collections.sort(thresholdArray, new Comparator<Pair<Double, Integer>>() {

            public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                return o1.b.compareTo(o2.b);
            }
        });

        int index = 0;
        for (Pair<Double, Integer> t : thresholdArray) {
            Bucket b = new Bucket(data.subList(index, t.b));
            bucketList.add(b);

            index = t.b;
        }

        Bucket b = new Bucket(data.subList(index, data.size()));
        bucketList.add(b);

        return bucketList;
    }
}
