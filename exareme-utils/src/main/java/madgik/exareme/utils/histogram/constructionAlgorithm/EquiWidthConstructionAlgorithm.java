/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.constructionAlgorithm;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.histogram.Bucket;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author herald
 */
public class EquiWidthConstructionAlgorithm implements ConstructionAlgorithm {

    private static final long serialVersionUID = 1L;

    public LinkedList<Bucket> createHistogram(ArrayList<Pair<?, Double>> data, int bucketNum,
        PartitionRule partitionRule) throws RemoteException {
        if (data.isEmpty()) {
            LinkedList<Bucket> bucketList = new LinkedList<Bucket>();
            for (int i = 0; i < bucketNum; ++i) {
                bucketList.add(new Bucket(new LinkedList<Pair<?, Double>>()));
            }

            return bucketList;
        }

        double min = data.get(0).b;
        double max = data.get(data.size() - 1).b;
        double step = (max - min) / (double) bucketNum;

        ArrayList<LinkedList<Pair<?, Double>>> buckets =
            new ArrayList<LinkedList<Pair<?, Double>>>();
        for (int i = 0; i < bucketNum; ++i) {
            buckets.add(new LinkedList<Pair<?, Double>>());
        }

        for (Pair<?, Double> d : data) {
            int b = (int) ((d.b - min) / step);
            if (b == bucketNum) {
                b--;
            }
            LinkedList<Pair<?, Double>> bData = buckets.get(b);
            bData.add(d);
        }

        LinkedList<Bucket> bucketList = new LinkedList<Bucket>();
        for (LinkedList<Pair<?, Double>> bData : buckets) {
            bucketList.add(new Bucket(bData));
        }

        return bucketList;
    }
}
