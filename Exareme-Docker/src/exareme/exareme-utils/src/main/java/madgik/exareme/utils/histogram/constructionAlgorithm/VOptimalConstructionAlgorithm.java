/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.constructionAlgorithm;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.histogram.Bucket;
import madgik.exareme.utils.histogram.constructionAlgorithm.vOptimalSA.VOptimalSA;
import madgik.exareme.utils.histogram.constructionAlgorithm.vOptimalSA.VOptimalState;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;
import madgik.exareme.utils.simulatedAnnealing.LogarithmicTemperature;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author herald
 */
public class VOptimalConstructionAlgorithm implements ConstructionAlgorithm {

    private static final long serialVersionUID = 1L;

    public LinkedList<Bucket> createHistogram(ArrayList<Pair<?, Double>> data, int bucketNum,
                                              PartitionRule partitionRule) throws RemoteException {

        if (bucketNum < 1) {
            throw new AccessException("Bucket num should be > 0");
        }

        LinkedList<Bucket> bucketList = new LinkedList<Bucket>();

        // Put everything into 1 bucket.
        if (bucketNum == 1) {
            Bucket b = new Bucket(data);
            bucketList.add(b);

            return bucketList;
        }

        VOptimalSA sa =
                new VOptimalSA(10000, 1000, new LogarithmicTemperature(1.0), data, bucketNum);

        VOptimalState result = (VOptimalState) sa.search();

        int index = 0;
        for (int t : result.thresholds) {
            Bucket b = new Bucket(data.subList(index, t));
            bucketList.add(b);

            index = t;
        }

        Bucket b = new Bucket(data.subList(index, data.size()));
        bucketList.add(b);

        return bucketList;
    }
}
