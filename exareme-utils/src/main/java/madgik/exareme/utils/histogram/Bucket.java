/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram;

import madgik.exareme.utils.association.Pair;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author herald
 */
public class Bucket implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    public List<Pair<?, Double>> data = null;

    public Bucket(List<Pair<?, Double>> data) {
        this.data = data;
    }

    @Override public Bucket clone() {
        return new Bucket(new ArrayList<Pair<?, Double>>(this.data));
    }

    public DescriptiveStatistics getStatistics() {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (Pair<?, Double> d : data) {
            statistics.addValue(d.b);
        }
        return statistics;
    }
}
