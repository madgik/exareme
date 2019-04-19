/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.constructionAlgorithm.vOptimalSA;

import madgik.exareme.utils.association.Pair;
import madgik.exareme.utils.simulatedAnnealing.State;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

public class VOptimalState implements State {

    private static final long serialVersionUID = 1L;
    public int[] thresholds = null;
    private ArrayList<Pair<?, Double>> data = null;

    public VOptimalState(ArrayList<Pair<?, Double>> data, int[] thresholds) {
        this.data = data;
        this.thresholds = thresholds;
    }

    public double getCost() {
        double cost = 0;

        int prev = 0;
        for (int t : thresholds) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int i = prev; i < t; i++) {
                stats.addValue(data.get(i).b);
            }
            cost += stats.getN() * stats.getVariance();
            prev = t;
        }

        /* last bucket */
        DescriptiveStatistics stats = new DescriptiveStatistics();
        int max = data.size();
        for (int i = prev; i < max; i++) {
            stats.addValue(data.get(i).b);
        }
        cost += stats.getN() * stats.getVariance();

        return cost;
    }

    @Override
    public State clone() {
        int[] newThresholds = new int[thresholds.length];
        System.arraycopy(thresholds, 0, newThresholds, 0, newThresholds.length);

        return new VOptimalState(data, newThresholds);
    }
}
