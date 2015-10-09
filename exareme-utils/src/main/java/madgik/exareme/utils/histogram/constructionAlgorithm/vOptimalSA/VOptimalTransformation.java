/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.utils.histogram.constructionAlgorithm.vOptimalSA;

import madgik.exareme.utils.simulatedAnnealing.State;
import madgik.exareme.utils.simulatedAnnealing.Transformation;

public class VOptimalTransformation implements Transformation {

    private static final long serialVersionUID = 1L;
    int threshold = -1;
    int offset = -1;

    public VOptimalTransformation(int threshold, int offset) {
        this.threshold = threshold;
        this.offset = offset;
    }

    public State apply(State state) {
        VOptimalState vos = (VOptimalState) state;
        vos.thresholds[threshold] += offset;
        return vos;
    }
}
