/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.constructionAlgorithm;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.histogram.Bucket;
import madgik.exareme.utils.histogram.partitionRule.PartitionRule;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Given a particular partition rule, this is the algorithm
 * that constructs histograms that satisfy the rule. It
 * is often the case that, for the same histogram class,
 * there are several construction algorithms with different efficiency.
 * [The History of Histograms Yannis Ioannidis]
 *
 * @author herald
 */
public interface ConstructionAlgorithm extends Serializable {

    LinkedList<Bucket> createHistogram(ArrayList<Pair<?, Double>> data, int bucketNum,
                                       PartitionRule partitionRule) throws RemoteException;
}
